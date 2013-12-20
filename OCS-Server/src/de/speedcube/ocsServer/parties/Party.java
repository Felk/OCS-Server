package de.speedcube.ocsServer.parties;

import java.util.ArrayList;
import java.util.Map;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsUtilities.PartyResultSet;

public class Party {

	public static final int OFF = -1;
	public static final int DNF = -2;
	public static final int DNS = -3;
	public static final int DNK = -4;

	private boolean over = false;

	private int id;
	private int round_num;
	private int rounds_num;
	private int rounds_counting;
	private String scramble;
	
	private PartyRound[] rounds;
	private PartyResultSet[] results;

	private ArrayList<User> users;
	private Userlist userlist;

	public Party(int id, int rounds, int counting, Userlist userlist) {
		this.id = id;
		this.rounds_num = rounds;
		this.rounds_counting = counting;
		this.userlist = userlist;
		this.rounds = new PartyRound[rounds_num];

		this.round_num = 0;
		users = new ArrayList<User>();
	}

	public void start() {
		nextRound();
	}

	public void addUser(User u) {
		if (userlist.hasUser(u)) return;
		users.add(u);
	}

	public void update() {
		getRound().update();
		boolean isRoundOver = isRoundOver();
		if (isRoundOver && round_num == rounds_num) {
			setOver();
		} else if (isRoundOver && round_num < rounds_num) {
			nextRound();
		}
	}

	private void nextRound() {
		round_num++;
		rounds[round_num - 1] = new PartyRound(users);
	}

	private boolean isRoundOver() {
		return getRound().isOver();
	}

	private void setOver() {
		//average = new PartyRound(users);
		User[] u_array = new User[users.size()];
		u_array = users.toArray(u_array);
		Average average = new Average(u_array, rounds, rounds_counting);
		results = new PartyResultSet[average.getResults().size()];
		int c = 0;
		for (Map.Entry<User, Integer> entry : average.getResults().entrySet()) {
			User user = entry.getKey();
			int value = entry.getValue();
			int[] times = new int[rounds_num];
			for (int i = 0; i < rounds.length; i++) {
				times[i] = rounds[i].getTime(user);
			}
			results[c] = new PartyResultSet(user.userInfo.userID, times, value);
			c++;
		}
		over = true;
	}

	public boolean isOver() {
		return over;
	}

	private PartyRound getRound() {
		return rounds[round_num - 1];
	}

	public void setTime(User user, int time) {
		getRound().setTime(user, time);
	}

	public void updateOfflineUsers() {
		getRound().updateOfflineUsers(userlist);
	}

	public String getDisplay() {

		StringBuilder sb = new StringBuilder();
		sb.append("Partyergebnisse:");
		for (PartyResultSet result : results) {
			sb.append('\n');
			User user = getUser(result.getUserID());
			int average = result.getAverage();
			sb.append(user.userInfo.username + " " + average + ":");
			for (int t : result.getTimes())
				sb.append(" " + t);
		}
		return sb.toString();

	}

	private User getUser(int id) {
		for (User u : users)
			if (u.userInfo.userID == id) return u;
		return null;
	}

}
