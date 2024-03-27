package fr.thomas.quizzgameserver.controller;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.EnglishSequenceData;
import org.passay.IllegalSequenceRule;
import org.passay.LengthRule;
import org.passay.PasswordValidator;
import org.passay.WhitespaceRule;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import fr.thomas.quizzgameserver.controller.threading.GameThread;
import fr.thomas.quizzgameserver.model.Answer;
import fr.thomas.quizzgameserver.model.OnlineGame;
import fr.thomas.quizzgameserver.model.Player;
import fr.thomas.quizzgameserver.net.object.AnswerNetObject;
import fr.thomas.quizzgameserver.net.object.OnlineGameNetObject;
import fr.thomas.quizzgameserver.net.object.PlayerNetObject;
import fr.thomas.quizzgameserver.net.object.QuestionNetObject;
import fr.thomas.quizzgameserver.net.request.Broadcast.ServerInfoRefresh;
import fr.thomas.quizzgameserver.net.request.Login;
import fr.thomas.quizzgameserver.net.request.Login.LoginRequest;
import fr.thomas.quizzgameserver.net.request.Login.LoginResponse;
import fr.thomas.quizzgameserver.net.request.ServerInfo.ServerCountDown;
import fr.thomas.quizzgameserver.net.request.ServerInfo.ServerInfoRequest;
import fr.thomas.quizzgameserver.net.request.ServerInfo.ServerInfoResponse;
import fr.thomas.quizzgameserver.net.request.ServerJoin.ServerJoinRequest;
import fr.thomas.quizzgameserver.net.request.ServerJoin.ServerJoinResponse;
import fr.thomas.quizzgameserver.net.request.ServerList.ServerListRequest;
import fr.thomas.quizzgameserver.net.request.ServerList.ServerListResponse;
import fr.thomas.quizzgameserver.net.request.ServerPlay.AnswerTimeLeft;
import fr.thomas.quizzgameserver.net.request.ServerPlay.GetPlayerAnswer;
import fr.thomas.quizzgameserver.net.request.ServerPlay.ServerEndGame;
import fr.thomas.quizzgameserver.net.request.ServerPlay.ShowQuestion;
import fr.thomas.quizzgameserver.net.request.ServerQuit.ServerQuitRequest;
import fr.thomas.quizzgameserver.net.request.ServerQuit.ServerQuitResponse;
import fr.thomas.quizzgameserver.utils.DatabaseHelper;

public class GameController {

	private Server server;
	private Kryo kryo;

	private DatabaseHelper databaseHelper;
	private Config myConfig;

	private PasswordValidator passwordValidator;
	
	private HashMap<Integer, OnlineGame> activeGames; //Game with at least one player waiting...
	private HashMap<Integer, GameThread> launchedThreads;
	
	// Server id and list of player IDs
	private HashMap<Integer, ArrayList<Integer>> gamePlayerList;
	
	private HashMap<Integer, Integer> socketPlayerMap;

	/**
	 * @author Thomas PRADEAU
	 */
	public GameController() {
		server = new Server();
		server.start();
		try {
			server.bind(54555, 54777);
		} catch (IOException e) {
			e.printStackTrace();
		}

		kryo = server.getKryo();
		kryo.register(Login.LoginRequest.class);
		kryo.register(Login.LoginResponse.class);
		kryo.register(ServerListResponse.class);
		kryo.register(ServerListRequest.class);
		kryo.register(PlayerNetObject.class);
		kryo.register(OnlineGameNetObject.class);
		kryo.register(ArrayList.class);
		kryo.register(ServerJoinRequest.class);
		kryo.register(ServerJoinResponse.class);
		kryo.register(ServerInfoRequest.class);
		kryo.register(ServerInfoResponse.class);
		kryo.register(ServerQuitResponse.class);
		kryo.register(ServerQuitRequest.class);
		kryo.register(ServerInfoRefresh.class);
		kryo.register(ServerCountDown.class);
		kryo.register(ShowQuestion.class);
		kryo.register(GetPlayerAnswer.class);
		kryo.register(QuestionNetObject.class);
		kryo.register(AnswerNetObject.class);
		kryo.register(ServerEndGame.class);
		kryo.register(AnswerTimeLeft.class);

		this.myConfig = new Config();
		
		this.socketPlayerMap = new HashMap<Integer, Integer>();
		this.activeGames = new HashMap<Integer, OnlineGame>();
		this.launchedThreads = new HashMap<Integer, GameThread>();
		
		try {
			this.databaseHelper = new DatabaseHelper(this);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		gamePlayerList = new HashMap<Integer, ArrayList<Integer>>();
		Player player = new Player("", this);
		
		server.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof LoginRequest) {
					LoginRequest login_request = (LoginRequest) object;
					LoginResponse login_response = new LoginResponse();

					if (player.authenticate(login_request.username, login_request.password)) {
						login_response.isConnected = true;
						PlayerNetObject playerNet = new PlayerNetObject(player.getID(), player.getName(),
								player.getPassword(), player.getHighestScore());
						login_response.player = playerNet;
						socketPlayerMap.put(player.getID(), connection.getID()); //Register player id by his socket id;
					} else {
						login_response.isConnected = false;
					}

					server.sendToTCP(connection.getID(), login_response);
				}

				if (object instanceof ServerListRequest) {
					ArrayList<OnlineGame> serverList = getServerList();
					ArrayList<OnlineGameNetObject> temp = new ArrayList<OnlineGameNetObject>();

					for (OnlineGame game : serverList) {
						temp.add(new OnlineGameNetObject(game.getId(), game.getName(), game.getMaxPlayer(),
								game.getMinPlayer(), game.getTimeToAnswer(), game.getGameStatus(), game.getTimeBeforeStart()));
					}

					ServerListResponse response = new ServerListResponse();
					response.servers = temp;

					server.sendToTCP(connection.getID(), response);
				}

				if (object instanceof ServerJoinRequest) {
					ServerJoinResponse response = new ServerJoinResponse();
					ServerJoinRequest request_data = (ServerJoinRequest) object;

					// Verify if the game can be joined.
					if (request_data.game.getGameStatus() == EOnlineGameStatus.CREATED.getStatusID()) {
						if (!gamePlayerList.get(request_data.game.getId()).contains(request_data.player.getId())) {
							gamePlayerList.get(request_data.game.getId()).add(request_data.player.getId());
							response.isJoinable = true;
							
							System.out.println("Player " + request_data.player.getName() + " joined game "
									+ request_data.game.getName());
							
							OnlineGame tmpGame = new OnlineGame(request_data.game);
							
							if(activeGames.containsKey(tmpGame.getId())) {
								//If game is already marked active (players already waiting)
								tmpGame = activeGames.get(request_data.game.getId());
							} else {
								activeGames.put(tmpGame.getId(), tmpGame);
							}
							
							tmpGame.updatePlayerList(gamePlayerList.get(request_data.game.getId()));
							
							if(tmpGame.isThereEnoughPlayer()) {
								GameThread thread = new GameThread(getController(), tmpGame);
								Thread gameThread = new Thread(thread);
								gameThread.setName("Thread-" + tmpGame.getName().replace(" ", "_"));
								
								launchedThreads.put(tmpGame.getId(), thread);
								gameThread.start();
							}
							
							System.out.println("New active game size : " + activeGames.size());
						} else {
							response.isJoinable = false;
							System.out.println("The player have already joined the game.");
						}

					} else
						response.isJoinable = false;

					server.sendToTCP(connection.getID(), response);
				}
				
				if (object instanceof ServerQuitRequest) {
					ServerQuitResponse response = new ServerQuitResponse();
					ServerQuitRequest request_data = (ServerQuitRequest) object;

					if (gamePlayerList.get(request_data.gameID).contains(request_data.playerID)) {
						gamePlayerList.get(request_data.gameID).remove(new Integer(request_data.playerID));
						
						ServerInfoRefresh refreshBroadcast = new ServerInfoRefresh();
						refreshBroadcast.playerIDs = gamePlayerList.get(request_data.playerID);
						server.sendToAllTCP(refreshBroadcast);
						
						response.hasQuit = true;
						
						OnlineGame tmpGame = activeGames.get(request_data.gameID);
						
						tmpGame.updatePlayerList(gamePlayerList.get(request_data.gameID));
						
						if(tmpGame.getPlayers().size() == 0) {
							activeGames.remove(tmpGame.getId());
							System.out.println("remove game from active game list");
						}
						
						for(OnlineGame activeGame : activeGames.values()) {
							if(!activeGame.isThereEnoughPlayer() && launchedThreads.containsKey(activeGame.getId())) {
								GameThread thread = launchedThreads.get(activeGame.getId());
								thread.abortStart();
							}
						}
						
						System.out.println("New active game size : " + activeGames.size());
						
					} else {
						response.hasQuit = false;
					}

					server.sendToTCP(connection.getID(), response);
				}

				if (object instanceof ServerInfoRequest) {
					ServerInfoResponse response = new ServerInfoResponse();
					ServerInfoRequest request_data = (ServerInfoRequest) object;

					// TODO change this for something more optimized plsssss - Like caching server
					// list
					OnlineGame game = null;
					for (OnlineGame server : getServerList()) {
						if (server.getId() == request_data.gameID) {
							game = server;
							break;
						}
					}

					response.gameStatus = game.getGameStatus();
					response.name = game.getName();
					response.onlinePlayer = gamePlayerList.get(request_data.gameID).size();
					response.maxPlayers = game.getMaxPlayer();
					response.minPlayers = game.getMinPlayer();
					response.players = gamePlayerList.get(request_data.gameID);
					server.sendToTCP(connection.getID(), response);
				}
				
				if(object instanceof GetPlayerAnswer) {
					GetPlayerAnswer request_data = (GetPlayerAnswer) object;
					
					GameThread currentThread = launchedThreads.get(request_data.onlineGameID);
					currentThread.givePlayerAnswer(request_data.playerID, new Answer(request_data.answer));
				}
			}
		});

		passwordValidator = new PasswordValidator(new LengthRule(12, 24),
				new CharacterRule(EnglishCharacterData.LowerCase, 1),
				new CharacterRule(EnglishCharacterData.UpperCase, 1), new CharacterRule(EnglishCharacterData.Digit, 1),
				new CharacterRule(EnglishCharacterData.Special, 1),
				new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 4, false),
				new IllegalSequenceRule(EnglishSequenceData.Numerical, 4, false),
				new IllegalSequenceRule(EnglishSequenceData.USQwerty, 4, false), new WhitespaceRule());

		// Initializes empty playerlist for each joinable server.
		for (OnlineGame server : getServerList()) {
			if (server.getGameStatus() == EOnlineGameStatus.CREATED.getStatusID()) {
				gamePlayerList.put(server.getId(), new ArrayList<Integer>());
			}
		}

	}
	
	/**
	 * Send the same message to every players in the arraylist specified
	 * @param recievers
	 * @param object
	 */
	public void sendTCPTo(ArrayList<Integer> recievers, Object object) {
		for(int reciever : recievers) {
			server.sendToTCP(socketPlayerMap.get(reciever), object);
			
			if(object instanceof ShowQuestion) {
				System.out.println("Question showed : " + ((ShowQuestion) object).question.getLabel());
			}
		}
	}
	
	public ArrayList<OnlineGame> getServerList() {
		try {
			Statement st = getDatabaseHelper().getStatement(0);
			ResultSet res = st.executeQuery("SELECT * FROM OnlineGame;");
			ArrayList<OnlineGame> serverList = new ArrayList<>();

			while (res.next()) {
				serverList.add(new OnlineGame(this, res.getInt("id"), res.getString("name"), res.getInt("max_player"),
						res.getInt("min_player"), res.getInt("time_to_answer"), res.getInt("game_status"), res.getInt("time_before_start")));
			}

			return serverList;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public GameController getController() {
		return this;
	}

	public DatabaseHelper getDatabaseHelper() {
		return databaseHelper;
	}

	public Config getMyConfig() {
		return myConfig;
	}
	
	public HashMap<Integer, Integer> getSocketPlayerMap() {
		return socketPlayerMap;
	}

	public void setMyConfig(Config myConfig) {
		this.myConfig = myConfig;
	}
}
