package fr.thomas.quizzgameserver.controller.threading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import fr.thomas.quizzgameserver.controller.GameController;
import fr.thomas.quizzgameserver.model.Answer;
import fr.thomas.quizzgameserver.model.OnlineGame;
import fr.thomas.quizzgameserver.model.Question;
import fr.thomas.quizzgameserver.net.object.AnswerNetObject;
import fr.thomas.quizzgameserver.net.object.QuestionNetObject;
import fr.thomas.quizzgameserver.net.request.ServerInfo.ServerCountDown;
import fr.thomas.quizzgameserver.net.request.ServerPlay.AnswerTimeLeft;
import fr.thomas.quizzgameserver.net.request.ServerPlay.ServerEndGame;
import fr.thomas.quizzgameserver.net.request.ServerPlay.ShowQuestion;

public class GameThread implements Runnable {

	private OnlineGame game;
	private GameController controller;

	private int countDown;

	private Timer gameStartTimer;
	private TimerTask task;

	private int questionCountdown;

	private Timer questionTimer;
	private TimerTask questionTimerTask;

	private ServerCountDown updateTimeRequest;

	private boolean isStarted;
	private boolean readyNextQuestion;

	private HashMap<Question, HashMap<Integer, Answer>> gameHistory;
	private HashMap<Integer, Integer> playerScores;
	private Question currentQuestion;

	public GameThread(GameController controller, OnlineGame game) {
		this.game = game;
		this.isStarted = false;
		this.readyNextQuestion = false;
		this.game.defineController(controller);
		this.game.getRandomQuestions();
		this.gameHistory = new HashMap<Question, HashMap<Integer, Answer>>();
		this.playerScores = new HashMap<Integer, Integer>();

		for (Question question : game.getQuestions()) {
			for (int playerID : game.getPlayers()) {
				HashMap<Integer, Answer> tmpMap = new HashMap<>();
				tmpMap.put(playerID, null);
				playerScores.put(playerID, 0);
				gameHistory.put(question, tmpMap);
			}
		}

		this.questionCountdown = game.getTimeToAnswer();
		this.questionTimer = new Timer("QuestionTimer-" + game.getName().replace(" ", "_"));
		this.gameStartTimer = new Timer("GameStartTimer-" + game.getName().replace(" ", "_"));
		this.controller = controller;
		this.updateTimeRequest = new ServerCountDown();

		this.task = new TimerTask() {

			@Override
			public void run() {
				// Check if current game meet player requirement
				if (countDown == 0 && game.getPlayers().size() >= game.getMinPlayer()
						&& game.getPlayers().size() <= game.getMaxPlayer()) {
					isStarted = true;
					System.out.println("Game is ready to start !");
					cancel();
				}

				// Send countdown message to evry players
				updateTimeRequest.time = countDown;
				controller.sendTCPTo(game.getPlayers(), updateTimeRequest); // Update time for each players waiting in
																			// the game.
				countDown--;
			}
		};

		this.questionTimerTask = new TimerTask() {

			@Override
			public void run() {
				if (questionCountdown == 0) {
					readyNextQuestion = true;
				} else {
					questionCountdown--;
				}
			}
		};
	}

	public void start() {
		countDown = game.getTimeBeforeStart();
		gameStartTimer.schedule(task, countDown, 1000);

		// Send start message to every players.
	}

	public void abortStart() {
		gameStartTimer.cancel();
		gameStartTimer.purge();
		isStarted = false;
	}

	public void end() {
		ServerEndGame serverEndRequest = new ServerEndGame();
		serverEndRequest.score = 0;
		controller.sendTCPTo(game.getPlayers(), serverEndRequest);
	}

	public void givePlayerAnswer(int playerID, Answer answer) {
		gameHistory.get(this.currentQuestion).put(playerID, answer);
	}

	@Override
	public void run() {
		try {
			start();

			while (!isStarted) {
				if (game.getPlayers().size() == 0) {
					questionTimer.cancel();
					gameStartTimer.cancel();
					return;
				}
				Thread.sleep(1000);
			}

			// When process starts, start asking question on by one to the players
			// Collect each player's answers and store them during the game
			questionTimer.schedule(questionTimerTask, questionCountdown, 1000);

			ArrayList<AnswerNetObject> answers = new ArrayList<AnswerNetObject>();
			AnswerTimeLeft answerTimeLeftRequest = new AnswerTimeLeft();
			ShowQuestion showQuestionRequest = new ShowQuestion();
			ServerEndGame endGameRequest = new ServerEndGame();

			for (Question question : this.game.getQuestions()) {
				answers.clear();
				this.currentQuestion = question;
				for (Answer questionAnswer : question.getAnswers()) {
					answers.add(new AnswerNetObject(questionAnswer.getLabel(), questionAnswer.isCorrect()));
				}

				QuestionNetObject netQuestion = new QuestionNetObject(question.getId(), question.getLabel(),
						question.getDifficulty(), answers);
				showQuestionRequest.question = netQuestion;
				controller.sendTCPTo(game.getPlayers(), showQuestionRequest);

				while (!readyNextQuestion) {
					Thread.sleep(1000);
					answerTimeLeftRequest.timeLeftToAnswer = questionCountdown;
					answerTimeLeftRequest.maxTime = game.getTimeToAnswer();
					controller.sendTCPTo(game.getPlayers(), answerTimeLeftRequest);
				}

				readyNextQuestion = false;
				questionCountdown = game.getTimeToAnswer();
			}

			for (Map.Entry<Question, HashMap<Integer, Answer>> entry : gameHistory.entrySet()) {
				System.out.println("Question : " + entry.getKey().getLabel() + " : ");
				for (Map.Entry<Integer, Answer> playerAnswersEntries : entry.getValue().entrySet()) {
					try {
						System.out.println("Player " + playerAnswersEntries.getKey() + " answered "
								+ playerAnswersEntries.getValue().getLabel());
						int currentPlayerScore = this.playerScores.get(playerAnswersEntries.getKey());
						if (playerAnswersEntries.getValue().isCorrect())
							this.playerScores.put(playerAnswersEntries.getKey(), currentPlayerScore + 100);
					} catch (NullPointerException e) {
						System.out.println("Player " + playerAnswersEntries.getKey() + " answered nothing...");
					}
				}
			}
			
			for(int playerID : game.getPlayers()) 

			questionTimer.cancel();
			questionTimer.purge();

			gameStartTimer.purge();
			end();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
