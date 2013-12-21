package de.speedcube.ocsServer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import de.speedcube.ocsServer.autoUpdater.UpdateServerThread;
import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsServer.network.ServerThread;
import de.speedcube.ocsServer.parties.PartyContainer;
import de.speedcube.ocsServer.sql.OCSDatabase;
import de.speedcube.ocsServer.userlistServer.UserlistServerThread;
import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketDisconnect;

public class OCSServer {

	public static final String version = "0";
	public boolean running;
	
	public ServerThread serverThread;
	public UpdateServerThread updateServerThread;
	public UserlistServerThread userlistServerThread;
	
	public PartyContainer parties;
	
	public static Object packageReceiveNotify = new Object();
	public OCSDatabase database;
	public Userlist userlist;
	private static boolean restart = false;

	public OCSServer() {
		userlist = new Userlist();
		parties = new PartyContainer(userlist);
		/*User u = new User(null);
		User u2 = new User(null);
		u.userInfo.username = "test";
		u.userInfo.userID = 1;
		u2.userInfo.username = "test2";
		u2.userInfo.userID = 2;
		Party party = new Party(4, 2, userlist);
		party.addUser(u);
		party.addUser(u2);
		party.start();
		party.update();
		party.setTime(u, 30);
		party.setTime(u2, 30);
		party.update();
		party.setTime(u, 10);
		party.setTime(u2, 10);
		party.update();
		party.setTime(u, 13);
		party.setTime(u2, 14);
		party.update();
		party.setTime(u, 17);
		party.setTime(u2, 18);
		party.update();
		if (party.isOver()) {
			System.out.println("Party ist vorbei");
			System.out.println(party.getDisplay());
		}*/
	}

	public static void main(String[] args) {
		
		OCSServer server;
		do {
			server = new OCSServer();
			System.gc();
			server.start();
		} while (restart);

		System.out.println("Terminating!");
	}

	public void stop(String msg) {
		System.out.println("Stopping Server!");
		PacketDisconnect packet = new PacketDisconnect();
		packet.msg = msg;
		serverThread.broadcastData(packet);
		serverThread.stopServer();
		running = false;
	}

	public void restart(String msg) {
		stop(msg);
		restart = true;
	}

	private void start() {

		System.out.println("Started Server");

		running = true;
		restart = false;

		serverThread = new ServerThread(34543, packageReceiveNotify);
		updateServerThread = new UpdateServerThread();
		userlistServerThread = new UserlistServerThread(userlist);

		try {

			// Load Database connection info
			Properties properties = new Properties();
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream("dbconnection.properties"));
			properties.load(stream);
			stream.close();

			// Try database connection
			System.out.println("trying to load database...");
			database = new OCSDatabase(properties.getProperty("host"), properties.getProperty("username"), properties.getProperty("password"), properties.getProperty("database"));

			if (database.checkAllTables()) {
				System.out.println("Alle Datenbanktabellen vorhanden!");
			} else {
				running = false;
				System.out.println("ERROR: Nicht alle Datenbanktabellen vorhanden!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not make a database connection! Aborting programm.");
			running = false;
		} catch (IOException e) {
			System.out.println("Could not read connection properties file");
			e.printStackTrace();
		}
		
		while (running) {

			try {
				synchronized (packageReceiveNotify) {
					packageReceiveNotify.wait();
				}

				ArrayList<Packet> packets = new ArrayList<Packet>();
				for (int i = 0; i < serverThread.getClients().size(); i++) {
					Client c = serverThread.getClients().get(i);
					for (Packet p : c.getData(Packet.DEFAULT_CHANNEL))
						packets.add(p);
					for (Packet p : c.getData(Packet.CHAT_CHANNEL))
						packets.add(p);
					for (Packet p : packets) {
						PacketHandler.handlePackage(this, c, p);
					}
					packets.clear();
				}
				
			} catch (InterruptedException e) {
				running = false;
				e.printStackTrace();
			}

		}

		serverThread.stopServer();
		updateServerThread.stopServer();
		userlistServerThread.stopServer();
		database.closeConnection();

	}

}
