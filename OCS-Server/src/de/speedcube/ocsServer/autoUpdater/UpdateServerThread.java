package de.speedcube.ocsServer.autoUpdater;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class UpdateServerThread extends Thread {

	private ServerSocket serverSocket;
	private ArrayList<UpdateClient> clients;

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
		boolean running = true;
		while (running) {
			try {
				clients.add(new UpdateClient(serverSocket.accept()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
