package de.speedcube.ocsServer;

import java.sql.SQLException;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsUtilities.Config;
import de.speedcube.ocsUtilities.Userranks;
import de.speedcube.ocsUtilities.language.LangAuthentification;
import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketChat;
import de.speedcube.ocsUtilities.packets.PacketChatBroadcast;
import de.speedcube.ocsUtilities.packets.PacketLogin;
import de.speedcube.ocsUtilities.packets.PacketLoginError;
import de.speedcube.ocsUtilities.packets.PacketLoginSuccess;
import de.speedcube.ocsUtilities.packets.PacketRegistration;
import de.speedcube.ocsUtilities.packets.PacketRegistrationError;
import de.speedcube.ocsUtilities.packets.PacketRegistrationSuccess;
import de.speedcube.ocsUtilities.packets.PacketSalt;
import de.speedcube.ocsUtilities.packets.PacketSaltGet;
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

			} else if (client.user.rank >= Userranks.NORMAL) {
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
		client.user.username = packet.username;

	}

	public static void handleLoginPacket(OCSServer server, Client client, PacketLogin packet) throws SQLException {

		System.out.println("Got login attempt.");
		//client.clientInformation;
		User user = server.database.getUser(client.user.username, packet.password);

		if (user != null) {
			// SUCCESSFULL LOGIN
			User existing = server.userlist.getUser(user.username);
			if (existing != null) {
				existing.kick();
			}

			server.userlist.addUser(user, client);

			PacketLoginSuccess packetSuccess = new PacketLoginSuccess();
			packetSuccess.username = client.user.username;
			client.sendPacket(packetSuccess);

			server.serverThread.broadcastData(server.userlist.toPacket());

			System.out.println("LOGIN SUCCESSFULL FOR: " + client.user.username);

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
			packetError.errNr = LangAuthentification.ERR_REG_USERNAME_TAKEN;
			System.out.println("Username existiert bereits");
		} else if (packet.username.length() > Config.USERNAME_MAX_LENGTH || packet.username.length() < Config.USERNAME_MIN_LENGTH) {
			PacketRegistrationError packetError = new PacketRegistrationError();
			packetError.errNr = LangAuthentification.ERR_REG_USERNAME_INVALID;
			System.out.println("Username zu lang/kurz");
		} else {
			server.database.register(packet.username, packet.password, packet.salt);
			PacketRegistrationSuccess packetSuccess = new PacketRegistrationSuccess();
			packetSuccess.username = packet.username;
			System.out.println("Registrierung erfolgreich für: "+packet.username);
		}

	}

	public static void handleChatPacket(OCSServer server, Client client, PacketChat packet) {
		System.out.println("Got chat message: " + packet.text);
		PacketChatBroadcast broadcast = new PacketChatBroadcast();
		broadcast.text = packet.text;
		if (client.user != null)
			broadcast.userId = client.user.userId;
		else
			broadcast.userId = 0;
		server.serverThread.broadcastData(broadcast);
		//System.out.println("broadcasted Chat message");
	}

}
