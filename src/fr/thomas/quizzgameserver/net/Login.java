package fr.thomas.quizzgameserver.net;

public class Login {
	
	public static class LoginRequest {
		public String username;
		public String password;
	}
	
	public static class LoginResponse {
		public boolean isConnected;
	}

}
