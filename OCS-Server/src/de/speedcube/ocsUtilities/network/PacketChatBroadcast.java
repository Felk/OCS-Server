package de.speedcube.ocsUtilities.network;

import de.speedcube.ocsUtilities.DNFile.DNFile;

public class PacketChatBroadcast extends Packet {
	public String text;
	public int userId;

	@Override
	public void pack() {
		data = new DNFile("");
		data.addNode("text", text);
		data.addNode("id", userId);
		packedData = data.toByteArray();
	}

	@Override
	public void unpack() {
		data = new DNFile("");
		data.fromByteArray(packedData);
		text = data.getString("text");
		userId = data.getInt("id");
	}

	@Override
	public String getName() {
		return "ChatBroadcast";
	}

}
