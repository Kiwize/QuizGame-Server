package fr.thomas.quizzgameserver.controller;

import java.io.IOException;
import java.sql.SQLException;
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
import fr.thomas.quizzgameserver.model.Player;
import fr.thomas.quizzgameserver.model.Question;
import fr.thomas.quizzgameserver.net.Login;
import fr.thomas.quizzgameserver.utils.BCrypt;
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
	
		// Créer la vue
		this.myConfig = new Config();
		
		try {
			this.databaseHelper = new DatabaseHelper(this);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		
		player = new Player("", this);
		
		server.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if(object instanceof Login.LoginRequest) {
					Login.LoginRequest login_request = (Login.LoginRequest) object;
					Login.LoginResponse login_response = new Login.LoginResponse();
					
					if(player.authenticate(login_request.username, login_request.password)) {
						login_response.isConnected = true;
					} else {
						login_response.isConnected = false;
					}
					
					server.sendToTCP(connection.getID(), login_response);
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
		
		//view.showGameRecap(gameHistory);
		
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
