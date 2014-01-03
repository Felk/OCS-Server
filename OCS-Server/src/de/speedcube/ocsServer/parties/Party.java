package de.speedcube.ocsServer.parties;

import java.util.ArrayList;
import java.util.Map;

import de.speedcube.ocsServer.User;
import de.speedcube.ocsServer.Userlist;
import de.speedcube.ocsUtilities.Config;
import de.speedcube.ocsUtilities.PartyResultSet;
import de.speedcube.ocsUtilities.PartyStates;
import de.speedcube.ocsUtilities.PartyTimeTypes;
import de.speedcube.ocsUtilities.packets.PacketPartyData;
import de.speedcube.ocsUtilities.security.RandomString;

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

	private ArrayList<User> users = new ArrayList<User>();
	private ArrayList<User> users_left = new ArrayList<User>();
	private Userlist userlist;

	public final String chatChannel;

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
		this.scrambles = new String[rounds_num];

		this.round_num = 0;
		
		this.chatChannel = newChatChannel();
	}

	private String newChatChannel() {
		return "Party " + name + Config.CHAT_CHANNEL_SEPARATOR + RandomString.getNew(8);
	}

	public void start() {
		if (isOpen()) {
			nextRound();
			state = PartyStates.RUNNING;
		}
	}

	public boolean addUser(User u) {
		if (!userlist.hasUser(u)) return false;
		if (users_left.contains(u))
			users_left.remove(u);
		else
			users.add(u);
		System.out.println("Added user to party, " + u.userInfo.username + ", " + u.userInfo.userID);
		return true;
	}

	public void leaveUser(User u) {
		if (!users.contains(u) || users_left.contains(u)) return;
		users_left.add(u);
		setTime(u, PartyTimeTypes.DNS);
	}

	public void update() {
		if (isOpen()) return;
		updateOffUsers();
		getRound().update();
		boolean isRoundOver = isRoundOver();
		if (isRoundOver && round_num == rounds_num) {
			setOver();
		} else if (isRoundOver && round_num < rounds_num) {
			nextRound();
		}
	}

	public void updateUsers() {
		updateResults();
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

	private void updateResults() {
		User[] u_array = new User[users.size()];
		u_array = users.toArray(u_array);
		Average average = new Average(u_array, rounds, rounds_counting, type);
		results = new PartyResultSet[average.getResults().size()];
		for (int i = 0; i < rounds.length && rounds[i] != null; i++)
			scrambles[i] = rounds[i].getScramble();
		int c = 0;
		for (Map.Entry<User, Integer> entry : average.getResults().entrySet()) {
			User user = entry.getKey();
			int value = (entry.getValue() == null) ? 0 : entry.getValue();
			int[] times = new int[rounds_num];
			for (int i = 0; i < rounds.length; i++) {
				if (rounds[i] == null) {
					if (i == 0) {
						times = null;
						break;
					}
					continue;
				}
				times[i] = (rounds[i].getTime(user) == null) ? PartyTimeTypes.DNS : rounds[i].getTime(user);
			}
			results[c] = new PartyResultSet(user.userInfo.userID, times, value);
			c++;
		}
	}

	public boolean hasUser(User user) {
		return users.contains(user) && !users_left.contains(user);
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
		int index = round_num - 1;
		if (index < 0) return null;
		return rounds[index];
	}

	public void setTime(User user, int time) {
		PartyRound r = getRound();
		if (r == null) return;
		r.setTime(user, time);
		updateResults();
	}

	public void updateOffUsers() {
		getRound().updateOffUsers(userlist, users_left);
		if (!userlist.hasUser(ownerID)) {
			User u = getNextActiveUser();
			if (u == null) {
				userlist.broadcastSystemMessage("party.no_online_owner", chatChannel, true);
				// TODO problem
			} else {
				this.ownerID = u.userInfo.userID;
				userlist.broadcastSystemMessage("party.owner_changed", chatChannel, true, u.userInfo.username);
				// TODO owner change
			}
		}
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
			if (u.userInfo.userID == id && !users_left.contains(u)) return u;
		return null;
	}

	public PacketPartyData toPacket() {
		PacketPartyData packet = new PacketPartyData();
		packet.partyID = id;
		packet.ownerID = ownerID;
		packet.type = type;
		packet.round = round_num;
		packet.rounds = rounds_num;
		packet.rounds_counting = rounds_counting;
		packet.name = name;
		packet.results = results;
		packet.state = state;
		packet.scrambleType = scrambleType;
		packet.scrambles = scrambles;
		//int[] us = new int[users.size()];
		//for (int i = 0; i < users.size(); i++) us[i] = users.get(i).userInfo.userID;
		//packet.users = us;
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

	public void init() {
		//update();
		updateResults();
	}

	public boolean isOwner(User user) {
		return user.userInfo.userID == ownerID;
	}

	public boolean hasLeft(User user) {
		return users_left.contains(user);
	}

	private User getNextActiveUser() {
		for (User u : users) {
			if (!users_left.contains(u)) return u;
		}
		return null;
	}
}
