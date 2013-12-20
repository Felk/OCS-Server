package de.speedcube.ocsServer.parties;

import java.util.HashMap;
import java.util.TreeMap;

import de.speedcube.ocsServer.User;

public class Average {

	private PartyRound[] rounds;
	private User[] users;
	private int min;

	private TreeMap<User, Integer> results;

	public Average(User[] users, PartyRound[] rounds, int counting) {
		this.users = users;
		this.rounds = rounds;
		this.min = counting;
		if (counting <= rounds.length && counting > 0)
			results = calculateAverage();
		else {
			results = null;
		}
	}

	private TreeMap<User, Integer> calculateAverage() {

		HashMap<User, Integer> averageMap = new HashMap<User, Integer>();

		for (int i = 0; i < users.length; i++) {
			User user = users[i];
			int[] times = new int[rounds.length];
			for (int j = 0; j < rounds.length; j++) {
				times[j] = rounds[j].getTime(user);
			}
			sortTimes(times);
			averageMap.put(user, getAverage(times, min));
		}

		TimeComparator tc = new TimeComparator(averageMap);
		TreeMap<User, Integer> sorted = new TreeMap<User, Integer>(tc);

		sorted.putAll(averageMap);
		return sorted;

	}

	private void sortTimes(int[] times) {
		int help;
		for (int i = 0; i < times.length; i++) {
			for (int j = i; j < times.length; j++) {
				if (times[i] < 0 || times[i] > times[j]) {
					help = times[i];
					times[i] = times[j];
					times[j] = help;
				}
			}
		}
	}

	private Integer getAverage(int[] times, int counting) {
		int min = (times.length - counting) / 2;
		int max = (times.length - counting + 1) / 2 + counting;
		int average = 0;
		for (int i = min; i < max; i++) {
			if (times[i] < 0) return Party.DNF;
			average += times[i];
		}
		average /= counting;
		return average;
	}
	
	public TreeMap<User, Integer> getResults() {
		return results;
	}

}