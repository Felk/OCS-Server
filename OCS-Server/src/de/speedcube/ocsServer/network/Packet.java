package de.speedcube.ocsServer.network;

import java.util.ArrayList;
import java.util.HashMap;

import de.speedcube.ocsServer.DNFile.DNFile;

public abstract class Packet {

	protected DNFile data;
	public byte[] packedData;
	public byte[] networkBuffer;
	public int channel = DEFAULT_CHANNEL;
	public boolean packed = false;

	public static final int DEFAULT_CHANNEL = 0;
	public static final int LOBBY_CHANNEL = 1;
	public static final int WORLD_CHANNEL = 2;
	public static final int PLAYER_CHANNEL = 3;
	public static final int ENTITY_CHANNEL = 3;

	private static int biggestID = 0; //0 is reserved for PacketConnectionInfo

	protected static ArrayList<Class<? extends Packet>> packets = new ArrayList<Class<? extends Packet>>();
	
	public Packet() {
		
	}

	public static Class<? extends Packet> getPacket(int id) {
		return packets.get(id);
	}

	public abstract void pack();

	public void packInNetworkBuffer() {
		networkBuffer = new byte[packedData.length + 8];
		System.arraycopy(packedData, 0, networkBuffer, 8, packedData.length);
		networkBuffer[0] = (byte) ((packedData.length & 0xff000000) >> 24);
		networkBuffer[1] = (byte) ((packedData.length & 0xff0000) >> 16);
		networkBuffer[2] = (byte) ((packedData.length & 0xff00) >> 8);
		networkBuffer[3] = (byte) ((packedData.length & 0xff));

		networkBuffer[4] = (byte) ((getPacketId() & 0xff000000) >> 24);
		networkBuffer[5] = (byte) ((getPacketId() & 0xff0000) >> 16);
		networkBuffer[6] = (byte) ((getPacketId() & 0xff00) >> 8);
		networkBuffer[7] = (byte) ((getPacketId() & 0xff));
	}

	public abstract void unpack();

	public DNFile getData() {
		return data;
	}

	public abstract String getName();
	public abstract int getPacketId();

	private static void registerPacket(Class<? extends Packet> packet) {
		packets.add(packet);
	}

	static {
		registerPacket(PacketConnectionInfo.class);//has to be at position 0
	}
}
