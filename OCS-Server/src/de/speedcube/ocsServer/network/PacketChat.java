package de.speedcube.ocsServer.network;

import de.speedcube.ocsServer.DNFile.DNFile;

public class PacketChat extends Packet {
	public String text;

	@Override
	public void pack() {
		data = new DNFile("");
		data.addNode("text", text);
		packedData = data.toByteArray();
	}

	@Override
	public void unpack() {
		data = new DNFile("");
		data.fromByteArray(packedData);
		text = data.getString("text");
	}

	@Override
	public String getName() {
		return "Chat";
	}

}
