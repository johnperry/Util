/*---------------------------------------------------------------
*  Copyright 2011 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.rsna.util.LdapUtil;
import org.rsna.util.StringUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * A class to extend the org.rsna.server.UsersXmlFileImpl class
 * for managing the users.xml file while authenticating credentials
 * via an LDAP server.
 */
public class UsersLdapFileImpl extends UsersXmlFileImpl {

	static final Logger logger = Logger.getLogger(UsersLdapFileImpl.class);

	String initialContextFactory = "";
	String providerURL = "";
	String securityAuthentication = "simple";
	String securityPrincipal = "";
	String lookup = "";

	/**
	 * Constructor.
	 * @param element the Server element from the configuration.
	 */
	public UsersLdapFileImpl(Element element) {
		super(element);
		initialContextFactory = element.getAttribute("initialContextFactory");
		providerURL = element.getAttribute("providerURL");
		securityAuthentication = element.getAttribute("securityAuthentication");
		securityPrincipal = element.getAttribute("securityPrincipal");
		lookup = element.getAttribute("lookup");

		//Make sure we have an admin user who is known to LDAP
		String ldapAdmin = element.getAttribute("ldapAdmin");
		User admin = getUser(ldapAdmin);
		if (admin == null) {
			admin = new User(ldapAdmin, "");
			logger.info("\""+ldapAdmin+"\" admin user created");
		}
		admin.addRole("admin");
		addUser(admin);
	}

	/**
	 * Check whether a set of credentials match a user in the system.
	 * To be authenticated, a user must appear in the users.xml file
	 * <b>and</b> the user's credentials must be accepted by the LDAP server.
	 * @return true if the credentials match a user; false otherwise.
	 */
	public User authenticate(String username, String password) {

		User user = getUser(username);
		if (user != null) {

			Properties props = new Properties();
			props.setProperty( "username", username );
			String principal = StringUtil.replace( securityPrincipal, props );

			if ( LdapUtil.authenticate(
							initialContextFactory,
							providerURL,
							securityAuthentication,
							principal,
							password,
							lookup) ) {
				return user;
			}
		}
		return null;
	}

}