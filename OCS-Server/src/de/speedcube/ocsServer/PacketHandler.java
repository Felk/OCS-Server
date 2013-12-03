package de.speedcube.ocsServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketChat;
import de.speedcube.ocsUtilities.packets.PacketChatBroadcast;
import de.speedcube.ocsUtilities.packets.PacketLoginUsername;
import de.speedcube.ocsUtilities.packets.PacketLoginFailed;
import de.speedcube.ocsUtilities.packets.PacketLoginPassword;
import de.speedcube.ocsUtilities.packets.PacketLoginSalt;
import de.speedcube.ocsUtilities.packets.PacketUserlist;

public class PacketHandler {

	public static void handlePackage(OCSServer server, Client client, Packet p) {

		try {
			if (p instanceof PacketLoginUsername) {
				handleLoginNamePacket(server, client, (PacketLoginUsername) p);
			} else if (p instanceof PacketLoginPassword) {
				handleLoginPasswordPacket(server, client, (PacketLoginPassword) p);
				return;
			} else if (client.isAuthorized()) {
				// Wenn authentifiziert
				if (p instanceof PacketChat) {
					handleChatPacket(server, client, (PacketChat) p);
					return;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not handle package " + p.getName() + " due to database errors.");
		}

		System.out.println("Packet not handled: " + p.getName());

	}

	public static void handleLoginNamePacket(OCSServer server, Client client, PacketLoginUsername packet) throws SQLException {

		String salt = "";
		salt = server.database.getTransmissionSalt(packet.username);

		PacketLoginSalt packetSalt = new PacketLoginSalt();
		packetSalt.salt = salt;
		client.sendPacket(packetSalt);

		client.username = packet.username;

	}

	public static void handleLoginPasswordPacket(OCSServer server, Client client, PacketLoginPassword packet) throws SQLException {

		System.out.println("Got login attempt.");
		client.clientInformation = server.database.getUser(client.username, packet.password);

		if (client.isAuthorized()) {
			// SUCCESSFULL LOGIN
			PacketLoginUsername packetUsername = new PacketLoginUsername();
			packetUsername.username = client.clientInformation.username;
			client.sendPacket(packetUsername);

			PacketUserlist packetUserlist = new PacketUserlist();
			ArrayList<Integer> userIds = new ArrayList<Integer>();
			ArrayList<String> usernames = new ArrayList<String>();
			for (int i = 0; i < server.serverThread.getClients().size(); i++) {
				if (!server.serverThread.getClients().get(i).isAuthorized()) continue;
				userIds.add(server.serverThread.getClients().get(i).clientInformation.userId);
				usernames.add(server.serverThread.getClients().get(i).clientInformation.username);
			}
			packetUserlist.userIds = toPrimitives(userIds);
			packetUserlist.usernames = (String[]) usernames.toArray(new String[usernames.size()]);
			server.serverThread.broadcastData(packetUserlist);

			System.out.println("LOGIN SUCCESSFULL FOR: " + client.clientInformation.username);

		} else {
			PacketLoginFailed packetFailed = new PacketLoginFailed();
			packetFailed.msg = "Failed to log in!";
			client.sendPacket(packetFailed);
		}
	}

	public static void handleChatPacket(OCSServer server, Client client, PacketChat packet) {
		System.out.println("Got chat message: " + packet.text);
		PacketChatBroadcast broadcast = new PacketChatBroadcast();
		broadcast.text = packet.text;
		if (client.clientInformation != null)
			broadcast.userId = client.clientInformation.userId;
		else
			broadcast.userId = 0;
		server.serverThread.broadcastData(broadcast);
	}

	public static int[] toPrimitives(ArrayList<Integer> integers) {
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().intValue();
		}
		return ret;
	}

}
