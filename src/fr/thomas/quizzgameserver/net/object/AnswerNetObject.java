package fr.thomas.quizzgameserver.net.object;

public class AnswerNetObject {

	private String label;
	private boolean isCorrect;
	
	public AnswerNetObject(String label, boolean iscorrect) {
		this.label = label;
		this.isCorrect = iscorrect;
	}
	
	public AnswerNetObject() {
	}
	
	public String getLabel() {
		return label;
	}
	
	public boolean isCorrect() {
		return isCorrect;
	}
}
