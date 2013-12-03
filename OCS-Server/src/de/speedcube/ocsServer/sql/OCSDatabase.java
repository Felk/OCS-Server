package de.speedcube.ocsServer.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.speedcube.ocsServer.ClientInformation;
import de.speedcube.ocsUtilities.security.Sha2;

public class OCSDatabase {

	public Connection connection;
	private static final String PREFIX = "ocs_";
	private static final String[] requiredTables = new String[1];
	public String host;
	public String user;
	public String database;
	public int port;

	public OCSDatabase(String host, String user, String password, String database, int port) throws SQLException {
		this.host = host;
		this.user = user;
		this.database = database;
		this.port = port;
		connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
	}

	public OCSDatabase(String host, String user, String password, String database) throws SQLException {
		this(host, user, password, database, 3306);
	}

	public boolean checkAllTables() throws SQLException {
		boolean isOk = true;

		for (String table : requiredTables) {
			PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?;");
			ps.setString(1, database);
			ps.setString(2, PREFIX+table);
			ResultSet result = ps.executeQuery();
			result.next();
			if (result.getInt(1) != 1) {
				System.out.println("Missing Database table " + table);
				isOk = false;
			}
		}

		return isOk;
	}

	public ClientInformation getUser(String username, String password) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + PREFIX + "users WHERE username = ? LIMIT 1");
		System.out.println("USER: " + username);
		System.out.println("PASS: " + password);
		ps.setString(1, username);
		ResultSet result = ps.executeQuery();
		
		if (!result.next()) return new ClientInformation(); // No valid user authentification
		
		String salt = result.getString("salt");
		
		String hashed_password = Sha2.hashPassword(password, salt);
		
		String original_password = result.getString("password");
		
		System.out.println("HASHED: "+hashed_password);
		System.out.println("IN DB:  "+original_password);
		
		if (!hashed_password.equals(original_password)) return null;

		return new ClientInformation(result.getInt("id"), result.getString("username"), result.getInt("rank"));
	}
	
	public String getTransmissionSalt(String username) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("SELECT transmission_salt FROM "+PREFIX+"users WHERE username = ? LIMIT 1");
		ps.setString(1, username);
		ResultSet result = ps.executeQuery();
		if (!result.next()) return "";
		return result.getString(1);
	}

	static {
		try {
			// Loading the Connector/J Driver
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR: Could not load Connector/J Driver for Database Connection");
		}

		requiredTables[0] = "users";
	}

}
