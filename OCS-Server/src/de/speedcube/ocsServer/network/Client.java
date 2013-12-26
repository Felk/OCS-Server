package de.speedcube.ocsServer.network;

import java.net.*;
import java.util.ArrayList;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketConnectionInfo;
import de.speedcube.ocsUtilities.packets.PacketDisconnect;
import de.speedcube.ocsUtilities.packets.PacketSystemMessage;

public class Client {
	private Socket socket;
	public boolean connected = false;
	SendThread sender;
	ReceiveThread receiver;

	public ServerThread server;
	public PacketConnectionInfo connectionInfo;
	public boolean connectionInfoReceived = false;
	public boolean connectionInfoSent = false;
	private Object receiveNotify;
	private User user;
	public String tempUsername;

	public String closeMessage = "";

	public Client(Socket socket, ServerThread server, Object receiveNotify) {
		this.socket = socket;
		this.server = server;
		this.receiveNotify = receiveNotify;
		connected = true;
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
		if (socket != null) {
			return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
		}
		return "";
	}

	public void sendSystemMessage(String msg) {
		sendSystemMessage(msg, new String[] {});
	}

	public void sendSystemMessage(String msg, String... values) {
		PacketSystemMessage p = new PacketSystemMessage();
		p.msg = msg;
		p.global = true;
		p.values = values;
		p.timestamp = System.currentTimeMillis();
		sendPacket(p);
	}

	public void stopClient() {
		connected = false;

		receiver.stopThread();

		sender.stopThread();

		if (user != null) user.logout();

		//if (user != null )server.broadcastData(user.userlist.toPacket());

	}

	public void disconnect(String msg) {
		PacketDisconnect packet = new PacketDisconnect();
		packet.msg = msg;
		sendPacket(packet);
		stopClient();
	}

	public User getUser() {
		if (user == null) System.out.println("returnung user==null!");
		return user;
	}

	public void setUser(User u) {
		this.user = u;
	}

}
