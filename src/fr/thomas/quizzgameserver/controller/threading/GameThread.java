package fr.thomas.quizzgameserver.controller.threading;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import fr.thomas.quizzgameserver.controller.GameController;
import fr.thomas.quizzgameserver.model.Answer;
import fr.thomas.quizzgameserver.model.OnlineGame;
import fr.thomas.quizzgameserver.model.Player;
import fr.thomas.quizzgameserver.model.Question;
import fr.thomas.quizzgameserver.net.request.ServerInfo.ServerCountDown;

public class GameThread implements Runnable{
	
	private OnlineGame game;
	private GameController controller;
	
	private long gameStartTimeMillis;
	private int countDown = 20;
	private Timer gameStartTimer;
	private TimerTask task;
	
	private ServerCountDown updateTimeRequest;
	
	private boolean isStarted;
	private boolean shouldRun;
	
	private HashMap<Question, HashMap<Player, Answer>> gameHistory;
	
	public GameThread(GameController controller, OnlineGame game) {
		this.game = game;
		this.shouldRun = true;
		this.isStarted = false;
		this.game.defineController(controller);
		this.game.getRandomQuestions();
		this.gameStartTimer = new Timer();
		this.gameStartTimeMillis = System.currentTimeMillis();
		this.controller = controller;
		this.updateTimeRequest = new ServerCountDown();
		
		this.task = new TimerTask() {
			
			@Override
			public void run() {
				//Check if current game meet player requirement
				if(countDown == 0 && game.getPlayers().size() >= game.getMinPlayer() && game.getPlayers().size() <= game.getMaxPlayer()) {
					isStarted = true;
					System.out.println("Game is ready to start !");
					cancel();
				}
					
				
				//Send countdown message to evry players
				System.out.println("Countdown before start : " + countDown);
				updateTimeRequest.time = countDown;
				controller.sendTCPTo(game.getPlayers(), updateTimeRequest); //Update time for each players waiting in the game.
				countDown--;
			}
		};
	}
	
	
	public void start() {
		countDown = 20;
		gameStartTimer.schedule(task, countDown, 1000);
		
		//Send start message to every players.
		
		this.run();
	}
	
	public void abortStart() {
		System.out.println("Game have been cancelled !");
		gameStartTimer.cancel();
		gameStartTimer.purge();
		isStarted = false;
	}
	
	public void end() {
		
	}

	@Override
	public void run() {
		//When process starts, start asking question on by one to the players
		//Collect each player's answers and store them during the game
		
		/*
		while(shouldRun) {
			if(isStarted) {
				
				//Go though questions and follow players choices and behaviors
				
				
				
				
			}
		}
		*/
	}

}
