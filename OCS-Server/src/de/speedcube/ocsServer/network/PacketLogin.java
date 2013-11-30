package de.speedcube.ocsServer.network;

import de.speedcube.ocsServer.DNFile.DNFile;

public class PacketLogin extends Packet {
	public String username;

	@Override
	public void pack() {
		data = new DNFile("");
		//username = GameOptions.instance.getOption("playerName");

		data.addNode("username", username);
		
		packedData = data.toByteArray();
	}

	@Override
	public void unpack() {
		data = new DNFile("");
		data.fromByteArray(packedData);

		username = data.getString("username");
	}

	@Override
	public String getName() {
		return "Login";
	}

}
