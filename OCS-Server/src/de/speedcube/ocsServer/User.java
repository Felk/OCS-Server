package de.speedcube.ocsServer;

import java.sql.SQLException;
import java.util.ArrayList;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsServer.sql.OCSDatabase;
import de.speedcube.ocsUtilities.packets.PacketLogout;
import de.speedcube.ocsUtilities.packets.PacketUserInfo;
import de.speedcube.ocsUtilities.UserInfo;

public class User {

	public Userlist userlist;
	private Client client;
	public String salt = "";
	public UserInfo userInfo = new UserInfo();
	public ArrayList<String> channels = new ArrayList<String>();

	public User(Client client) {
		setClient(client);
	}

	public User(Userlist userlist, int id, String username, int rank, int color, String status) {
		this.userInfo = new UserInfo(id, username, rank, color, status);
		this.userlist = userlist;
	}

	public void remove() {
		userInfo = new UserInfo();
		if (userlist != null) {
			userlist.getUsers().remove(this);
			userlist.broadcastData(userlist.toPacket());
		}
	}

	public void closeConnection() {
		if (client != null) client.stopClient();
	}

	public void kick() {
		PacketLogout packetLogout = new PacketLogout();
		packetLogout.msg = "You got kicked!";
		client.sendPacket(packetLogout);
		remove();
	}
	
	public void update() {
		PacketUserInfo packet = new PacketUserInfo();
		packet.addUserInfo(userInfo);
		userlist.broadcastData(packet);
	}

	public void setClient(Client c) {
		this.client = c;
		if (c != null) c.user = this;
	}

	public Client getClient() {
		return client;
	}

	public void setUserlist(Userlist userlist) {
		this.userlist = userlist;
	}

	public void leaveChannel(String channel) {
		channels.remove(channel);
	}

	public boolean enterChannel(String channel) {
		if (!channels.contains(channel)) channels.add(channel);
		return true;
	}

	public void setColor(int color, OCSDatabase db) throws SQLException {
		db.setColor(userInfo.userID, color);
		userInfo.color = color;
		update();
	}

}
