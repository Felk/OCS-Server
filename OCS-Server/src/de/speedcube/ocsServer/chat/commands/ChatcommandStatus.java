package de.speedcube.ocsServer.chat.commands;

import java.sql.SQLException;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.chat.Chatmessage;
import de.speedcube.ocsUtilities.Userranks;

public class ChatcommandStatus extends Chatcommand {

	@Override
	public Chatmessage parse(OCSServer server, Chatmessage msg) {

		// Database length
		if (msg.getText().length() > 200) msg.setText(msg.getText().substring(0, 200));

		User user = server.userlist.getUser(msg.getUserID());
		try {
			user.setStatus(msg.getText(), server.database);
		} catch (SQLException e) {
			System.out.println("Could not update status due to database rrrors");
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public int getRank() {
		return Userranks.NORMAL;
	}

}
