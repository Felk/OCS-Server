package de.speedcube.ocsServer.parties;

import java.util.ArrayList;
import java.util.HashMap;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;

public class PartyRound {

	private boolean over = false;
	
	private HashMap<User, Integer> times;
	
	public PartyRound(ArrayList<User> users) {
		this.times = new HashMap<User, Integer>();
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

	public void updateOfflineUsers(Userlist userlist) {
		for (User user : times.keySet())
			if (!userlist.hasUser(user)) times.put(user, Party.OFF);
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

}
