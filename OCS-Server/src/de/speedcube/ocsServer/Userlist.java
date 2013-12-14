package de.speedcube.ocsServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
		for (User u : users) {
			if (u.userInfo.username.equals(username)) return u;
		}
		return null;
	}

	public User getUser(int id) {
		for (User u : users) {
			if (u.userInfo.userID == id) return u;
		}
		return null;
	}

	public boolean addUser(User user, Client client) {
		user.setClient(client);
		user.setUserlist(this);
		return addUser(user);
	}

	public boolean addUser(User user) {
		if (users.contains(user)) return false;
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
		for (User u : users) {
			System.out.println("USER: " + u);
			infos.add(u.userInfo);
		}
		return infos;
	}

	public void broadcastData(Packet packet) {
		if (!packet.isPacked()) packet.pack();
		for (User u : users)
			u.getClient().sendPacket(packet);
	}

	public void updateUserlist() {
		broadcastData(toPacket());
		writeUserlistFile();
	}

	public void writeUserlistFile() {

		BufferedWriter writer = null;
		try {
			File file = new File("online.txt");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(toTxtString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}

	}

	public String toTxtString() {
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (User u : users) {
			if (!first) sb.append(",");
			sb.append(u.userInfo.username+":"+Integer.toHexString(u.userInfo.color));
			first = false;
		}
		
		return sb.toString();
		
	}
}
