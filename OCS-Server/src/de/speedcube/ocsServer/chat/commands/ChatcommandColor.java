package de.speedcube.ocsServer.chat.commands;

import java.sql.SQLException;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.chat.Chatmessage;
import de.speedcube.ocsUtilities.Userranks;

public class ChatcommandColor extends Chatcommand {

	@Override
	public Chatmessage parse(OCSServer server, Chatmessage msg) {

		if (msg.getText().length() != 6) return null;

		int c;
		try {
			Integer r = Integer.valueOf(msg.getText().substring(0, 2), 16);
			Integer g = Integer.valueOf(msg.getText().substring(2, 4), 16);
			Integer b = Integer.valueOf(msg.getText().substring(4, 6), 16);
			c = (0xff0000 & r << 16) | (0x00ff00 & g << 8) | (0x0000ff & b);
		} catch (Exception e) {
			return null;
		}

		try {
			server.userlist.getUser(msg.getUserID()).setColor(c, server.database);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return null;
	}

	@Override
	public int getRank() {
		return Userranks.NORMAL;
	}

}
