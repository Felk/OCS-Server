package de.speedcube.ocsServer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import de.speedcube.ocsServer.autoUpdater.UpdateServerThread;
import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsServer.network.ReceiveThread;
import de.speedcube.ocsServer.network.ServerThread;
import de.speedcube.ocsServer.sql.OCSDatabase;
import de.speedcube.ocsUtilities.packets.Packet;

public class OCSServer {

	public static final String version = "0";
	public boolean running;
	public ServerThread serverThread;
	public UpdateServerThread updateServerThread;
	public ReceiveThread receiveThread;
	public static Object packageReceiveNotify = new Object();
	public OCSDatabase database;
	public Userlist userlist;

	public OCSServer() {
		userlist = new Userlist();
	}

	public static void main(String[] args) {

		OCSServer server = new OCSServer();
		server.start();

		System.out.println("Terminating!");

	}

	private void start() {

		//System.out.println(Sha2.hashPassword(Sha2.hashPassword("42", "UkyKiTw;Hje.;@kNwKPL"), "$xccyy^>ef21an-5fUF="));
		
		running = true;

		serverThread = new ServerThread(34543, packageReceiveNotify);
		updateServerThread = new UpdateServerThread();

		try {
			
			// Load Database connection info
			Properties properties = new Properties();
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream("dbconnection.properties"));
			properties.load(stream);
			stream.close();
			
			// Try database connection
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

		System.out.println("Starting Mainloop:.");

		while (running) {

			try {
				synchronized (packageReceiveNotify) {
					packageReceiveNotify.wait();
				}

				System.out.println("Packet(s) verfügbar");

				ArrayList<Packet> packets = new ArrayList<Packet>();
				for (int i = 0; i < serverThread.getClients().size(); i++) {
					Client c = serverThread.getClients().get(i);
					for (Packet p : c.getData(Packet.DEFAULT_CHANNEL))
						packets.add(p);
					for (Packet p : c.getData(Packet.CHAT_CHANNEL))
						packets.add(p);
					for (Packet p : packets) {
						System.out.println("handling packet: "+p.getName());
						PacketHandler.handlePackage(this, c, p);
					}
					packets.clear();
				}

				System.out.println("Packet verarbeitet!");
			} catch (InterruptedException e) {
				running = false;
				e.printStackTrace();
			}

		}

		System.out.println("Stopped mainloop");
		serverThread.stopServer();
		updateServerThread.stopServer();

	}

}
