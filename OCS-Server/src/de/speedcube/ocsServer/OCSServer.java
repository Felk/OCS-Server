package de.speedcube.ocsServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.speedcube.ocsServer.autoUpdater.UpdateServerThread;
import de.speedcube.ocsServer.network.ReceiveThread;
import de.speedcube.ocsServer.network.ServerThread;

public class OCSServer {

	public static final String version = "0";
	public boolean running = true;
	public ServerThread serverThread;
	public ReceiveThread receiveThread;
	public static Object packageReceiveNotify = new Object();

	public OCSServer() {

	}

	public static void main(String[] args) {

		System.out.println("Everything works so far!");
		OCSServer server = new OCSServer();
		server.start();

	}

	private void start() {

		serverThread = new ServerThread(34543, packageReceiveNotify);
		UpdateServerThread updateServerThread = new UpdateServerThread();
		SQLtest();

		while (running) {

			try {
				packageReceiveNotify.wait();
				System.out.println("Package verfügbar!");
			} catch (InterruptedException e) {
				running = false;
				e.printStackTrace();
			}
			//System.out.println("ok");
			//serverThread.getClients().get(0).g

		}

		serverThread.stopServer();

	}

	public void SQLtest() {

		try {
			// Der Aufruf von newInstance() ist ein Workaround
			// für einige misslungene Java-Implementierungen

			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			// Fehler behandeln
		}

		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/jocs", "Felk", "ruamzuzla");
			con.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
