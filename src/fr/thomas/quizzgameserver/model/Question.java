package fr.thomas.quizzgameserver.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import fr.thomas.quizzgameserver.controller.GameController;

public class Question {

	private int id;
	private String label;
	private int difficultyLevel;
	private ArrayList<Answer> answers;
	private GameController controller;

	public Question(GameController controller, String label, ArrayList<Answer> answers) {
		this.label = label;
		this.answers = answers;
		this.controller = controller;
	}

	public Question(int id, GameController controller) {
		try {
			Statement st = controller.getDatabaseHelper().getStatement(0);
			ResultSet res = st.executeQuery("SELECT * FROM Question WHERE idquestion = " + id + ";");
			this.controller = controller;
			if (!res.next()) {
				this.id = -1;
				this.label = "";
				this.difficultyLevel = 0;
				this.answers = new ArrayList<Answer>();
			} else {
				this.id = res.getInt("idquestion");
				this.label = res.getString("label");
				this.difficultyLevel = res.getInt("difficultyLevel");
				this.answers = new ArrayList<>();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Answer> getAnswers() {
		return answers;
	}
	
	public String getAnAnswer(int id) {
		return answers.get(id).getLabel();
	}

	public String getLabel() {
		return label;
	}

	public int getDifficulty() {
		return difficultyLevel;
	}
}
