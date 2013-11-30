package de.speedcube.ocsServer.network;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsServer.DNFile.DNFile;

public class PacketConnectionInfo extends Packet {
	public String version;

	@Override
	public void pack() {
		data = new DNFile("");
		version = OCSServer.version;

		data.addNode("version", version);

		packedData = data.toByteArray();
	}

	@Override
	public void unpack() {
		data = new DNFile("");
		data.fromByteArray(packedData);

		version = data.getString("version");
	}

	@Override
	public String getName() {
		return "ConnectionInfo";
	}

}
