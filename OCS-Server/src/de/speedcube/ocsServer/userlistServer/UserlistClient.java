package de.speedcube.ocsServer.userlistServer;

import java.io.*;
import java.net.Socket;

import de.speedcube.ocsServer.Userlist;

public class UserlistClient extends Thread {
	DataInputStream in;
	BufferedOutputStream out;
	Socket socket;
	private Userlist userlist;

	public UserlistClient(Socket socket, Userlist userlist) {
		this.socket = socket;
		this.userlist = userlist;
		this.start();
	}

	@Override
	public void run() {

		try {
			in = new DataInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());

			String json = userlist.getJsonString()+"\n";

			out.write(json.getBytes("utf-8"));

			out.flush();

			stopClient();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendInt(BufferedOutputStream out, int i) {
		try {
			out.write((i & 0xff000000) >>> 24);
			out.write((i & 0xff0000) >>> 16);
			out.write((i & 0xff00) >>> 8);
			out.write(i & 0xff);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopClient() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not close socket for UserlistClient.");
		}
	}
}
