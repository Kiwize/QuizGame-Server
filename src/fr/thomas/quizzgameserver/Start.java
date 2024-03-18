package fr.thomas.quizzgameserver;

import fr.thomas.quizzgameserver.controller.GameController;

public class Start {
	
	private GameController game;

	public static void main(String[] args) {
		new Start().init();
	}
	
	public void init() {
		System.out.println("Starting VINCI SpriteBot server...");
		game = new GameController();
	}

}
