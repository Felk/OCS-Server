package de.speedcube.ocsServer.chat;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsServer.chat.commands.Chatcommand;
import de.speedcube.ocsUtilities.security.RandomString;

public class Chat {

	public static final int MAX_CHAT_LENGTH = 2000;
	public static final char CHANNEL_WHISPER = '#';
	public static final String DEFAULT_CHANNEL = "Main";

	public static void parseMessage(OCSServer server, Userlist userlist, Chatmessage msg) {

		User u = userlist.getUser(msg.getUserID());
		if (u == null) return;
		
		if (msg.getText().length() > MAX_CHAT_LENGTH) {
			// send an error message?
			return;
		}

		msg.setText(msg.getText().trim());
		if (msg.getText().isEmpty()) return;

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
					msg = chatcommand.parseMessage(server, msg);
				} else {
					msg = null;
					u.getClient().sendSystemMessage("chat.command.insufficient_rank", new String[]{command});
				}
			} else {
				msg = null;
				u.getClient().sendSystemMessage("chat.command.unknown", new String[]{command});
			}
		}
		
		// maybe parsed to null (errors, non-chat commands)
		if (msg == null) return;

		Chat.broadcastMessage(userlist, msg);

	}

	public static void broadcastMessage(Userlist userlist, Chatmessage msg) {

		for (User u : userlist.getUsers()) {
			if (u.isInChannel(msg.getChannel())) {
				sendMessage(msg, u);
			}
		}

	}

	public static void sendMessage(Chatmessage msg, User user) {
		sendMessage(msg, new User[] { user });
	}
	
	// Not used. Client has to escape
	public static String escapeHTML(String s) {
		return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	public static void sendMessage(Chatmessage msg, User[] users) {
		// Escape HTML
		//msg.setText(escapeHTML(msg.getText()));
		for (User u : users)
			u.getClient().sendPacket(msg.toPacket());
	}

	public static String getNewWhisperChannel() {
		return CHANNEL_WHISPER+RandomString.getNew(8);
	}

}
