package de.speedcube.ocsServer.chat.commands;

import java.util.HashMap;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.chat.Chatmessage;

public abstract class Chatcommand {

	protected static HashMap<String, Chatcommand> commandMap = new HashMap<String, Chatcommand>();

	public Chatmessage parseMessage(OCSServer server, Chatmessage msg) {
		return parse(server, server.userlist.getUser(msg.getUserID()), msg);
	}

	public abstract Chatmessage parse(OCSServer server, User user, Chatmessage msg);

	public abstract int getRank();

	private static void registerCommand(String cmd, Chatcommand command) {
		if (!commandMap.containsKey(cmd)) commandMap.put(cmd, command);
	}

	public static Chatcommand getCommand(String cmd) {
		return commandMap.get(cmd);
	}

	static {
		// Parties
		registerCommand("time", new ChatcommandPartyTime());
		ChatcommandPartyStart partyStart = new ChatcommandPartyStart();
		registerCommand("go", partyStart);
		registerCommand("start", partyStart);
		
		// UserInfo
		registerCommand("color", new ChatcommandColor());
		registerCommand("status", new ChatcommandStatus());
		
		// System
		registerCommand("stop", new ChatcommandStop());
		registerCommand("restart", new ChatcommandRestart());
	}

}
