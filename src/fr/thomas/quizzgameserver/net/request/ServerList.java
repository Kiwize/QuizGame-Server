package fr.thomas.quizzgameserver.net.request;

import java.util.ArrayList;

import fr.thomas.quizzgameserver.net.object.OnlineGameNetObject;

public class ServerList {
	
	public static class ServerListRequest {
		
	}
	
	public static class ServerListResponse {
		public ArrayList<OnlineGameNetObject> servers;
	}

}
