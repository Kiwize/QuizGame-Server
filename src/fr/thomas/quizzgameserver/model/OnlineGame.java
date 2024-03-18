package fr.thomas.quizzgameserver.model;

import fr.thomas.quizzgameserver.controller.GameController;

public class OnlineGame {

	private int id;
	private String name;
	private int maxPlayer;
	private int minPlayer;
	private int timeToAnswer;
	private int gameStatus;
	
	private GameController controller;
	
	public OnlineGame(GameController controller) {
		this.controller = controller;
	}

	public OnlineGame(GameController controller, int id, String name, int maxPlayer, int minPlayer, int timeToAnswer, int gameStatus) {
		this.controller = controller;
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
