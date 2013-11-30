package de.speedcube.ocsUtilities.network;

import de.speedcube.ocsUtilities.DNFile.DNFile;

public class PacketLogin extends Packet {
	public String username;
	public String password;

	@Override
	public void pack() {
		data = new DNFile("");
		//username = GameOptions.instance.getOption("playerName");

		data.addNode("username", username);
		data.addNode("password", password);
		
		packedData = data.toByteArray();
	}

	@Override
	public void unpack() {
		data = new DNFile("");
		data.fromByteArray(packedData);

		username = data.getString("username");
		password = data.getString("password");
	}

	@Override
	public String getName() {
		return "Login";
	}

}
