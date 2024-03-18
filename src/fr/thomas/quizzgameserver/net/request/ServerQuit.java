package fr.thomas.quizzgameserver.net.request;

public class ServerQuit {
	
	public static class ServerQuitRequest {
		public int gameID;
		public int playerID;
	}
	
	public static class ServerQuitResponse {
		public boolean hasQuit;
	}

}
