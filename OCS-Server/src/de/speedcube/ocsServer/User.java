package de.speedcube.ocsServer;

import java.sql.SQLException;
import java.util.ArrayList;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsServer.parties.Party;
import de.speedcube.ocsServer.sql.OCSDatabase;
import de.speedcube.ocsUtilities.packets.PacketChannelEnter;
import de.speedcube.ocsUtilities.packets.PacketUserInfo;
import de.speedcube.ocsUtilities.UserInfo;

public class User {

	private final Userlist userlist;
	private Client client;
	private String salt = "";
	public UserInfo userInfo = new UserInfo();
	private ArrayList<String> channels = new ArrayList<String>();

	public User(Client client, Userlist userlist, UserInfo userInfo) {
		setClient(client);
		this.userlist = userlist;
		this.userInfo = userInfo;
	}

	public User(Client client, Userlist userlist, int id, String username, int rank, int color, String status) {
		this(client, userlist, new UserInfo(id, username, rank, color, status));
	}

	/*private void remove() throws NullPointerException {
		if (userlist == null) {
			throw new NullPointerException();
		}
		userlist.logoutUser(this);
		userlist.updateUserlist();
		userInfo = new UserInfo();
	}*/

	public void closeConnection() {
		if (client != null) client.stopClient();
	}

	/*public void logout() {
		if (getClient(true) != null) {
			PacketLogout packetLogout = new PacketLogout();
			packetLogout.msg = "system.msg.logout";
			client.sendPacket(packetLogout);

		}
		remove();
	}*/

	public void update() {
		PacketUserInfo packet = new PacketUserInfo();
		packet.addUserInfo(userInfo);
		userlist.broadcastData(packet);
		userlist.updateJsonString();
	}

	public void updateEverything(OCSServer server) {
		// Userlist, UserInfos, Parties, PartyInfos
		PacketUserInfo pUserInfo = new PacketUserInfo();
		for (User u : userlist.getUsers())
			pUserInfo.addUserInfo(u.userInfo);
		client.sendPacket(pUserInfo);
		client.sendPacket(server.parties.toPacket());
		for (Party p : server.parties.getParties())
			client.sendPacket(p.toPacket());
	}

	private void setClient(Client c) {
		this.client = c;
		if (c != null) c.setUser(this);
	}

	public Client getClient(boolean mustBeConnected) {
		if (client != null) if (mustBeConnected && !client.connected) return null;
		return client;
	}

	public Client getClient() {
		return getClient(false);
	}

	public void leaveChannel(String channel) {
		channels.remove(channel);
		update();
	}

	public boolean enterChannel(String channel) {
		if (!channels.contains(channel)) channels.add(channel);
		PacketChannelEnter p = new PacketChannelEnter();
		p.chatChannel = channel;
		getClient().sendPacket(p);
		update();
		return true;
	}

	public boolean isInChannel(String channel) {
		return channels.contains(channel);
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

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public ArrayList<String> getChannels() {
		return channels;
	}

	public void logout() {
		userlist.logoutUser(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof User)) return false;
		User u = (User) o;
		if (u.userInfo == userInfo) return true;
		if (u.userInfo == null) return false;
		if (u.userInfo.userID == userInfo.userID) return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		if (userInfo == null) return 0;
		return userInfo.userID;
	}

}
