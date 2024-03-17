package fr.thomas.quizzgameserver.net;

import fr.thomas.quizzgameserver.net.object.PlayerNetObject;

public class Login {
	
	public static class LoginRequest {
		public String username;
		public String password;
	}
	
	public static class LoginResponse {
		public boolean isConnected;
		public PlayerNetObject player;
	}

}
