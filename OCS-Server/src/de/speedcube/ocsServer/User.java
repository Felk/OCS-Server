package de.speedcube.ocsServer;

import de.speedcube.ocsServer.network.Client;
import de.speedcube.ocsUtilities.Userranks;
import de.speedcube.ocsUtilities.packets.PacketLogout;

public class User {

	public Userlist userlist;
	
	public int rank = Userranks.GUEST;
	private Client client;
	public int userId;
	public String username;
	public String salt = "";
	
	public User(Client client) {
		setClient(client);
	}
	
	public User(Userlist userlist, int id, String username, int rank) {
		this.userlist = userlist;
		this.userId = id;
		this.username = username;
		this.rank = rank;
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
