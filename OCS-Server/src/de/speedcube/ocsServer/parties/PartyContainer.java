package de.speedcube.ocsServer.parties;

import java.util.ArrayList;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsUtilities.packets.PacketPartyList;

public class PartyContainer {

	private int last_party_id = 0;
	private ArrayList<Party> parties;
	private Userlist userlist;

	public PartyContainer(Userlist userlist) {
		this.userlist = userlist;
	}

	public Party newParty(int ownerID, byte type, int rounds, int rounds_counting, String name, String scramble) {
		last_party_id++;
		Party p = new Party(last_party_id, ownerID, type, rounds, rounds_counting, name, scramble, userlist);
		parties.add(p);
		return p;
	}

	public Party getParty(int id) {
		for (Party p : parties)
			if (p.getID() == id) return p;
		return null;
	}

	public boolean hasParty(Party p) {
		return parties.contains(p);
	}
	
	public PacketPartyList toPacket() {
		int[] ids = new int[parties.size()];
		for (int i = 0; i < parties.size(); i++)
			ids[i] = parties.get(i).getID();
		PacketPartyList packet = new PacketPartyList();
		packet.partyIDs = ids;
		return packet;
	}
	
	public Party getParty(User user) {
		for (Party p : parties) if (p.hasUser(user)) return p;
		return null;
	}

}
