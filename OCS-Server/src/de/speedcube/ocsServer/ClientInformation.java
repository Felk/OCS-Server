package de.speedcube.ocsServer;

import de.speedcube.ocsUtilities.Userranks;

public class ClientInformation {

	public int rank = Userranks.GUEST;
	public int userId;
	public String username;
	public String salt = "";

	public ClientInformation() {
		this.rank = Userranks.GUEST;
	}
	
	public ClientInformation(int id, String username, int rank) {
		this.userId = id;
		this.username = username;
		this.rank = rank;
	}

}
