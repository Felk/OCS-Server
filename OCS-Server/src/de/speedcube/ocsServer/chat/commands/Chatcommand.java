package de.speedcube.ocsServer.chat.commands;

import java.util.HashMap;

import de.speedcube.ocsServer.chat.Chatmessage;

public abstract class Chatcommand {

	protected static HashMap<String, Chatcommand> commandMap = new HashMap<String, Chatcommand>();
	
	public abstract Chatmessage parse(Chatmessage msg);
	
	private static void registerCommand(String cmd, Chatcommand command) {
		if (!commandMap.containsKey(cmd)) commandMap.put(cmd, command);
	}
	
	public static Chatcommand getCommand(String cmd) {
		return commandMap.get(cmd);
	}
	
	static {
		registerCommand("test", new ChatcommandTest());
	}
	
}
