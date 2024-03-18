package fr.thomas.quizzgameserver.controller;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.EnglishSequenceData;
import org.passay.IllegalSequenceRule;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import fr.thomas.quizzgameserver.model.Answer;
import fr.thomas.quizzgameserver.model.Game;
import fr.thomas.quizzgameserver.model.OnlineGame;
import fr.thomas.quizzgameserver.model.Player;
import fr.thomas.quizzgameserver.model.Question;
import fr.thomas.quizzgameserver.net.object.OnlineGameNetObject;
import fr.thomas.quizzgameserver.net.object.PlayerNetObject;
import fr.thomas.quizzgameserver.net.request.Broadcast.ServerInfoRefresh;
import fr.thomas.quizzgameserver.net.request.Login;
import fr.thomas.quizzgameserver.net.request.Login.LoginRequest;
import fr.thomas.quizzgameserver.net.request.Login.LoginResponse;
import fr.thomas.quizzgameserver.net.request.ServerInfo.ServerInfoRequest;
import fr.thomas.quizzgameserver.net.request.ServerInfo.ServerInfoResponse;
import fr.thomas.quizzgameserver.net.request.ServerJoin.ServerJoinRequest;
import fr.thomas.quizzgameserver.net.request.ServerJoin.ServerJoinResponse;
import fr.thomas.quizzgameserver.net.request.ServerList.ServerListRequest;
import fr.thomas.quizzgameserver.net.request.ServerList.ServerListResponse;
import fr.thomas.quizzgameserver.net.request.ServerQuit.ServerQuitRequest;
import fr.thomas.quizzgameserver.net.request.ServerQuit.ServerQuitResponse;
import fr.thomas.quizzgameserver.utils.DatabaseHelper;

public class GameController {

	private Server server;
	private Kryo kryo;

	private DatabaseHelper databaseHelper;

	private Player player;
	private ArrayList<Question> questions;
	private Game game;
	private int diff;
	private Config myConfig;

	private boolean isGameStarted = false;

	private PasswordValidator passwordValidator;

	// Server id and list of player IDs
	private HashMap<Integer, ArrayList<Integer>> gamePlayerList;

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

		// Créer la vue
		this.myConfig = new Config();

		try {
			this.databaseHelper = new DatabaseHelper(this);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		player = new Player("", this);
		gamePlayerList = new HashMap<Integer, ArrayList<Integer>>();

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
								game.getMinPlayer(), game.getTimeToAnswer(), game.getGameStatus()));
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
							
							ServerInfoRefresh refreshBroadcast = new ServerInfoRefresh();
							refreshBroadcast.playerIDs = gamePlayerList.get(request_data.player.getId());
							server.sendToAllTCP(refreshBroadcast);
							
							System.out.println("Player " + request_data.player.getName() + " joined game "
									+ request_data.game.getName());
						} else {
							response.isJoinable = false;
							System.out.println("The player have already joined the game.");
						}

					} else
						response.isJoinable = false;

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
					response.players = gamePlayerList.get(request_data.gameID);
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
					} else {
						response.hasQuit = false;
					}

					server.sendToTCP(connection.getID(), response);
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

	public void playerAuth(String name, String password) {
		if (player.authenticate(name, password)) {
		} else {
			System.err.println("Invalid password...");
		}
	}

	public void startGame() {
		this.game = new Game(this, player);
		if (!isGameStarted) {
			game.getRandomQuestions(); // Choisir les questions aléatoirement
			game.begin();
			isGameStarted = true;
		}
	}

	public ArrayList<OnlineGame> getServerList() {
		try {
			Statement st = getDatabaseHelper().getStatement(0);
			ResultSet res = st.executeQuery("SELECT * FROM OnlineGame;");
			ArrayList<OnlineGame> serverList = new ArrayList<>();

			while (res.next()) {
				serverList.add(new OnlineGame(this, res.getInt("id"), res.getString("name"), res.getInt("max_player"),
						res.getInt("min_player"), res.getInt("time_to_answer"), res.getInt("game_status")));
			}

			return serverList;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void finishGame(HashMap<Question, Answer> gameHistory) {
		// TODO Show recap view
		isGameStarted = false;

		int gameScoreBuffer = 0;

		for (Map.Entry<Question, Answer> gameEntry : gameHistory.entrySet()) {
			if (gameEntry.getValue().isCorrect()) {
				gameScoreBuffer += gameEntry.getKey().getDifficulty() * 100;
			}
		}

		game.setScore(gameScoreBuffer);
		game.insert();

		// view.showGameRecap(gameHistory);

	}

	/**
	 * Asks controller to show password change view if the logins provided by the
	 * user are correct.
	 * 
	 * @param username
	 * @param password
	 */
	public void submitPasswordChange(String username, String password) {
		if (player.authenticate(username, password)) {
		} else {
		}
	}

	public void changePassword(String password, String confirm) {
		if (password.equals(confirm)) {
			final PasswordData passwordData = new PasswordData(password);
			final RuleResult validate = passwordValidator.validate(passwordData);

			if (!validate.isValid()) {
				return;
			}

			if (player.updatePassword(password)) {

			}
		} else {
		}
	}

	public ArrayList<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(ArrayList<Question> questions) {
		this.questions = questions;
	}

	public Player getPlayer() {
		return player;
	}

	public Game getGame() {
		return game;
	}

	public DatabaseHelper getDatabaseHelper() {
		return databaseHelper;
	}

	/**
	 * Password errors callback enumeration That allows to define error messages for
	 * each password requirement.
	 */

	public Config getMyConfig() {
		return myConfig;
	}

	public void setMyConfig(Config myConfig) {
		this.myConfig = myConfig;
	}
}
