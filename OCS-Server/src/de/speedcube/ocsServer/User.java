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
		if (userlist != null) {
			userlist.removeUser(this);
			userlist.updateUserlist();
		}
		userInfo = new UserInfo();
	}

	public void closeConnection() {
		if (client != null) client.stopClient();
	}

	public void kick() {
		PacketLogout packetLogout = new PacketLogout();
		packetLogout.msg = "system.msg.logout";
		client.sendPacket(packetLogout);
		remove();
	}

	public void update() {
		PacketUserInfo packet = new PacketUserInfo();
		packet.addUserInfo(userInfo);
		userlist.broadcastData(packet);
		userlist.updateJsonString();
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
		update();
	}

	public boolean enterChannel(String channel) {
		if (!channels.contains(channel)) channels.add(channel);
		update();
		return true;
	}

	public void setColor(int color, OCSDatabase db) throws SQLException {
		userInfo.color = color;
		db.updateUserInfo(userInfo);
		update();
	}
	
	public void setStatus(String status, OCSDatabase db) throws SQLException {
		userInfo.status = status;
		db.updateUserInfo(userInfo);
		update();
	}

}
