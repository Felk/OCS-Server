package de.speedcube.ocsServer;

import java.sql.SQLException;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsUtilities.Config;
import de.speedcube.ocsUtilities.Userranks;
import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketChat;
import de.speedcube.ocsUtilities.packets.PacketChatBroadcast;
import de.speedcube.ocsUtilities.packets.PacketLoginUsername;
import de.speedcube.ocsUtilities.packets.PacketLoginFailed;
import de.speedcube.ocsUtilities.packets.PacketLoginPassword;
import de.speedcube.ocsUtilities.packets.PacketLoginSalt;
import de.speedcube.ocsUtilities.packets.PacketRegistrationPassword;
import de.speedcube.ocsUtilities.packets.PacketRegistrationSalt;
import de.speedcube.ocsUtilities.packets.PacketRegistrationUsername;
import de.speedcube.ocsUtilities.packets.PacketUserlist;
import de.speedcube.ocsUtilities.security.RandomString;

public class PacketHandler {

	public static void handlePackage(OCSServer server, Client client, Packet p) {

		try {
			if (p instanceof PacketLoginUsername) {
				handleLoginNamePacket(server, client, (PacketLoginUsername) p);
				return;
			} else if (p instanceof PacketLoginPassword) {
				handleLoginPasswordPacket(server, client, (PacketLoginPassword) p);
				return;
				
			} else if (p instanceof PacketRegistrationUsername) {
				handleRegistrationUsernamePacket(server, client, (PacketRegistrationUsername) p);
				return;
			} else if (p instanceof PacketRegistrationPassword) {
				handleRegistrationPasswordPacket(server, client, (PacketRegistrationPassword) p);
				return;
				
				
			} else if (client.clientInformation.rank >= Userranks.NORMAL) {
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

	private static void handleRegistrationUsernamePacket(OCSServer server, Client client, PacketRegistrationUsername p) {

		String salt = RandomString.getNew(Config.SALT_LENGTH);
		client.clientInformation.salt = salt;
		
		PacketRegistrationSalt packetSalt = new PacketRegistrationSalt();
		packetSalt.salt = salt;
		client.sendPacket(packetSalt);
		
	}

	public static void handleLoginNamePacket(OCSServer server, Client client, PacketLoginUsername packet) throws SQLException {

		String salt = "";
		salt = server.database.getTransmissionSalt(packet.username);

		PacketLoginSalt packetSalt = new PacketLoginSalt();
		packetSalt.salt = salt;
		client.sendPacket(packetSalt);

		client.clientInformation.username = packet.username;

	}

	public static void handleLoginPasswordPacket(OCSServer server, Client client, PacketLoginPassword packet) throws SQLException {

		System.out.println("Got login attempt.");
		client.clientInformation = server.database.getUser(client.clientInformation.username, packet.password);

		if (client.clientInformation.rank >= Userranks.NORMAL) {
			// SUCCESSFULL LOGIN
			PacketLoginUsername packetUsername = new PacketLoginUsername();
			packetUsername.username = client.clientInformation.username;
			client.sendPacket(packetUsername);

			PacketUserlist packetUserlist = new PacketUserlist();
			int clients_num = server.serverThread.getClients().size();
			int[] userIds = new int[clients_num];
			String[] usernames = new String[clients_num];
			for (int i = 0; i < clients_num; i++) {
				userIds[i] = server.serverThread.getClients().get(i).clientInformation.userId;
				usernames[i] = server.serverThread.getClients().get(i).clientInformation.username;
			}
			packetUserlist.userIds = userIds;
			packetUserlist.usernames = usernames;
			client.sendPacket(packetUserlist);

			System.out.println("LOGIN SUCCESSFULL FOR: " + client.clientInformation.username);

		} else {
			PacketLoginFailed packetFailed = new PacketLoginFailed();
			packetFailed.msg = "Failed to log in!";
			client.sendPacket(packetFailed);
		}
	}
	
	public static void handleRegistrationPasswordPacket(OCSServer server, Client client, PacketRegistrationPassword packet) throws SQLException {

		System.out.println("Got registration attempt.");
		client.clientInformation = server.database.getUser(client.clientInformation.username, packet.password);

		// TODO redo this
		
		if (client.clientInformation.rank >= Userranks.NORMAL) {
			// SUCCESSFULL LOGIN
			PacketLoginUsername packetUsername = new PacketLoginUsername();
			packetUsername.username = client.clientInformation.username;
			client.sendPacket(packetUsername);

			PacketUserlist packetUserlist = new PacketUserlist();
			int clients_num = server.serverThread.getClients().size();
			int[] userIds = new int[clients_num];
			String[] usernames = new String[clients_num];
			for (int i = 0; i < clients_num; i++) {
				userIds[i] = server.serverThread.getClients().get(i).clientInformation.userId;
				usernames[i] = server.serverThread.getClients().get(i).clientInformation.username;
			}
			packetUserlist.userIds = userIds;
			packetUserlist.usernames = usernames;
			client.sendPacket(packetUserlist);

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
		//System.out.println("broadcasted Chat message");
	}

}
