package de.speedcube.ocsServer;

import java.sql.SQLException;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketChat;
import de.speedcube.ocsUtilities.packets.PacketChatBroadcast;
import de.speedcube.ocsUtilities.packets.PacketLoginUsername;
import de.speedcube.ocsUtilities.packets.PacketLoginFailed;
import de.speedcube.ocsUtilities.packets.PacketLoginPassword;
import de.speedcube.ocsUtilities.packets.PacketLoginSalt;

public class PacketHandler {

	public static void handlePackage(OCSServer server, Client client, Packet packet) {

		// unauthorized
		switch (packet.getName()) {
		case "LoginUsername":
			handleLoginNamePacket(server, client, (PacketLoginUsername) packet);
			return;
		case "LoginPassword":
			handleLoginPasswordPacket(server, client, (PacketLoginPassword) packet);
			return;
		}
		
		// authorized
		if (client.isAuthorized()) switch (packet.getName()) {
		case "Chat":
			handleChatPacket(server, client, (PacketChat) packet);
			return;
		}
		
		System.out.println("Packet not handled: "+packet.getName());

	}
	
	public static void handleLoginNamePacket(OCSServer server, Client client, PacketLoginUsername packet) {
		String salt = "";
		try {
			salt = server.database.getTransmissionSalt(packet.username);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("ERROR: Could not retrieve transmission salt for " + packet.username);
			return;
		}
		PacketLoginSalt packetSalt = new PacketLoginSalt();
		packetSalt.salt = salt;
		client.sendPacket(packetSalt);
		client.username = packet.username;
	}
	
	public static void handleLoginPasswordPacket(OCSServer server, Client client, PacketLoginPassword packet) {
		System.out.println("Got login attempt.");
		try {
			client.clientInformation = server.database.getUser(client.username, packet.password);
		} catch (SQLException e) {
			client.clientInformation = null;
			System.out.println("error checking login information");
			e.printStackTrace();
		}
		if (client.isAuthorized()) {
			PacketLoginUsername packetUsername = new PacketLoginUsername();
			packetUsername.username = client.clientInformation.username;
			client.sendPacket(packetUsername);
			System.out.println("LOGIN SUCCESSFULL FOR: "+client.clientInformation.username);
		} else {
			PacketLoginFailed packetFailed = new PacketLoginFailed();
			packetFailed.msg = "Failed to log in!";
			client.sendPacket(packetFailed);
		}
	}
	
	public static void handleChatPacket(OCSServer server, Client client, PacketChat packet) {
		System.out.println("Got chat message: "+packet.text);
		PacketChatBroadcast broadcast = new PacketChatBroadcast();
		broadcast.text = packet.text;
		if (client.clientInformation != null) broadcast.userId = client.clientInformation.userId;
		else broadcast.userId = 0;
		server.serverThread.broadcastData(broadcast);
		//System.out.println("broadcasted Chat message");
	}
	
}
