package de.speedcube.ocsServer;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsServer.network.Packet;
import de.speedcube.ocsServer.network.PacketChat;
import de.speedcube.ocsServer.network.PacketChatBroadcast;

public class PacketHandler {

	public static void handlePackage(OCSServer server, Client client, Packet packet) {

		switch (packet.getName()) {
		case "Login":
			// TODO Account validation
			client.clientInformation.userId = 1;
			break;
		case "Chat":
			handleChatPacket(server, client, (PacketChat) packet);
			break;
		default:
			break;
		}

	}
	
	public static void handleChatPacket(OCSServer server, Client client, PacketChat packet) {
		PacketChatBroadcast broadcast = new PacketChatBroadcast();
		broadcast.text = packet.text;
		broadcast.userId = client.clientInformation.userId;
		server.serverThread.broadcastData(packet);
		
	}
	
}
