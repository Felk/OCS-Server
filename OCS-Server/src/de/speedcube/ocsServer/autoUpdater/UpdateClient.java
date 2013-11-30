package de.speedcube.ocsServer.autoUpdater;

import java.io.*;
import java.net.Socket;

public class UpdateClient extends Thread {
	DataInputStream in;
	BufferedOutputStream out;
	Socket socket;

	public UpdateClient(Socket socket) {
		this.socket = socket;
		this.start();
	}

	@Override
	public void run() {

		String serverVersion = getServerVersion();

		try {
			in = new DataInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());

			String clientVersion = in.readUTF();

			if (!serverVersion.equals(clientVersion)) {
				System.out.println("updating client");
				byte[] clientFile = loadClientFile();
				out.write(1);

				sendInt(out, serverVersion.length());
				out.write(serverVersion.getBytes());

				sendInt(out, clientFile.length);
				System.out.println("size: " + clientFile.length);
				System.out.println("1");
				out.write(clientFile);
				System.out.println("2");

				out.flush();
			} else {
				System.out.println("client up to date");
				out.write(0);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getServerVersion() {
		String fileName = "ServerFiles" + File.separator + "version.txt";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String version = br.readLine();
			br.close();
			return version;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "null";
		} catch (IOException e) {
			e.printStackTrace();
			return "null";
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

	public byte[] loadClientFile() {
		try {
			File f = new File("ServerFiles" + File.separator + "ocsClient.jar");
			int fileSize = (int) f.length();
			InputStream is;

			is = new FileInputStream(f);
			byte[] data = new byte[fileSize];
			is.read(data);
			is.close();

			return data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	
	public void stopClient() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not close socket for UpdateClient.");
		}
	}
}
