package fr.thomas.quizzgameserver.model;

import fr.thomas.quizzgameserver.net.object.AnswerNetObject;

public class Answer {
	
	private String label;
	private boolean isCorrect;
	
	public Answer(AnswerNetObject netObject) {
		this.label = netObject.getLabel();
		this.isCorrect = netObject.isCorrect();
	}
	
	public Answer(String label, boolean isCorrect) {
		this.label = label;
		this.isCorrect = isCorrect;
	}
	
	public String getLabel() {
		return label;
	}
	
	public boolean isCorrect() {
		return isCorrect;
	}
}
