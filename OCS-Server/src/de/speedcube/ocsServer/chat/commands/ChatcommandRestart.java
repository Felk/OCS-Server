package de.speedcube.ocsServer.chat.commands;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.chat.Chatmessage;
import de.speedcube.ocsUtilities.Userranks;

public class ChatcommandRestart extends Chatcommand {

	@Override
	public Chatmessage parse(OCSServer server, Chatmessage msg) {
		server.restart(msg.getText());
		return null;
	}

	@Override
	public int getRank() {
		return Userranks.DEV;
	}

}
