package fr.thomas.quizzgameserver.net.object;

public class PlayerNetObject {

	private int id;
	private String name;
	private String password;
	private int highestScore;
	
	public PlayerNetObject() {
	}
	
	public PlayerNetObject(int id, String username, String password, int highestScore) {
		this.id = id;
		this.name = username;
		this.password = password;
		this.highestScore = highestScore;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
	}
	
	public int getHighestScore() {
		return highestScore;
	}
	
}
