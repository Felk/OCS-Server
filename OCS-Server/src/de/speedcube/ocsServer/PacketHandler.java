package de.speedcube.ocsServer;

import java.sql.SQLException;

import de.speedcube.ocsServer.chat.Chat;
import de.speedcube.ocsServer.chat.Chatmessage;
import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsServer.parties.Party;
import de.speedcube.ocsUtilities.Config;
import de.speedcube.ocsUtilities.PartyTypes;
import de.speedcube.ocsUtilities.UserInfo;
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
				} else if (p instanceof PacketPartyJoin) {
					handlePartyJoinPacket(server, client, (PacketPartyJoin) p);
					return;
				} else if (p instanceof PacketPartyLeave) {
					handlePartyLeavePacket(server, client, (PacketPartyLeave) p);
					return;
				} else if (p instanceof PacketPartyCreate) {
					handlePartyCreatePacket(server, client, (PacketPartyCreate) p);
					return;
				} else if (p instanceof PacketPartyTime) {
					handlePartyTimePacket(server, client, (PacketPartyTime) p);
					return;
				} else if (p instanceof PacketLogout) {
					System.out.println("GOT LOGOUT PACKET!");
					client.user.logout();
					return;
				} else if (p instanceof PacketChannelWhisper) {
					handleChannelWhisperPacket(server, client, (PacketChannelWhisper) p);
					return;
				}
			}
			if (client.user.userInfo.rank >= Userranks.DEV) {
				// Wenn DEV
				if (p instanceof PacketSound) {
					server.userlist.broadcastData(p);
					return;
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not handle package " + p.getName() + " due to database errors.");
		}

		System.out.println("Packet not handled: " + p.getName());

	}

	private static void handleChannelWhisperPacket(OCSServer server, Client client, PacketChannelWhisper p) {
		
		User user2 = server.userlist.getUser(p.userID);
		if (user2 == null) {
			client.sendSystemMessage("chat.whisper_invalid_user");
			return;
		}
		
		String chatChannel = Chat.getNewWhisperChannel();
		client.user.enterChannel(chatChannel);
		user2.enterChannel(chatChannel);
		
	}

	private static void handlePartyJoinPacket(OCSServer server, Client client, PacketPartyJoin p) {

		Party party = server.parties.getParty(p.partyID);
		if (party == null) {
			client.sendSystemMessage("party.join_fail.not_existent");
			return;
		}
		if (!party.isOpen()) {
			client.sendSystemMessage("party.join_fail.running");
			return;
		}
		// If already in a party
		if (server.parties.getParty(client.user) != null) {
			client.sendSystemMessage("party.join_fail.already_in_party");
			return;
		}
		
		// confirm joining to client
		client.sendPacket(p);

		party.addUser(client.user);
		party.update();
		server.userlist.broadcastData(party.toPacket());

	}
	
	private static void handlePartyLeavePacket(OCSServer server, Client client, PacketPartyLeave p) {

		Party party = server.parties.getParty(p.partyID);
		if (party == null) {
			client.sendSystemMessage("party.leave_fail.not_existent");
			return;
		}
		if (party.isOver()) {
			client.sendSystemMessage("party.leave_fail.over");
			return;
		}
		// If already in a party
		if (server.parties.getParty(client.user) == null) {
			client.sendSystemMessage("party.leave_fail.not_in_party");
			return;
		}
		
		// confirm leaving to client
		client.sendPacket(p);

		party.leaveUser(client.user);
		party.update();
		server.userlist.broadcastData(party.toPacket());

	}

	private static void handlePartyCreatePacket(OCSServer server, Client client, PacketPartyCreate p) {

		if (p.rounds <= 0 || p.rounds_counting <= 0 || p.rounds_counting > p.rounds || !PartyTypes.has(p.type)) {
			client.sendSystemMessage("party.create_fail");
			return;
		}

		Party party = server.parties.newParty(client.user.userInfo.userID, p.type, p.rounds, p.rounds_counting, p.name, p.scrambleType);
		server.userlist.broadcastData(server.parties.toPacket());
		server.userlist.broadcastData(party.toPacket());

	}

	private static void handlePartyTimePacket(OCSServer server, Client client, PacketPartyTime p) {

		Party party = server.parties.getParty(p.partyID);
		if (party == null) {
			client.sendSystemMessage("party.time_fail.not_existent");
			return;
		}
		if (!party.isRunning()) {
			client.sendSystemMessage("party.time_fail.not_running");
			return;
		}
		if (!party.hasUser(client.user)) {
			client.sendSystemMessage("party.time_fail.not_in_party");
			return;
		}

		party.setTime(client.user, p.time);
		party.update();
		server.userlist.broadcastData(party.toPacket());

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

		UserInfo userInfo = server.database.getUserInfo(client.user.userInfo.username, packet.password);

		if (userInfo != null) {
			// SUCCESSFULL LOGIN
			User existing = server.userlist.getUser(userInfo.username);
			if (existing != null) {
				existing.logout();
				// Userlist update btw.
				// Overlog => 2 Userlist updates
			}

			User user = server.userlist.addUser(userInfo, client);
			user.enterChannel(Chat.DEFAULT_CHANNEL);

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
			client.sendPacket(pUserInfo);

			server.userlist.broadcastSystemMessage("chat.login", Chat.DEFAULT_CHANNEL, false, client.user.userInfo.username);

		} else {
			PacketLoginError packetFailed = new PacketLoginError();
			packetFailed.msg = "login.fail";
			client.sendPacket(packetFailed);
		}
	}

	public static void handleRegistrationPacket(OCSServer server, Client client, PacketRegistration packet) throws SQLException {

		System.out.println("Got registration attempt.");

		if (server.database.userExists(packet.username)) {

			PacketRegistrationError packetError = new PacketRegistrationError();
			packetError.err = "reg.username_taken";
			client.sendPacket(packetError);

			//} else if (packet.username.length() > Config.USERNAME_MAX_LENGTH || packet.username.length() < Config.USERNAME_MIN_LENGTH) {
		} else if (!packet.username.matches(Config.USERNAME_REGEXP)) {

			PacketRegistrationError packetError = new PacketRegistrationError();
			packetError.err = "reg.username_invalid";
			client.sendPacket(packetError);

		} else {

			server.database.register(packet.username, packet.password, packet.salt);
			PacketRegistrationSuccess packetSuccess = new PacketRegistrationSuccess();
			packetSuccess.username = packet.username;
			client.sendPacket(packetSuccess);

		}

	}

	public static void handleChatPacket(OCSServer server, Client client, PacketChat packet) {

		System.out.println("Got chat message, channel: "+packet.chatChannel);
		
		Chatmessage msg = new Chatmessage(client.user.userInfo.userID, packet.chatChannel, packet.text, System.currentTimeMillis());

		Chat.parseMessage(server, server.userlist, msg);

	}

}
