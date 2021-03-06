package de.speedcube.ocsServer.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.speedcube.ocsUtilities.Config;
import de.speedcube.ocsUtilities.UserInfo;
import de.speedcube.ocsUtilities.Userranks;
import de.speedcube.ocsUtilities.security.RandomString;
import de.speedcube.ocsUtilities.security.Sha2;

public class OCSDatabase {

	//private ComboPooledDataSource dataSource;
	private Connection connection;
	private static final String PREFIX = "ocs_";
	private static final int TIMEOUT = 3000;
	private static final String[] requiredTables = new String[1];
	public String host;
	public String user;
	public String password;
	public String database;
	public int port;

	public OCSDatabase(String host, String user, String password, String database, int port) throws SQLException {
		this.host = host;
		this.user = user;
		this.password = password;
		this.database = database;
		this.port = port;

		connect();
		/*dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("com.mysql.jdbc.Driver");
		dataSource.setJdbcUrl("jdbc:mysql://"+host+":"+port+"/"+database);
		dataSource.setUser(user);
		dataSource.setPassword(password); 
		dataSource.setCheckoutTimeout(TIMEOUT);*/
	}
	
	private void connect() throws SQLException {
		connection = DriverManager.getConnection("jdbc:mysql://"+host+"/"+database, user, password);
	}

	public OCSDatabase(String host, String user, String password, String database) throws SQLException {
		this(host, user, password, database, 3306);
	}
	
	private Connection getConnection() throws SQLException {
		if (!connection.isValid(TIMEOUT)) {
			connection.close();
			connect();
		}
		return connection;
	}

	public boolean checkAllTables() throws SQLException {
		boolean isOk = true;

		for (String table : requiredTables) {
			PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?;");
			ps.setString(1, database);
			ps.setString(2, PREFIX + table);
			ResultSet result = ps.executeQuery();
			result.next();
			if (result.getInt(1) != 1) {
				System.out.println("Missing Database table " + table);
				isOk = false;
			}
		}

		return isOk;
	}

	public UserInfo getUserInfo(String username, String password) throws SQLException {
		PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM " + PREFIX + "users WHERE username = ? LIMIT 1");

		ps.setString(1, username);
		ResultSet result = ps.executeQuery();

		if (!result.next()) return null; // No valid user authentification

		String salt = result.getString("salt");
		String hashed_password = Sha2.hashPassword(password, salt);
		String original_password = result.getString("password");

		// The mighty login password equality check
		if (!hashed_password.equals(original_password)) return null;

		return new UserInfo(result.getInt("id"), result.getString("username"), result.getInt("rank"), result.getInt("color"), result.getString("status"));
	}

	public boolean userExists(String username) throws SQLException {
		PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM " + PREFIX + "users WHERE username = ?");
		ps.setString(1, username);
		ResultSet result = ps.executeQuery();
		result.next();
		return result.getInt(1) > 0;
	}

	public String getTransmissionSalt(String username) throws SQLException {
		PreparedStatement ps = getConnection().prepareStatement("SELECT transmission_salt FROM " + PREFIX + "users WHERE username = ? LIMIT 1");
		ps.setString(1, username);
		ResultSet result = ps.executeQuery();
		if (!result.next()) return null;
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

	public void register(String username, String password, String transmission_salt) throws SQLException {
		String salt = RandomString.getNew(Config.SALT_LENGTH);
		password = Sha2.hashPassword(password, salt);
		PreparedStatement ps = getConnection().prepareStatement("INSERT INTO "+PREFIX+"users (username, password, salt, transmission_salt, rank) VALUES (?, ?, ?, ?, "+Userranks.NORMAL+")");
		ps.setString(1, username);
		ps.setString(2, password);
		ps.setString(3, salt);
		ps.setString(4, transmission_salt);
		ps.execute();
	}
	
	public void updateUserInfo(UserInfo userInfo) throws SQLException {
		PreparedStatement ps = getConnection().prepareStatement("UPDATE "+PREFIX+"users SET username = ?, rank = ?, color = ?, status = ? WHERE id = ? LIMIT 1");
		ps.setString(1, userInfo.username);
		ps.setInt(2, userInfo.rank);
		ps.setInt(3, userInfo.color);
		ps.setString(4, userInfo.status);
		ps.setInt(5, userInfo.userID);
		ps.execute();
	}

	public void closeConnection() {
		try {
			getConnection().close();
			//dataSource.close();
		} catch (SQLException e) {
			System.out.println("Could not close database.");
			e.printStackTrace();
		}
	}

}
