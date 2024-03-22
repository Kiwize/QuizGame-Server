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

public class Game implements IModel {

	private int id;
	private int score;
	private Player player;

	private Random rand;
	private GameController controller;

	public Game(GameController controller, Player player) {
		this.rand = new Random();
		this.player = player;
		this.controller = controller;
		this.score = 0;
	}

	/**
	 * Débute la partie, pose toutes les questions une par une.
	 * 
	 * @author Thomas PRADEAU
	 */
	public void begin() {
	}

	public void addScore(int amount) {
		score += amount;
	}
	
	public void setScore(int amount) {
		score = amount;
	}

	/**
	 * Choisis des questions aléatoires parmis toutes les questions.
	 * 
	 * @param amount Nombre de questions à choisir
	 * @author Thomas PRADEAU
	 */
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

			//controller.setQuestions(cq);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean insert() {
		try {
			Statement st = controller.getDatabaseHelper().getStatement(0);

			st.executeUpdate("INSERT INTO Game (score, idplayer) VALUES ('" + score + "', " + player.getID() + ");",
					Statement.RETURN_GENERATED_KEYS);

			ResultSet res = st.getGeneratedKeys();

			if (res.next()) {
				this.id = res.getInt(1);
			}

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean save() {
		try {
			Statement st = controller.getDatabaseHelper().getStatement(0);

			boolean res = st.execute("UPDATE Game SET score = " + score + ", idplayer=" + player.getID()
					+ " WHERE Game.idgame=" + id + ";");

			return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	
}
