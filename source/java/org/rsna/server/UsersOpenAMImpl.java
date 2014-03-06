/*---------------------------------------------------------------
*  Copyright 2011 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.rsna.util.OpenAMUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * A class to extend the org.rsna.server.UsersXmlFileImpl class
 * for managing the users.xml file while authenticating credentials
 * via an LDAP server.
 */
public class UsersOpenAMImpl extends Users {

	static final Logger logger = Logger.getLogger(UsersOpenAMImpl.class);

	String openAMURL = "";
	String cookieName = "";

	/**
	 * Constructor.
	 * @param element the Server element from the configuration.
	 */
	public UsersOpenAMImpl(Element element) {
		Element openAM = XmlUtil.getFirstNamedChild(element, "OpenAM");
		if (openAM != null) {
			openAMURL = openAM.getAttribute("openAMURL").trim();
			URL url = null;
			if (!openAMURL.equals("")) {
				try { url = new URL(openAMURL); }
				catch (Exception ex) { url = null; }
			}
			if (url == null) logger.warn("Invalid openAMURL attribute");
			cookieName = OpenAMUtil.getCookieName(openAMURL);
			if (!cookieName.equals("")) {
				Authenticator.getInstance().setSSOCookieName(cookieName);
				logger.info("OpenAM SSO cookie name: "+cookieName);
			}
			else logger.warn("Unable to obtain the SSO cookie name");
		}
		else logger.warn("Missing OpenAM element - no parameters are available for initialization");
	}

	/**
	 * Check whether a request comes from a user known to the OpenAM system.
	 * @return the user who matches the credentials, or null if no matching user exists.
	 */
	public User validate(HttpRequest req) {
		String token = req.getCookie(cookieName);
		boolean isValid = OpenAMUtil.validate(openAMURL, token);
		logger.debug("Validating \""+token+"\";  result = "+isValid);
		if (isValid) {
			String attributes = OpenAMUtil.getAttributes(openAMURL, token);
			Hashtable<String,LinkedList<String>> attrs = OpenAMUtil.parseAttributes(attributes);
			LinkedList<String> usernameList = attrs.get("uid");
			if (usernameList != null) {
				String username = usernameList.getFirst();
				User user = new User(username, "");
				LinkedList<String>roleList = attrs.get("role");
				if (roleList != null) {
					for (String role : roleList) {
						user.addRole(role);
					}
				}
				//*********************************
				// FOR TESTING ONLY
				//*********************************
				user.addRole("admin");
				user.addRole("import");
				user.addRole("export");
				//*********************************
				logger.debug("Created user\n"+XmlUtil.toPrettyString(user.getXML(true)));
				return user;
			}
			else logger.debug("uid attribute is missing");
		}
		return null;
	}

}