package fr.thomas.quizzgameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fr.thomas.quizzgameserver.controller.GameController;
import fr.thomas.quizzgameserver.net.object.OnlineGameNetObject;

public class OnlineGame {
	
	private ArrayList<Integer> players;
	private ArrayList<Question> questions;

	private int id;
	private String name;
	private int maxPlayer;
	private int minPlayer;
	private int timeToAnswer;
	private int gameStatus;
	private int timeBeforeStart;
	
	private GameController controller;
	
	public OnlineGame(GameController controller) {
		this.controller = controller;
		this.players = new ArrayList<Integer>();
	} 
	
	public OnlineGame(OnlineGameNetObject netObject) {
		this.id = netObject.getId();
		this.name = netObject.getName();
		this.maxPlayer = netObject.getMaxPlayer();
		this.minPlayer = netObject.getMinPlayer();
		this.timeToAnswer = netObject.getTimeToAnswer();
		this.gameStatus = netObject.getGameStatus();
		this.timeBeforeStart = netObject.getTimeBeforeStart();
		
		this.players = new ArrayList<Integer>();
	}

	public OnlineGame(GameController controller, int id, String name, int maxPlayer, int minPlayer, int timeToAnswer, int gameStatus, int timeBeforeStart) {
		this.controller = controller;
		this.id = id;
		this.name = name;
		this.maxPlayer = maxPlayer;
		this.minPlayer = minPlayer;
		this.timeToAnswer = timeToAnswer;
		this.gameStatus = gameStatus;
		this.timeBeforeStart = timeBeforeStart;
		
		this.players = new ArrayList<Integer>();
	}
	
	public void addPlayer(Player player) {
		this.players.add(player.getID());
		System.out.println("[JOIN] Adding player " + player.getName() + " to game " + this.name + " | Player count [" + players.size() + " / " + maxPlayer + "]");
		System.out.println("============ CURRENT PLAYER LIST ==========");
		for(int loggedPlayer : players) {
			System.out.println("ID : " + loggedPlayer);
		}
	}
	
	public void removePlayer(Player player) {
		removePlayer(player.getID());
	}
	
	public void removePlayer(int playerID) {
		for(int player : players) {
			if(player == playerID) {
				players.remove(player);
				System.out.println("[LEAVE] Removing player " + player + " to game " + this.name + " | Player count [" + players.size() + " / " + maxPlayer + "]");
				break;
			}
		}
		
		System.out.println("============ CURRENT PLAYER LIST ==========");
		for(int loggedPlayer : players) {
			System.out.println("ID : " + loggedPlayer);
		}
	}
	
	public void updatePlayerList(ArrayList<Integer> newPlayerList) {
		players.clear();
		players.addAll(newPlayerList);
	}
	
	public void defineController(GameController controller) {
		this.controller = controller;
	}
	
	public void getRandomQuestions() {
		// DIFFICULTY : 1 = 5 Questions / 2 = 10 Questions / 3 = 30 Questions.
		ArrayList<Question> cq = new ArrayList<Question>();
		try {

			Statement st = controller.getDatabaseHelper().getStatement(0);
			int count = 0;
			ResultSet set = st.executeQuery("SELECT Count(*) as total FROM Question;");
			if (set.next()) {
				count = set.getInt("total");
			}

			List<Integer> randomNumbers = new ArrayList<>();
			for (int i = 1; i <= count; i++) {
				randomNumbers.add(i);
			}
			Random rand = new Random();
			Collections.shuffle(randomNumbers);
			int qcount = 5;
			int lim = rand.nextInt(0, count - qcount);

			List<Integer> selectedQuestions = randomNumbers.subList(lim, lim + 5);

			// Récupérer les questions correspondantes à ces nombres

			String sqlParams = "";
			for (int i = 0; i < qcount - 1; i++) {
				sqlParams += "?,";
			}

			sqlParams += "?";

			String query = "SELECT idquestion, label FROM Question WHERE idquestion IN (" + sqlParams + ")";
			PreparedStatement ps = controller.getDatabaseHelper().getCon().prepareStatement(query);
			for (int i = 1; i <= qcount; i++) {
				ps.setInt(i, selectedQuestions.get(i - 1));
			}
			ResultSet rs = ps.executeQuery();

			// Afficher les questions récupérées
			while (rs.next()) {
				int questionId = rs.getInt("idquestion");
				cq.add(new Question(questionId, this.controller));
			}

			this.questions = cq;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isThereEnoughPlayer() {
		System.out.println("There is/are : " + players.size() + " player(s)");
		return (players.size() >= minPlayer && players.size() <= maxPlayer);
	}

	public int getId() {
		return id;
	}

	public int getMaxPlayer() {
		return maxPlayer;
	}

	public int getMinPlayer() {
		return minPlayer;
	}
	
	public ArrayList<Integer> getPlayers() {
		return players;
	}
	
	public ArrayList<Question> getQuestions() {
		return questions;
	}

	public String getName() {
		return name;
	}

	public int getTimeToAnswer() {
		return timeToAnswer;
	}
	
	public int getGameStatus() {
		return gameStatus;
	}

	public int getTimeBeforeStart() {
		return timeBeforeStart;
	}	
}
