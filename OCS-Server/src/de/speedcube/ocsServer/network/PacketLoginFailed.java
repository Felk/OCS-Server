package de.speedcube.ocsServer.network;

import de.speedcube.ocsServer.DNFile.DNFile;

public class PacketLoginFailed extends Packet {
	public String msg;

	@Override
	public void pack() {
		data = new DNFile("");
		//username = GameOptions.instance.getOption("playerName");

		data.addNode("msg", msg);
		
		packedData = data.toByteArray();
	}

	@Override
	public void unpack() {
		data = new DNFile("");
		data.fromByteArray(packedData);

		msg = data.getString("msg");
	}

	@Override
	public String getName() {
		return "LoginFailed";
	}

}
