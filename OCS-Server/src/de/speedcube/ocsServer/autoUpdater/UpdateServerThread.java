package de.speedcube.ocsServer.autoUpdater;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class UpdateServerThread extends Thread {

	private ServerSocket serverSocket;
	private ArrayList<UpdateClient> clients;
	private boolean running;

	public UpdateServerThread() {
		try {
			serverSocket = new ServerSocket(34343);
			clients = new ArrayList<UpdateClient>();
		} catch (IOException e) {
			System.out.println("Failed to start update server");
		}
		this.start();
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				clients.add(new UpdateClient(serverSocket.accept()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopServer() {
		running = false;
		synchronized (clients) {
			while (clients.size() > 0) {
				clients.get(0).stopClient();
			}
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not stop UpdateServerThread");
		}
	}
}
