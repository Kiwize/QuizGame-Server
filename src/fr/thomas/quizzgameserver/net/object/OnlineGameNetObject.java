package fr.thomas.quizzgameserver.net.object;

public class OnlineGameNetObject {
	
	private int id;
	private String name;
	private int maxPlayer;
	private int minPlayer;
	private int timeToAnswer;
	private int gameStatus;
	
	public OnlineGameNetObject() {
	}
	
	public OnlineGameNetObject(int id, String name, int maxPlayer, int minPlayer, int timeToAnswer, int gameStatus) {
		this.id = id;
		this.name = name;
		this.maxPlayer = maxPlayer;
		this.minPlayer = minPlayer;
		this.timeToAnswer = timeToAnswer;
		this.gameStatus = gameStatus;
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
	
	public String getName() {
		return name;
	}
	
	public int getTimeToAnswer() {
		return timeToAnswer;
	}
	
	public int getGameStatus() {
		return gameStatus;
	}
}
