package fr.thomas.quizzgameserver.net.request;

import java.util.ArrayList;

public class ServerInfo {
	
	public static class ServerInfoRequest {
		public int gameID;
	}
	
	public static class ServerInfoResponse {
		public String name;
		public int onlinePlayer;
		public int gameStatus;
		public int maxPlayers;
		public int minPlayers;
		public ArrayList<Integer> players;
	}
	
	public static class ServerCountDown {
		public int time;
	}

}
