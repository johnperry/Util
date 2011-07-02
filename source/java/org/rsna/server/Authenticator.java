/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.math.BigInteger;
import java.security.*;
import java.util.Arrays;
import java.util.Hashtable;
import org.apache.log4j.Logger;
import org.rsna.util.FileUtil;
import org.rsna.util.Base64;

/**
 * The standard authenticator singleton class for the server.
 */
public class Authenticator {

	static final Logger logger = Logger.getLogger(Authenticator.class);

	static Authenticator authenticator = null;

	Hashtable<String,Session> sessions = null;
	long timeout = 1 * 60 * 60 * 1000; //default session timeout in ms = 1 hour

	/**
	 * The protected constructor to prevent instantiation of
	 * the class except through the getInstance() method.
	 */
	protected Authenticator() {
		sessions = new Hashtable<String,Session>();
	}

	/**
	 * Get the Authenticator instance, creating it if it does not exist.
	 */
	public static Authenticator getInstance() {
		if (authenticator == null) authenticator = new Authenticator();
		return authenticator;
	}

	/**
	 * Authenticate the user from an HttpRequest.
	 * This method tests for the session cookie.
	 * If the session cookie is present, it uses it to find
	 * the session. If the session exists and applies to the
	 * request (e.g., it is from the same IP address, the same MAC
	 * address, and hasn't timed out), it sets the User to the User
	 * of the session. In all other cases, it sets the User to null.
	 * @param req the request.
	 * @return the authenticated user, or null if the user cannot be authenticated.
	 */
    public User authenticate(HttpRequest req) {

		Session session;
		//First try the session cookie
		String id = req.getCookie("RSNASESSION");
/**/	logger.debug("Authenticator.authenticate: RSNASESSION cookie: "+id);
		if ( (id != null) && ((session=sessions.get(id)) != null) && session.appliesTo(req) ) {
/**/		logger.debug("...session accepted");
			session.recordAccess();
			return session.user;
		}
		//No session cookie, or cookie is not valid; check the headers.
		//First try the RSNA header. This header is not encoded.
		Users users = Users.getInstance();
		String credentials = req.getHeader("RSNA");
		if (credentials != null) {
			String[] up = credentials.trim().split(":");
			if (up.length == 2) {
				User user = users.getUser(up[0]);
				if ((user != null) && user.getPassword().equals(up[1])) {
					return user;
				}
			}
		}
		//Next try the Authorization header.
		credentials = req.getHeader("Authorization");
		if (credentials != null) {
			String type = "basic";
			credentials = credentials.trim();
			if (credentials.toLowerCase().startsWith(type)) {
				credentials = credentials.substring(type.length()).trim();
				try { credentials = new String(Base64.decode(credentials), "UTF-8"); }
				catch (Exception ex) { credentials = ""; } //make it fail
				String[] up = credentials.split(":");
				if (up.length == 2) {
					User user = users.getUser(up[0]);
					if ((user != null) && user.getPassword().equals(up[1])) {
						return user;
					}
				}
			}
		}
		//The user cannot be authenticated.
/**/	logger.debug("...authentication failed");
		return null;
	}

	/**
	 * Create a session for a User, storing the session
	 * cookie in the supplied HttpResponse as a Set-Cookie
	 * header.
	 * @param user the user for whom to create a session.
	 * @param req the response.
	 * @param res the response.
	 * @return true if the session was created; false otherwise.
	 */
    public boolean createSession(User user, HttpRequest req, HttpResponse res) {
		try {
			Session session = new Session(user, req.getRemoteAddress());
			sessions.put(session.id, session);
			res.setHeader("Set-Cookie", "RSNASESSION="+session.id);
			res.setHeader("Cache-Control", "no-cache=\"set-cookie\"");
/**/		logger.debug("Authenticator.createSession: session created: "+session.id);
			return true;
		}
		catch (Exception failed) {
/**/		logger.warn("Authenticator.createSession: unable to create session", failed);
			return false;
		}
	}

	/**
	 * Close a session if one is identified by a session
	 * cookie in the request. Closing a session deletes the
	 * session from the sessions hashtable, making the session
	 * unrecognizable by the authenticator. It also sets
	 * a dummy session cookie with Max-Age=0.
	 * @param req the request.
	 * @param res the response.
	 */
    public void closeSession(HttpRequest req, HttpResponse res) {
		//See if there is a cookie specifying an existing session.
		String session = req.getCookie("RSNASESSION");
		if (session != null) {
			//A session was specified. Remove it from the hashtable.
			sessions.remove(session);
			//Set a dummy session cookie that expires immediately.
			res.setHeader("Set-Cookie","RSNASESSION=NONE; Max-Age=0");
			res.setHeader("Cache-Control", "no-cache=\"set-cookie\"");
		}
	}

	//Class to encapsulate a session
	class Session {
		public long lastAccess;
		public User user;
		public String ipAddress;
		public String id;

		//Construct a Session
		public Session(User user, String ipAddress) throws Exception {
			lastAccess = System.currentTimeMillis();
			this.user = user;
			this.ipAddress = ipAddress;
			id = getSessionID(user.getUsername(), ipAddress);
		}

		public boolean appliesTo(HttpRequest req) {
			return ((System.currentTimeMillis() - lastAccess) < timeout)
					&& req.getRemoteAddress().equals(ipAddress);
		}

		public void recordAccess() {
			lastAccess = System.currentTimeMillis();
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
}
