/*---------------------------------------------------------------
*  Copyright 2011 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.net.URI;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.rsna.util.OpenAMUtil;
import org.rsna.util.StringUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * A class to extend the org.rsna.server.UsersXmlFileImpl class
 * for managing the users.xml file while authenticating credentials
 * via an LDAP server.
 */
public class UsersOpenAMImpl extends Users {

	static final Logger logger = Logger.getLogger(UsersOpenAMImpl.class);

	volatile String openAMURL = "";
	volatile String ssoCookieName = "";

	/**
	 * Constructor.
	 * @param element the Server element from the configuration.
	 */
	public UsersOpenAMImpl(Element element) {
		Element openAM = XmlUtil.getFirstNamedChild(element, "OpenAM");
		if (openAM != null) {
			openAMURL = openAM.getAttribute("openAMURL").trim();
			openAMURL = StringUtil.replace( openAMURL, System.getProperties(), true ).trim();
			URL url = null;
			if (!openAMURL.equals("")) {
				try { 
					URI uri = new URI(openAMURL);
					url = uri.toURL();
				}
				catch (Exception ex) { url = null; }
			}
			if (url == null) logger.warn("Invalid openAMURL attribute");
			else logger.info("OpenAM URL: "+url);
		}
		else logger.warn("Missing OpenAM element - no parameters are available for initialization");
	}

	/**
	 * Check whether a request comes from a user known to the OpenAM system.
	 * @return the user who matches the credentials, or null if no matching user exists.
	 */
	public User validate(HttpRequest req) {
		String token = req.getCookie(ssoCookieName);
		boolean isValid = OpenAMUtil.validate(openAMURL, token);
		logger.debug("Validating \""+token+"\";  result = "+isValid);
		if (isValid) {
			String attributes = OpenAMUtil.getAttributes(openAMURL, token);
			logger.debug("Attributes:\n"+attributes);
			Hashtable<String,LinkedList<String>> attrs = OpenAMUtil.parseAttributes(attributes);
			LinkedList<String> usernameList = attrs.get("uid");
			if (usernameList != null) {
				String username = usernameList.getFirst();
				User user = new User(username, "");
				LinkedList<String>roleList = attrs.get(OpenAMUtil.ROLESKEY);
				if (roleList != null) {
					for (String role : roleList) {
						user.addRole(role);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Created user\n"+XmlUtil.toPrettyString(user.getXML(true)));
				}
				return user;
			}
			else logger.debug("uid attribute is missing");
		}
		return null;
	}

	/**
	 * Get the URL of the OpenAM system's login servlet.
	 * @return the URL of the OpenAM system's login servlet.
	 */
	public String getLoginURL(String redirectURL) {
		return OpenAMUtil.getLoginURL(openAMURL, redirectURL);
	}

	/**
	 * Get the URL of the OpenAM system's logout servlet.
	 * @return the URL of the OpenAM system's logout servlet.
	 */
	public String getLogoutURL() {
		return OpenAMUtil.getLogoutURL(openAMURL);
	}

	/**
	 * Indicate that the OpenAM system supports single signon.
	 * @return true.
	 * false otherwise.
	 */
	public boolean supportsSSO() {
		return true;
	}

	/**
	 * Get the OpenAM system's SSO cookie name.
	 * @return the name of the OpenAM system's SSO cookie.
	 */
	public String getSSOCookieName() {
		if (ssoCookieName.equals("")) {
			ssoCookieName = OpenAMUtil.getCookieName(openAMURL);			
			if (!ssoCookieName.equals("")) {
				logger.info("OpenAM SSO cookie name: "+ssoCookieName);
			}
			else logger.warn("Unable to obtain the SSO cookie name");
		}
		return ssoCookieName;
	}

}