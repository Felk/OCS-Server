package de.speedcube.ocsServer.userlistServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import de.speedcube.ocsServer.Userlist;

public class UserlistServerThread extends Thread {

	private ServerSocket serverSocket;
	private ArrayList<UserlistClient> clients;
	private boolean running;
	private Userlist userlist;

	public UserlistServerThread(Userlist userlist) {
		this.userlist = userlist;
		try {
			serverSocket = new ServerSocket(34743);
			clients = new ArrayList<UserlistClient>();
		} catch (IOException e) {
			System.out.println("Failed to start userlist server");
		}
		this.start();
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				clients.add(new UserlistClient(serverSocket.accept(), userlist));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopServer() {
		synchronized (clients) {
			for (UserlistClient c : clients) {
				c.stopClient();
			}
		}
		running = false;

		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not stop UserlistServerThread");
		}
	}
}
