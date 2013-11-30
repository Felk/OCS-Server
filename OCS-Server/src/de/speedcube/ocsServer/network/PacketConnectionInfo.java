package de.speedcube.ocsServer.network;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.DNFile.DNFile;

public class PacketConnectionInfo extends Packet {
	public String version;
	public String username;

	@Override
	public void pack() {
		data = new DNFile("");
		version = OCSServer.version;
		//username = GameOptions.instance.getOption("playerName");

		data.addNode("version", version);
		data.addNode("username", username);

		packedData = data.toByteArray();
	}

	@Override
	public void unpack() {
		data = new DNFile("");
		data.fromByteArray(packedData);

		version = data.getString("version");
		username = data.getString("username");
	}

	@Override
	public String getName() {
		return "ConnectionInfo";
	}

}
