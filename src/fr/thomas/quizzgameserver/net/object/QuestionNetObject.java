package fr.thomas.quizzgameserver.net.object;

import java.util.ArrayList;

public class QuestionNetObject {

	private int id;
	private String label;
	private int difficultyLevel;
	private ArrayList<AnswerNetObject> answers;
	
	public QuestionNetObject(int id, String label, int diff, ArrayList<AnswerNetObject> answers) {
		this.id = id;
		this.label = label;
		this.difficultyLevel = diff;
		this.answers = answers;
	}
	
	public QuestionNetObject() {
	}
	
	public ArrayList<AnswerNetObject> getAnswers() {
		return answers;
	}
	
	public int getDifficultyLevel() {
		return difficultyLevel;
	}
	
	public int getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
}
