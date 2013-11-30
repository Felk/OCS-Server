package de.speedcube.ocsUtilities.network;

import de.speedcube.ocsServer.OCSServer;
import de.speedcube.ocsUtilities.DNFile.DNFile;

public class PacketConnectionInfo extends Packet {
	public String version;
	public String salt = "";

	@Override
	public void pack() {
		data = new DNFile("");
		version = OCSServer.version;

		data.addNode("version", version);
		data.addNode("salt", salt);

		packedData = data.toByteArray();
	}

	@Override
	public void unpack() {
		data = new DNFile("");
		data.fromByteArray(packedData);

		version = data.getString("version");
		salt = data.getString("salt");
	}

	@Override
	public String getName() {
		return "ConnectionInfo";
	}

}
