package de.speedcube.ocsServer.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OCSDatabase {

	public Connection connection;

	public OCSDatabase(String host, String user, String password, String database, int port) throws SQLException {
		connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
	}

	public OCSDatabase(String host, String user, String password, String database) throws SQLException {
		this(host, user, password, database, 3306);
	}

	static {
		try {
			// Loading the Connector/J Driver
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR: Could not load Connector/J Driver for Database Connection");
		}
	}

}
