package de.speedcube.ocsServer.chat.commands;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.chat.Chatmessage;
import de.speedcube.ocsUtilities.Userranks;

public class ChatcommandStop extends Chatcommand {

	@Override
	public Chatmessage parse(OCSServer server, User user, Chatmessage msg) {
		server.stop(msg.getText());
		return null;
	}

	@Override
	public int getRank() {
		return Userranks.DEV;
	}

}
