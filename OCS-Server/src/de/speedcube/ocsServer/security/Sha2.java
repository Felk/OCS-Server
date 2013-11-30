package de.speedcube.ocsServer.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Sha2 {
	
	public static String hash(String data) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(data.getBytes());
		return bytesToHex(md.digest());
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuffer result = new StringBuffer();
		for (byte byt : bytes)
			result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
		return result.toString();
	}
	
	public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
		return hash(salt+password);
	}
}
