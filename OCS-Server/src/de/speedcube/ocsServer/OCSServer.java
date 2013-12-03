package de.speedcube.ocsServer;

import java.sql.SQLException;
import java.util.ArrayList;

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
	public OCSDatabase database = null;

	public OCSServer() {

	}

	public static void main(String[] args) {

		OCSServer server = new OCSServer();
		server.start();

		System.out.println("Terminating!");

	}

	private void start() {

		//String salt1 = RandomString.getNew(20); System.out.println(salt1);
		//String salt2 = RandomString.getNew(20); System.out.println(salt2);
		//System.out.println(Sha2.hashPassword(Sha2.hashPassword("14", salt1), salt2));
		//System.out.println(Sha2.hashPassword(Sha2.hashPassword("42", "UkyKiTw;Hje.;@kNwKPL"), "$xccyy^>ef21an-5fUF="));
		
		running = true;

		serverThread = new ServerThread(34543, packageReceiveNotify);
		updateServerThread = new UpdateServerThread();

		try {
			database = new OCSDatabase("localhost", "Felk", "ruamzuzla", "jocs");
			if (database.checkAllTables()) {
				System.out.println("Alle Datenbanktabellen vorhanden!");
			} else {
				running = false;
				System.out.println("ERROR: Nicht alle Datenbanktabellen vorhanden!");
			}
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

				System.out.println("Packet(s) verfügbar");

				ArrayList<Packet> packets = new ArrayList<Packet>();
				for (Client c : serverThread.getClients()) {
					for (Packet p : c.getData(Packet.DEFAULT_CHANNEL))
						packets.add(p);
					for (Packet p : c.getData(Packet.CHAT_CHANNEL))
						packets.add(p);
					for (Packet p : packets) {
						System.out.println("handling packet");
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
