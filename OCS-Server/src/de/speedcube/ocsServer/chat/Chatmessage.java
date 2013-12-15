package de.speedcube.ocsServer.chat;

import de.speedcube.ocsUtilities.packets.PacketChatBroadcast;

public class Chatmessage {

	private int userID;
	private String channel;
	private String text;
	private long timestamp;
	
	public Chatmessage(int userID, String channel, String text, long timestamp) {
		if (channel == null) channel = "";
		if (text == null) text = "";
		this.setUserID(userID);
		this.setChannel(channel);
		this.setText(text);
		this.setTimestamp(timestamp);
	}
	
	public PacketChatBroadcast toPacket() {
		PacketChatBroadcast packet = new PacketChatBroadcast();
		packet.userId = getUserID();
		packet.chatChannel = getChannel();
		packet.text = getText();
		packet.timestamp = getTimestamp();
		return packet;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
