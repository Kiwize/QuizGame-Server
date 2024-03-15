package fr.thomas.quizzgameserver.model;

public class Answer {
	
	private String label;
	private boolean isCorrect;
	private char qchar;
	
	public Answer(char qchar, String label, boolean isCorrect) {
		this.label = label;
		this.isCorrect = isCorrect;
		this.qchar = qchar;
	}
	
	public String getLabel() {
		return label;
	}
	
	public boolean isCorrect() {
		return isCorrect;
	}
	
	public char getQchar() {
		return qchar;
	}
}
