package de.speedcube.ocsServer.chat.commands;

import de.speedcube.ocsServer.chat.Chatmessage;

public class ChatcommandTest extends Chatcommand {

	@Override
	public Chatmessage parse(Chatmessage msg) {
		msg.setText("TESTCOMMAND: "+msg.getText());
		return msg;
	}

}
