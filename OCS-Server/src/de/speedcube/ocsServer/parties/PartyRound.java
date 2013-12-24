package de.speedcube.ocsServer.parties;

import java.util.ArrayList;
import java.util.HashMap;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsUtilities.PartyTimeTypes;

public class PartyRound {

	private boolean over = false;
	
	private HashMap<User, Integer> times;
	private final String scramble;
	
	public PartyRound(ArrayList<User> users, String scramble) {
		this.times = new HashMap<User, Integer>();
		this.scramble = scramble;
		for (User u : users)
			times.put(u, null);
	}
	
	public void update() {
		if (!times.containsValue(null)) {
			over = true;
		}
	}

	public boolean isOver() {
		return over;
	}

	public void updateOffUsers(Userlist userlist, ArrayList<User> users_left) {
		for (User user : times.keySet()) {
			if (!userlist.hasUser(user)) times.put(user, PartyTimeTypes.OFF);
			if (users_left.contains(user)) times.put(user, PartyTimeTypes.DNS);
		}
	}
	
	public boolean hasUser(User u) {
		return times.containsKey(u);
	}
	
	public void setTime(User user, int time) {
		if (!hasUser(user)) return;
		times.put(user, time);
	}
	
	public Integer getTime(User user) {
		return times.get(user);
	}

	public String getScramble() {
		return scramble;
	}

}
