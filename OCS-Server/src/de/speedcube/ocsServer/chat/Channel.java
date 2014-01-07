package de.speedcube.ocsServer.chat;

import java.util.ArrayList;

import de.speedcube.ocsServer.User;

public class Channel {

	public static final int TYPE_DEFAULT = 1;
	public static final int TYPE_PARTY = 2;
	public static final int TYPE_PRIVATE = 3;
	
	private int type;
	private ArrayList<User> users = new ArrayList<User>();
	
	public Channel(int type) {
		setType(type);
	}
	
	public boolean addUser(User user) {
		if (users.contains(user)) return false;
		return users.add(user);
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public boolean isPrivateChannel() {
		return type == TYPE_PRIVATE;
	}
	
	public boolean isPartyChannel() {
		return type == TYPE_PARTY;
	}
	
}
