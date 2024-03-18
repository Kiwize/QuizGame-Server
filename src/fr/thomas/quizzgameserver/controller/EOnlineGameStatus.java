package fr.thomas.quizzgameserver.controller;

public enum EOnlineGameStatus {
	
	CREATED(0),
	STARTED(1),
	FINISHED(2);
	
	private int statusID;
	
	private EOnlineGameStatus(int statusID) {
		this.statusID = statusID;
	}
	
	public int getStatusID() {
		return statusID;
	}

}
