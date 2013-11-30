package de.speedcube.ocsServer.network;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class ReceiveThread extends Thread {
	private Socket socket;
	private Client client;
	private boolean connectionClosed = false;
	private LinkedList<Packet> data = new LinkedList<Packet>();

	public ReceiveThread(Socket socket, Client client) {
		setName("ReceiveThread");
		this.socket = socket;
		this.client = client;
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

				Packet receivedPacket = Packet.getPacket(packetID).newInstance();
				//if (GameOptions.instance.getBoolOption("showNetworkTraffic")) System.out.println("received packet: " + receivedPacket.getName() + " (" + receivedBytes + "/" + buffer.length + " bytes)");
				receivedPacket.packedData = buffer;
				receivedPacket.unpack();

				//hardcoded PacketID for connectionData (0)
				if (!client.connectionInfoReceived && packetID == 0) {
					client.setConnectionInfo((PacketConnectionInfo) receivedPacket);
				} else {
					synchronized (data) {
						data.add(receivedPacket);
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
			if (data.size() > 0) {
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

		return null;
	}

	public void stopThread() {
		connectionClosed = true;
	}
}
