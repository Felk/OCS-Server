package de.speedcube.ocsServer.parties;

import java.util.Comparator;
import java.util.Map;

import de.speedcube.ocsServer.User;

public class TimeComparator implements Comparator<User> {

	Map<User, Integer> base;

	public TimeComparator(Map<User, Integer> base) {
		this.base = base;
	}

	@Override
	public int compare(User u1, User u2) {
		Integer o1 = base.get(u1);
		Integer o2 = base.get(u2);
		if (o1 == null) return -1;
		if (o2 == null) return 1;
		if (o2 > o1 || o2 < 0) return -1;
		if (o1 > o2 || o1 < 0) return 1;
		return 1;
	}

}
