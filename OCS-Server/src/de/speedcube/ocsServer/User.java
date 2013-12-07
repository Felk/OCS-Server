package de.speedcube.ocsServer;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsUtilities.packets.PacketLogout;
import de.speedcube.ocsUtilities.UserInfo;

public class User {

	public Userlist userlist;
	private Client client;
	public String salt = "";
	public UserInfo userInfo = new UserInfo();
	
	public User(Client client) {
		setClient(client);
	}
	
	public User(Userlist userlist, int id, String username, int rank, int color, String status) {
		this.userInfo = new UserInfo(id, username, rank, color, status);
		this.userlist = userlist;
	}
	
	public void remove() {
		if (userlist != null) userlist.getUsers().remove(this);
	}

	public void kick() {
		PacketLogout packetLogout = new PacketLogout();
		packetLogout.msg = "You got kicked!";
		client.sendPacket(packetLogout);
		client.stopClient();
		remove();
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

}
