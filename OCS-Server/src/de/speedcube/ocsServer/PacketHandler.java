package de.speedcube.ocsServer;

import java.sql.SQLException;

import de.speedcube.ocsServer.chat.Chat;
import de.speedcube.ocsServer.chat.Chatmessage;
import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsUtilities.Config;
import de.speedcube.ocsUtilities.Userranks;
import de.speedcube.ocsUtilities.packets.*;
import de.speedcube.ocsUtilities.security.RandomString;

public class PacketHandler {

	public static void handlePackage(OCSServer server, Client client, Packet p) {

		try {
			if (p instanceof PacketSaltGet) {
				handleSaltGetPacket(server, client, (PacketSaltGet) p);
				return;
			} else if (p instanceof PacketLogin) {
				handleLoginPacket(server, client, (PacketLogin) p);
				return;

			} else if (p instanceof PacketRegistration) {
				handleRegistrationPacket(server, client, (PacketRegistration) p);
				return;

			}
			if (client.user.userInfo.rank >= Userranks.NORMAL) {
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

	public static void handleSaltGetPacket(OCSServer server, Client client, PacketSaltGet packet) throws SQLException {

		// Get the salt for a specific username and generate a random one if the username does not exist
		String salt = server.database.getTransmissionSalt(packet.username);
		if (salt == null) salt = RandomString.getNew(Config.SALT_LENGTH);

		PacketSalt packetSalt = new PacketSalt();
		packetSalt.salt = salt;
		client.sendPacket(packetSalt);

		// Remember the Name, to which the client propably enters a password shortly after
		client.user.userInfo.username = packet.username;

	}

	public static void handleLoginPacket(OCSServer server, Client client, PacketLogin packet) throws SQLException {

		//System.out.println("Got login attempt.");
		//client.clientInformation;
		User user = server.database.getUser(client.user.userInfo.username, packet.password);

		if (user != null) {
			// SUCCESSFULL LOGIN
			User existing = server.userlist.getUser(user.userInfo.username);
			if (existing != null) {
				existing.kick();
				// No userlist update btw.
			}

			server.userlist.addUser(user, client);

			PacketLoginSuccess packetSuccess = new PacketLoginSuccess();
			packetSuccess.username = client.user.userInfo.username;
			packetSuccess.userID = client.user.userInfo.userID;
			client.sendPacket(packetSuccess);

			// Update userlist
			server.userlist.updateUserlist();

			// Update userinfo
			PacketUserInfo pUserInfo = new PacketUserInfo();
			pUserInfo.addUserInfo(user.userInfo);
			server.userlist.broadcastData(pUserInfo);

			// Same for new User
			pUserInfo = new PacketUserInfo();
			for (User u : server.userlist.getUsers())
				pUserInfo.addUserInfo(u.userInfo);
			server.userlist.broadcastData(pUserInfo);

			System.out.println("LOGIN SUCCESSFULL FOR: " + client.user.userInfo.username);

		} else {
			PacketLoginError packetFailed = new PacketLoginError();
			packetFailed.msg = "Failed to log in!";
			client.sendPacket(packetFailed);
		}
	}

	public static void handleRegistrationPacket(OCSServer server, Client client, PacketRegistration packet) throws SQLException {

		System.out.println("Got registration attempt.");

		if (server.database.userExists(packet.username)) {

			PacketRegistrationError packetError = new PacketRegistrationError();
			packetError.err = "reg.username_taken";
			client.sendPacket(packetError);
			System.out.println("Username existiert bereits: " + packet.username);

			//} else if (packet.username.length() > Config.USERNAME_MAX_LENGTH || packet.username.length() < Config.USERNAME_MIN_LENGTH) {
		} else if (!packet.username.matches(Config.USERNAME_REGEXP)) {

			PacketRegistrationError packetError = new PacketRegistrationError();
			packetError.err = "reg.username_invalid";
			client.sendPacket(packetError);
			System.out.println("Username zu lang/kurz: " + packet.username);

		} else {

			server.database.register(packet.username, packet.password, packet.salt);
			PacketRegistrationSuccess packetSuccess = new PacketRegistrationSuccess();
			packetSuccess.username = packet.username;
			client.sendPacket(packetSuccess);
			System.out.println("Registrierung erfolgreich für: " + packet.username);

		}

	}

	public static void handleChatPacket(OCSServer server, Client client, PacketChat packet) {

		Chatmessage msg = new Chatmessage(client.user.userInfo.userID, packet.chatChannel, packet.text, System.currentTimeMillis());

		Chat.parseMessage(server, server.userlist, msg);

	}

}
