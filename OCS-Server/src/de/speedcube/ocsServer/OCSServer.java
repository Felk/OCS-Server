package de.speedcube.ocsServer;

import de.speedcube.ocsServer.network.ReceiveThread;
import de.speedcube.ocsServer.network.ServerThread;

public class OCSServer {

	public static final String version = "0";
	public boolean running = true;
	public ServerThread serverThread;
	public ReceiveThread receiveThread;
	
	public OCSServer() {
		
	}
	
	public static void main(String[] args) {
		
		System.out.println("Everything works so far!");
		OCSServer server = new OCSServer();
		server.start();
		
	}

	private void start() {
		
		serverThread = new ServerThread(34543);

		while(running) {
			
			//System.out.println("ok");
			//serverThread.getClients().get(0).g
			
		}
		
	}
	
}
