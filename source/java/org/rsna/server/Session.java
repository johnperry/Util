/*---------------------------------------------------------------
 *  Copyright 2015 by the Radiological Society of North America
 *
 *  This source software is released under the terms of the
 *  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
 *----------------------------------------------------------------*/

package org.rsna.server;

import java.math.BigInteger;
import java.security.*;

/**
 * Class to encapsulate a session.
 */
public class Session implements Comparable<Session> {
	public long lastAccess;
	public final User user;
	public final String ipAddress;
	public final String id;

	//Construct a Session
	public Session(User user, String ipAddress) throws Exception {
		lastAccess = System.currentTimeMillis();
		this.user = user;
		this.ipAddress = ipAddress;
		id = getSessionID(user.getUsername(), ipAddress);
	}
	
	public User getUser() {
		return user;
	}

	public boolean appliesTo(HttpRequest req) {
		long timeout = Authenticator.getInstance().getSessionTimeout();
		boolean ok = req.getRemoteAddress().equals(ipAddress);
		if (ok && (timeout > 0)) {
			ok &= ((System.currentTimeMillis() - lastAccess) < timeout);
		}
		return ok;
	}
	
	public boolean hasTimedOut() {
		long timeout = Authenticator.getInstance().getSessionTimeout();
		return (timeout > 0) && ((System.currentTimeMillis() - lastAccess) >= timeout);
	}

	public void recordAccess() {
		lastAccess = System.currentTimeMillis();
	}
	
	public int compareTo(Session s) {
		long dif = lastAccess - s.lastAccess;
		return (dif>0) ? 1 : ((dif==0) ? 0 : -1);
	}

	//Make a session ID by hashing the username, the IP address and the current time.
	private String getSessionID(String username, String ipAddress) throws Exception {
		String string = username + ":" + ipAddress + ":" + System.currentTimeMillis();
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		byte[] hashed = messageDigest.digest(string.getBytes("UTF-8"));
		BigInteger bi = new BigInteger(1, hashed);
		return bi.toString();
	}
}
