package de.speedcube.ocsServer.parties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsUtilities.PartyTimeTypes;
import de.speedcube.ocsUtilities.PartyTypes;

public class Average {

	private PartyRound[] rounds;
	private User[] users;
	private int max;
	private int counting_num;
	private byte type;

	private TreeMap<User, Integer> results;

	public Average(User[] users, PartyRound[] rounds, int counting, byte type) {
		this.users = users;
		this.rounds = rounds;
		this.counting_num = counting;
		this.type = type;
		max = 0;
		for (PartyRound pr : rounds)
			if (pr != null) max++;
		results = calculateAverage();
	}

	private TreeMap<User, Integer> calculateAverage() {

		HashMap<User, Integer> averageMap = new HashMap<User, Integer>();

		for (int i = 0; i < users.length; i++) {
			User user = users[i];
			ArrayList<Integer> timesA = new ArrayList<Integer>();
			for (int j = 0; j < max; j++) {
				//if (rounds[j] == null) continue;
				if (rounds[j].getTime(user) == null) continue;
				timesA.add(rounds[j].getTime(user));
			}

			// Copy ArrayList to Array
			int[] times = new int[timesA.size()];
			for (int j = 0; j < timesA.size(); j++)
				times[j] = timesA.get(j);

			sortTimes(times);
			Integer average = getAverage(times, counting_num);
			//System.out.println("Times for " + user.userInfo.username + ": " + Arrays.toString(times) + ", average: " + average);
			averageMap.put(user, average == null ? 0 : average);
		}

		TimeComparator tc = new TimeComparator(averageMap);
		TreeMap<User, Integer> sorted = new TreeMap<User, Integer>(tc);

		sorted.putAll(averageMap);
		return sorted;

	}

	private void sortTimes(int[] times) {
		int help;
		for (int i = 0; i < times.length; i++) {
			for (int j = i + 1; j < times.length; j++) {
				if ((times[i] < 0 || times[i] > times[j]) && times[j] >= 0) {
					help = times[i];
					times[i] = times[j];
					times[j] = help;
				}
			}
		}
	}

	private Integer getAverage(int[] times, int counting) {

		if (counting == 0 || times.length == 0) return null;

		System.out.println("rounds length: " + rounds.length);

		int sub_counting = (int) Math.round(times.length * (((double) counting) / rounds.length));
		if (sub_counting < 1) sub_counting = 1;
		int margin = times.length - sub_counting;

		int average = 0;
		if (type == PartyTypes.AVG) {

			int min = (int) Math.floor(margin / 2);
			int max = (int) (times.length - Math.ceil(margin / 2));

			System.out.println("length: " + times.length + ", counting: " + counting);
			System.out.println("sub: " + sub_counting + ", margin: " + margin);
			System.out.println("Min: " + min + ", max: " + max);

			max = Math.min(max, times.length);
			System.out.println("sorted times: " + Arrays.toString(times));
			for (int i = min; i < max; i++) {
				if (times[i] < 0) return PartyTimeTypes.DNF;
				average += times[i];
			}
			average /= max - min;
		} else if (type == PartyTypes.BEST) {
			average = PartyTimeTypes.DNF;
			for (int i = 0; i < times.length; i++) {
				if (times[i] >= 0 && (times[i] < average || average < 0)) average = times[i];
			}
		}
		if (average < 0) return PartyTimeTypes.DNF;
		return average;
	}

	public TreeMap<User, Integer> getResults() {
		return results;
	}

}