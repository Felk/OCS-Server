package de.speedcube.ocsServer;

import java.sql.SQLException;
import java.util.ArrayList;

import de.speedcube.ocsServer.autoUpdater.UpdateServerThread;
import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsServer.network.Packet;
import de.speedcube.ocsServer.network.ReceiveThread;
import de.speedcube.ocsServer.network.ServerThread;
import de.speedcube.ocsServer.sql.OCSDatabase;

public class OCSServer {

	public static final String version = "0";
	public boolean running;
	public ServerThread serverThread;
	public UpdateServerThread updateServerThread;
	public ReceiveThread receiveThread;
	public static Object packageReceiveNotify = new Object();
	public OCSDatabase database = null;

	public OCSServer() {

	}

	public static void main(String[] args) {

		System.out.println("Everything works so far!");
		OCSServer server = new OCSServer();
		server.start();

		System.out.println("Terminating!");
		
	}

	private void start() {

		running = true;
		
		serverThread = new ServerThread(34543, packageReceiveNotify);
		updateServerThread = new UpdateServerThread();

		try {
			database = new OCSDatabase("localhost", "Felk", "ruamzuzla", "jocs");
		} catch (SQLException e1) {
			e1.printStackTrace();
			System.out.println("Could not make a database connection! Aborting programm.");
			running = false;
		}

		System.out.println("Starting Mainloop:.");
		
		while (running) {

			try {
				synchronized (packageReceiveNotify) {
					packageReceiveNotify.wait();
				}
				
				ArrayList<Packet> packets = new ArrayList<Packet>();
				for (Client c : serverThread.getClients()) {
					packets.addAll(c.getData(Packet.DEFAULT_CHANNEL));
					packets.addAll(c.getData(Packet.CHAT_CHANNEL));
				}
				
				
				
				System.out.println("Package verfügbar!");
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
