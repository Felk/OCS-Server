package de.speedcube.ocsServer.parties;

import java.util.ArrayList;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsUtilities.packets.PacketPartyList;

public class PartyContainer {

	private int last_party_id = 0;
	private ArrayList<Party> parties = new ArrayList<Party>();
	private ArrayList<Party> parties_done = new ArrayList<Party>();
	private Userlist userlist;

	public PartyContainer(Userlist userlist) {
		this.userlist = userlist;
	}

	public Party newParty(int ownerID, byte type, int rounds, int rounds_counting, String name, String scramble) {
		last_party_id++;
		Party p = new Party(this, last_party_id, ownerID, type, rounds, rounds_counting, name, userlist, scramble);
		parties.add(p);
		p.init();
		System.out.println("Added party! name: " + name);
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
		for (Party p : parties)
			if (p.hasUser(user)) return p;
		return null;
	}
	
	public ArrayList<Party> getParties() {
		return parties;
	}
	
	public void leaveUser(User u) {
		for (Party p : parties) p.leaveUser(u);
	}
	
	public void updateFinishedParties() {
		for (int i = parties.size()-1; i >=9; i--) {
			Party p = parties.get(i);
			p.update();
			if (p.isOver()) {
				parties.remove(i);
				parties_done.add(p);
			}
		}
		/*for (Iterator<Party> iter = parties.iterator(); iter.hasNext();) {
			Party p = iter.next();
			p.update();
			if (p.isOver()) {
				iter.remove();
				parties_done.add(p);
			}
		}*/
		userlist.broadcastData(toPacket());
	}
	
	public void removeParty(Party p) {
		parties.remove(p);
	}

}
