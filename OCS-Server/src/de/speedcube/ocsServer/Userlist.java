package de.speedcube.ocsServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsUtilities.UserInfo;
import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketSystemMessage;
import de.speedcube.ocsUtilities.packets.PacketUserlist;

public class Userlist {

	private ArrayList<User> users = new ArrayList<User>();
	private String jsonString = "[]";

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
	
	public boolean hasUser(User u) {
		return users.contains(u);
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
			infos.add(u.userInfo);
		}
		return infos;
	}

	public void broadcastData(Packet packet) {
		if (!packet.isPacked()) packet.pack();
		for (User u : users)
			u.getClient().sendPacket(packet);
	}
	
	public void broadcastSystemMessage(String msg) {
		broadcastSystemMessage(msg, new String[]{});
	}
	
	public void broadcastSystemMessage(String msg, String... values) {
		PacketSystemMessage p = new PacketSystemMessage();
		p.msg = msg;
		p.values = values;
		p.timestamp = System.currentTimeMillis();
		broadcastData(p);
	}

	public void updateUserlist() {
		broadcastData(toPacket());
		//writeUserlistFile();
		updateJsonString();
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
	
	public void updateJsonString() {
		StringBuilder s = new StringBuilder();
		s.append("[");
		for (int i = 0; i < users.size(); i++) {
			if (i>0) s.append(",");
			User u = users.get(i);
			s.append("{\"username\":\""+u.userInfo.username+"\",\"color\":\""+u.userInfo.getHexColor()+"\"}");
		}
		s.append("]");
		jsonString = s.toString();
	}
	
	public String getJsonString() {
		return jsonString;
	}
	
	public void removeUser(User u) {
		if (users.remove(u)) broadcastSystemMessage("chat.logout", u.userInfo.username);
	}
}
