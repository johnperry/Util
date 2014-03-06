/*---------------------------------------------------------------
*  Copyright 2014 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Static utility methods for accessing an OpenAM server.
 */
public class OpenAMUtil {

	static final String stringEquals = "string=";
	static final String booleanEquals = "boolean=";
	static final String nameEquals = "userdetails.attribute.name=";
	static final String valueEquals = "userdetails.attribute.value=";

	/**
	 * Get the OpenAM token cookie name.
	 * @param baseURL the base url of the OpenAM server, including the protocol, host, and port
	 * @return the name of the session cookie, or the empty string if the name cannot be obtained.
	 */
    public static String getCookieName(String baseURL) {
		String cookieName = doGet( baseURL + "/openam/identity/getCookieNameForToken" );
		if (cookieName.startsWith(stringEquals)) {
			cookieName = cookieName.substring(stringEquals.length());
		}
		else cookieName = "";
		return cookieName;
	}

	/**
	 * Validate a token.
	 * @param baseURL the base url of the OpenAM server, including the protocol, host, and port
	 * @param token the value of the token cookie to validate
	 * @return true if the token is valid; false otherwise.
	 */
    public static boolean validate(String baseURL, String token) {
		String result = doPost( baseURL + "/openam/identity/isTokenValid", "tokenid="+token );
		if (result.startsWith(booleanEquals)) {
			result = result.substring(booleanEquals.length());
		}
		return result.toLowerCase().equals("true");
	}

	/**
	 * Get the attributes.associated with a token
	 * @param baseURL the base url of the OpenAM server, including the protocol, host, and port
	 * @param token the value of the token cookie
	 * @return the attributes.
	 */
    public static String getAttributes(String baseURL, String token) {
		String result = doPost( baseURL + "/openam/identity/attributes", "subjectid="+token );
		return result;
	}

	/**
	 * Convert the attributes string to a hashtable.
	 * @return the attributes in a hashtable
	 */
    public static Hashtable<String,LinkedList<String>> parseAttributes(String attributes) {
		String[] lines = attributes.split("\n");
		String name = null;
		LinkedList<String> values = null;
		Hashtable<String,LinkedList<String>> attrs = new Hashtable<String,LinkedList<String>>();
		for (String line : lines) {
			line = line.trim();
			if (line.startsWith(nameEquals)) {
				name = line.substring(nameEquals.length());
				values = new LinkedList<String>();
				attrs.put(name, values);
			}
			else if (line.startsWith(valueEquals) && (values != null)) {
				values.add(line.substring(valueEquals.length()));
			}
		}
		return attrs;
	}

	/**
	 * Check whether a token has a specific role
	 * @param baseURL the base url of the OpenAM server, including the protocol, host, and port
	 * @param role the role
	 * @param token the value of the token cookie
	 * @return true if the token has the role; false otherwise.
	 */
    public static boolean authorize(String baseURL, String role, String token) {
		String postBody = "uri="+role+"&action=GET&subjectid="+token;
		String result = doPost( baseURL + "/openam/identity/authorize", postBody );
		if (result.startsWith(booleanEquals)) {
			result = result.substring(booleanEquals.length());
		}
		return result.toLowerCase().equals("true");
	}

	/**
	 * Launch the browser and go to the login page
	 * @param baseURL the base url of the OpenAM server, including the protocol, host, and port
	 * @return the result.
	 */
    public static String login(String baseURL, String redirectURL) {
		String result = "OK";
		try {
			URL url = new URL(baseURL + "/openam/UI/Login?goto="+redirectURL);
			Desktop.getDesktop().browse( url.toURI() );
		}
		catch (Exception ignore) { result = "Unable to launch browser."; }
		return result;
	}

	/**
	 * Log out - this probably doesn't work
	 * @param baseURL the base url of the OpenAM server, including the protocol, host, and port
	 * @return the result.
	 */
    public static String logout(String baseURL) {
		String result = doGet( baseURL + "/openam/UI/Logout" );
		return result;
	}

	private static String doGet(String urlString) {
		String text = "";
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = HttpUtil.getConnection(url);
			conn.setRequestMethod("GET");
			conn.setDoOutput(false);
			conn.connect();
			if (conn.getResponseCode() == 200) {
				text = FileUtil.getText( conn.getInputStream() );
			}
		}
		catch (Exception unable) { }
		return text.trim();
	}

	private static String doPost(String urlString, String postBody) {
		String text = "";
		BufferedWriter writer = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = HttpUtil.getConnection(url);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			conn.connect();
			writer = new BufferedWriter( new OutputStreamWriter( conn.getOutputStream(), FileUtil.utf8 ) );
			writer.write(postBody);
			writer.flush();
			if (conn.getResponseCode() == 200) {
				text = FileUtil.getText( conn.getInputStream() );
			}
		}
		catch (Exception unable) { }
		finally { FileUtil.close(writer); }
		return text.trim();
	}

}
