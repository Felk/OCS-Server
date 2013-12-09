package de.speedcube.ocsServer;

import java.util.ArrayList;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsUtilities.UserInfo;
import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketUserlist;

public class Userlist {

	private ArrayList<User> users = new ArrayList<User>();
	
	public Userlist() {
		
	}
	
	public User getUser(String username) {
		for(User u: users) {
			if (u == null) {
				System.out.println("USER IS NULL!");
				continue;
			}
			if (u.userInfo == null) {
				System.out.println("USERINFO IS NULL!");
				continue;
			}
			if (u.userInfo.username == null) {
				System.out.println("USERNAME IS NULL!");
				continue;
			}
			if (u.userInfo.username.equals(username)) return u;
		}
		return null;
	}
	
	public boolean addUser(User user, Client client) {
		user.setClient(client);
		user.setUserlist(this);
		return addUser(user);
	}
	
	public boolean addUser(User user) {
		if (users.contains(user))
			return false;
		users.add(user);
		return true;
	}
	
	public PacketUserlist toPacket() {
		int num = users.size();
		int[] userIds = new int[num];
		for (int i = 0; i < num; i++) {
			userIds[i] = users.get(i).userInfo.userID;
		}
		PacketUserlist p = new PacketUserlist();
		p.userIds = userIds;
		return p;
	}
	
	public ArrayList<User> getUsers() {
		return users;
	}
	
	public ArrayList<UserInfo> getUserInfos() {
		ArrayList<UserInfo> infos = new ArrayList<UserInfo>();
		for (User u : users) infos.add(u.userInfo);
		return infos;
	}
	
	public void broadcastData(Packet packet) {
		for (User u : users) u.getClient().sendPacket(packet);
	}
	
}
