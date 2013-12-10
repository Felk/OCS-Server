package de.speedcube.ocsServer.chat;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsServer.chat.commands.Chatcommand;

public class Chat {

	public static void parseMessage(OCSServer server, Userlist userlist, Chatmessage msg) {

		String command = msg.getText().split(" ")[0];
		if (command.substring(0, 1).equals("/")) {
			Chatcommand chatcommand = Chatcommand.getCommand(command.substring(1));
			if (chatcommand != null) {
				if (chatcommand.getRank() <= userlist.getUser(msg.getUserID()).userInfo.rank) {
					if (command.length() + 1 < msg.getText().length()) {
						String new_text = msg.getText().substring(command.length() + 1);
						msg.setText(new_text);
					} else {
						msg.setText("");
					}
					msg = chatcommand.parse(server, msg);
				} else {
					// TODO hack. Please nerf
					msg.setText("Nicht genügend Rechte");
				}
			} else {
				// TODO hack. Please nerf
				msg.setText("Befehl wurde nicht erkannt.");
			}
		}

		if (msg == null) return;
		
		// TODO temporary fix
		msg.setText(msg.getText().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));

		Chat.broadcastMessage(userlist, msg);

	}

	public static void broadcastMessage(Userlist userlist, Chatmessage msg) {

		System.out.println("trying to send chatpacket for " + msg.getChannel());

		for (User u : userlist.getUsers()) {
			if (msg.getChannel().isEmpty() || u.channels.contains(msg.getChannel())) {
				u.getClient().sendPacket(msg.toPacket());
			}
		}

	}

}
