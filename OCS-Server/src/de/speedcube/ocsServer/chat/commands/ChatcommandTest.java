package de.speedcube.ocsServer.chat.commands;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.chat.Chatmessage;
import de.speedcube.ocsUtilities.Userranks;

public class ChatcommandTest extends Chatcommand {

	@Override
	public Chatmessage parse(OCSServer server, Chatmessage msg) {
		msg.setText("TESTCOMMAND: " + msg.getText());
		return msg;
	}

	@Override
	public int getRank() {
		return Userranks.NORMAL;
	}

}
