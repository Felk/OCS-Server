package de.speedcube.ocsServer.chat.commands;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.PacketHandler;
import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.chat.Chatmessage;
import de.speedcube.ocsServer.parties.Party;
import de.speedcube.ocsUtilities.Userranks;
import de.speedcube.ocsUtilities.packets.PacketPartyStart;

public class ChatcommandPartyStart extends Chatcommand {

	@Override
	public Chatmessage parse(OCSServer server, User user, Chatmessage msg) {

		Party party = server.parties.getParty(user);
		int partyID = (party == null) ? -1 : party.getID();

		PacketPartyStart packet = new PacketPartyStart();
		packet.partyID = partyID;
		PacketHandler.handlePartyStartPacket(server, user.getClient(), packet);

		return null;
	}

	@Override
	public int getRank() {
		return Userranks.NORMAL;
	}

}
