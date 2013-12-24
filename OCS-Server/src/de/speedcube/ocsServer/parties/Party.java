package de.speedcube.ocsServer.parties;

import java.util.ArrayList;
import java.util.Map;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsUtilities.PartyResultSet;
import de.speedcube.ocsUtilities.PartyStates;
import de.speedcube.ocsUtilities.PartyTimeTypes;
import de.speedcube.ocsUtilities.packets.PacketPartyData;

public class Party {

	private byte state = PartyStates.OPEN;
	private int ownerID;
	private byte type;

	private int id;
	private int round_num;
	private int rounds_num;
	private int rounds_counting;
	private String name;
	private String scrambleType;
	private String[] scrambles;

	private PartyRound[] rounds;
	private PartyResultSet[] results;

	private ArrayList<User> users;
	private ArrayList<User> users_left;
	private Userlist userlist;

	public Party(int id, int ownerID, byte type, int rounds, int counting, String name, Userlist userlist, String scrambleType) {
		this.id = id;
		this.ownerID = ownerID;
		this.type = type;
		this.rounds_num = rounds;
		this.rounds_counting = counting;
		this.name = name;
		this.userlist = userlist;
		this.rounds = new PartyRound[rounds_num];
		this.scrambleType = scrambleType;

		this.round_num = 0;
		users = new ArrayList<User>();
	}

	public void start() {
		if (state == PartyStates.OPEN) {
			nextRound();
			state = PartyStates.RUNNING;
		}
	}

	public void addUser(User u) {
		if (userlist.hasUser(u) || users.contains(u)) return;
		users.add(u);
	}
	
	public void leaveUser(User u) {
		if (!users.contains(u) || users_left.contains(u)) return;
		users_left.add(u);
		setTime(u, PartyTimeTypes.DNS);
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
		// TODO add scrambles
		rounds[round_num - 1] = new PartyRound(users, "");
	}

	private boolean isRoundOver() {
		return getRound().isOver();
	}

	private void setOver() {
		state = PartyStates.OVER;
	}

	public void updateResults() {
		User[] u_array = new User[users.size()];
		u_array = users.toArray(u_array);
		Average average = new Average(u_array, rounds, Math.min(rounds_counting, round_num), type);
		results = new PartyResultSet[average.getResults().size()];
		for (int i = 0; i < rounds.length; i++) scrambles[i] = rounds[i].getScramble();
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
	}
	
	public boolean hasUser(User user) {
		return users.contains(user);
	}

	public boolean isOver() {
		return state == PartyStates.OVER;
	}

	public boolean isRunning() {
		return state == PartyStates.RUNNING;
	}

	public boolean isOpen() {
		return state == PartyStates.OPEN;
	}

	private PartyRound getRound() {
		return rounds[round_num - 1];
	}

	public void setTime(User user, int time) {
		getRound().setTime(user, time);
		updateResults();
	}

	public void updateOfflineUsers() {
		getRound().updateOffUsers(userlist, users_left);
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

	public PacketPartyData toPacket() {
		PacketPartyData packet = new PacketPartyData();
		packet.partyID = id;
		packet.ownerID = ownerID;
		packet.type = type;
		packet.rounds = rounds_num;
		packet.rounds_counting = rounds_counting;
		packet.name = name;
		packet.results = results;
		packet.state = state;
		packet.scrambleType = scrambleType;
		packet.scrambles = scrambles;
		return packet;
	}

	public String getName() {
		return name;
	}

	public int getID() {
		return id;
	}
	
	public int getOwnerID() {
		return ownerID;
	}
	
	public int getType() {
		return type;
	}

	public String getScrambleType() {
		return scrambleType;
	}
}
