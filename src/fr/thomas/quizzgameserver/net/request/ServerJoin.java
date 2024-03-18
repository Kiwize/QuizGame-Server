package fr.thomas.quizzgameserver.net.request;

import fr.thomas.quizzgameserver.net.object.OnlineGameNetObject;
import fr.thomas.quizzgameserver.net.object.PlayerNetObject;

public class ServerJoin {
	
	public static class ServerJoinRequest {
		public OnlineGameNetObject game;
		public PlayerNetObject player;
	}
	
	public static class ServerJoinResponse {
		public boolean isJoinable;
	}

}
