package de.speedcube.ocsServer;

import java.sql.SQLException;

import de.speedcube.ocsUtilities.network.Client;
import de.speedcube.ocsUtilities.network.Packet;
import de.speedcube.ocsUtilities.network.PacketChat;
import de.speedcube.ocsUtilities.network.PacketChatBroadcast;
import de.speedcube.ocsUtilities.network.PacketLogin;
import de.speedcube.ocsUtilities.network.PacketLoginFailed;

public class PacketHandler {

	public static void handlePackage(OCSServer server, Client client, Packet packet) {

		switch (packet.getName()) {
		case "Login":
			handleLoginPacket(server, client, (PacketLogin) packet);
			break;
		case "Chat":
			handleChatPacket(server, client, (PacketChat) packet);
			break;
		default:
			break;
		}

	}
	
	public static void handleLoginPacket(OCSServer server, Client client, PacketLogin packet) {
		System.out.println("Got login attempt.");
		try {
			client.clientInformation = server.database.getUser(packet.username, packet.password);
		} catch (SQLException e) {
			client.clientInformation = null;
			System.out.println("error checking login information");
			e.printStackTrace();
		}
		if (client.isValid()) {
			packet.password = "";
			client.sendPacket(packet);
		} else {
			PacketLoginFailed packet2 = new PacketLoginFailed();
			packet2.msg = "Failed to log in!";
			client.sendPacket(packet2);
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
