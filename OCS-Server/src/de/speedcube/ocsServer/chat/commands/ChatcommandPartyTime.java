package de.speedcube.ocsServer.chat.commands;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.PacketHandler;
import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.chat.Chatmessage;
import de.speedcube.ocsServer.parties.Party;
import de.speedcube.ocsUtilities.Userranks;
import de.speedcube.ocsUtilities.packets.PacketPartyTime;

public class ChatcommandPartyTime extends Chatcommand {

	@Override
	public Chatmessage parse(OCSServer server, User user, Chatmessage msg) {
		int time;
		try {
			time = Integer.parseInt(msg.getText());
		} catch (NumberFormatException e) {
			user.getClient().sendSystemMessage("party.time_fail.invalid_time");
			return null;
			//e.printStackTrace();
		}
		Party party = server.parties.getParty(user);
		int partyID = (party == null) ? -1 : party.getID();

		PacketPartyTime packet = new PacketPartyTime();
		packet.time = time;
		packet.partyID = partyID;
		PacketHandler.handlePartyTimePacket(server, user.getClient(), packet);

		return null;
	}

	@Override
	public int getRank() {
		return Userranks.NORMAL;
	}

}
