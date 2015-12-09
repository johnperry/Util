/*---------------------------------------------------------------
*  Copyright 2011 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.rsna.util.LdapUtil;
import org.rsna.util.StringUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * A class to extend the org.rsna.server.UsersLdapFileImpl class
 * for testing.
 */
public class UsersTestLdapFileImpl extends UsersLdapFileImpl {

	static final Logger logger = Logger.getLogger(UsersTestLdapFileImpl.class);

	/**
	 * Constructor.
	 * @param element the Server element from the configuration.
	 */
	public UsersTestLdapFileImpl(Element element) {
		super(element);
	}

	/**
	 * Accept any user whose username exists in the users.xml file,
	 * regardless of the password.
	 */
	public User authenticate(String username, String password) {
		return getUser(username);
	}

}