package de.speedcube.ocsServer.network;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import de.speedcube.ocsUtilities.packets.Packet;
import de.speedcube.ocsUtilities.packets.PacketConnectionInfo;

public class ReceiveThread extends Thread {
	private Socket socket;
	private Client client;
	private Object toNotify;
	private boolean connectionClosed = false;
	private LinkedList<Packet> data = new LinkedList<Packet>();

	public ReceiveThread(Socket socket, Client client, Object toNotify) {
		setName("ReceiveThread");
		this.socket = socket;
		this.client = client;
		this.toNotify = toNotify;
	}

	@Override
	public void run() {
		DataInputStream in;

		try {
			in = new DataInputStream(socket.getInputStream());
			byte[] buffer = null;

			while (!connectionClosed) {
				int length = in.readInt();
				buffer = new byte[length];
				int packetID = in.readInt();

				int receivedBytes = 0;

				while (receivedBytes < buffer.length) {
					receivedBytes += in.read(buffer, receivedBytes, buffer.length - receivedBytes);
				}

				if (packetID >= Packet.getPacketSize() || packetID < 0) {
					System.out.println("Client sent invalid packetID, aborting connection.");
					stopThread();
					client.stopClient();
					continue;
				}
				Packet receivedPacket = Packet.getPacket(packetID).newInstance();
				System.out.println("received packet: " + receivedPacket.getName() + " ID: " + receivedPacket.packetID + " (" + (receivedBytes) + " bytes)");
				receivedPacket.packedData = buffer;
				receivedPacket.unpack();

				// hardcoded PacketID for connectionData (0)
				if (!client.connectionInfoReceived && packetID == 0) {
					client.setConnectionInfo((PacketConnectionInfo) receivedPacket);
				} else {
					synchronized (data) {
						data.add(receivedPacket);
						if (toNotify != null) synchronized (toNotify) {
							toNotify.notify();
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			client.stopClient();
		}
	}

	public ArrayList<Packet> getData(int channel) {
		synchronized (data) {
			ArrayList<Packet> returnList = new ArrayList<Packet>();
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).channel == channel) {
					returnList.add(data.get(i));
					data.remove(i);
					i--;
				}
			}
			return returnList;
		}
	}

	public void stopThread() {
		connectionClosed = true;
	}
}
