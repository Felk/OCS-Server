package de.speedcube.ocsServer.chat;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsServer.chat.commands.Chatcommand;

public class Chat {

	public static void parseMessage(Userlist userlist, Chatmessage msg) {

		String command = msg.getText().split(" ")[0];
		if (command.substring(0, 1).equals("/")) {
			Chatcommand chatcommand = Chatcommand.getCommand(command.substring(1));
			if (chatcommand != null) msg = chatcommand.parse(msg);
		}

		// TODO temporary fix
		msg.setText(msg.getText().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));

		Chat.broadcastMessage(userlist, msg);

	}

	public static void broadcastMessage(Userlist userlist, Chatmessage msg) {

		for (User u : userlist.getUsers()) {
			if (msg.getChannel().isEmpty() || u.channels.contains(msg.getChannel())) {
				u.getClient().sendPacket(msg.toPacket());
			}
		}

	}

}
