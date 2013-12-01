package de.speedcube.ocsServer.network;

import java.net.*;
import java.util.ArrayList;

import de.speedcube.ocsServer.ClientInformation;
import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketConnectionInfo;


public class Client {
	private Socket socket;
	public boolean connected = false;
	SendThread sender;
	ReceiveThread receiver;

	public static final int CLIENT = 0;
	public static final int SERVER_CLIENT = 1;
	public int clientType = CLIENT;
	private ServerThread server;
	public PacketConnectionInfo connectionInfo;
	public boolean connectionInfoReceived = false;
	public boolean connectionInfoSent = false;
	private Object receiveNotify;
	public ClientInformation clientInformation = null;
	public String username;
	
	public String closeMessage = "";

	public Client(Socket socket, ServerThread server, Object receiveNotify) {
		this.socket = socket;
		this.server = server;
		this.receiveNotify = receiveNotify;
		connected = true;
		clientType = SERVER_CLIENT;
		init();
	}

	public Client(String adress, int port) {
		try {
			socket = new Socket(adress, port);
			connected = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		init();
	}

	public void init() {
		sender = new SendThread(socket);
		receiver = new ReceiveThread(socket, this, receiveNotify);

		sender.start();
		receiver.start();
	}

	public void setConnectionInfo(PacketConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
		connectionInfoReceived = true;

		if (!connectionInfo.version.equals(OCSServer.version)) {
			stopClient();
			closeMessage = "incorrect version number";
		} else if (!connectionInfoSent) {
			connectionInfoSent = true;
			sendPacket(new PacketConnectionInfo());
		}
	}

	public void sendPacket(Packet packet) {
		sender.sendPacket(packet);
	}

	public ArrayList<Packet> getData(int channel) {
		return receiver.getData(channel);
	}

	public String getAdress() {
		if (socket != null) { return socket.getInetAddress().getHostAddress() + ":" + socket.getPort(); }
		return "";
	}

	public void stopClient() {
		connected = false;

		receiver.stopThread();

		sender.stopThread();
		if (clientType == SERVER_CLIENT) {
			server.removeClient(this);
		}
	}
	
	public boolean isAuthorized() {
		return (clientInformation != null);
	}
}
