package de.speedcube.ocsServer.parties;

public class ResultSet {

	private int userID;
	private int[] times;
	private int average;
	
	public ResultSet(int userID, int[] times, int average) {
		this.userID = userID;
		this.times = times;
		this.average = average;
	}

	public int getUserID() {
		return userID;
	}
	
	public int[] getTimes() {
		return times;
	}

	public int getAverage() {
		return average;
	}
	
}
