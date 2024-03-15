package fr.thomas.quizzgameserver.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import fr.thomas.quizzgameserver.controller.GameController;

public class DatabaseHelper {

	// TODO Move to env file
	private String bdname = "";
	private String url = "";
	private String username = "";
	private String password = "";
	private GameController myController;

	private Connection con;

	private final int ACTIVE_STATEMENT_COUNT = 2;
	private ArrayList<Statement> activeStatements;

	public DatabaseHelper(GameController controller) throws ClassNotFoundException, SQLException {
		
		myController = controller;
		
		bdname = myController.getMyConfig().readAParam("db.name");
		url = myController.getMyConfig().readAParam("db.url");
		username = myController.getMyConfig().readAParam("db.username");
		password = myController.getMyConfig().decryptAParam("db.password");
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		this.url += this.bdname;
		this.con = DriverManager.getConnection(url, username, password);

		activeStatements = new ArrayList<Statement>();
		for (int i = 0; i < ACTIVE_STATEMENT_COUNT; i++) {
			activeStatements.add(con.createStatement());
		}
	}

	public Statement create() {
		try {
			return con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Connection getCon() {
		return con;
	}
	
	public Statement getStatement(int id) {
		if(id <= ACTIVE_STATEMENT_COUNT - 1 && id >= 0) {
			return activeStatements.get(id);
		}
		
		throw new IllegalArgumentException("Invalid statement ID !");
	}

	public void close() {
		try {
			this.con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
