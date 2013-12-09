package de.speedcube.ocsServer.chat;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsUtilities.packets.PacketChatBroadcast;

public class Chat {

	public static void broadcastMessage(String channel, Userlist userlist, PacketChatBroadcast packet) {
		
		for (User u: userlist.getUsers()) {
			if (u.channels.contains(channel)) u.getClient().sendPacket(packet);
		}
		
	}
	
}
