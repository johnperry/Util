/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.lang.reflect.Constructor;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * A singleton class for managing users.
 * The getInstance method of this class is used to obtain an instance that
 * matches the standard implementation. The purpose of this approach is to make
 * it easy to switch methods of managing users without affecting other code.
 */
public abstract class Users {

	static final Logger logger = Logger.getLogger(Users.class);

	protected static Users users = null;

	/**
	 * Protected class constructor to prevent anyone from instantiating this class.
	 */
    protected Users() {	}

	/**
	 * Get a Users instance from the specified class.
	 * @param className the fully qualified name of the class to
	 * instantiate if a Users instance does not already exist.
	 * If the className is null or blank, no class is instantiated.
	 * @param element the element from which to obtain any required
	 * configuration parameters
	 * @return the Users object or null if it cannot be instantiated from
	 * the supplied className.
	 */
	public static synchronized Users getInstance(String className, Element element) {
		if ((users == null) && (className != null) && !className.trim().equals("")) {
			try {
				Class theClass = Class.forName(className);
				Class[] signature = { Element.class };
				Constructor constructor = theClass.getConstructor(signature);
				Object[] args = { element };
				users = (Users)constructor.newInstance(args);

				//Add the administrative roles
				users.addRole("admin");
				users.addRole("shutdown");
			}
			catch (Exception ex) {
				logger.warn("Unable to load the Users class: " + className, ex);
			}
		}
		return users;
	}

	/**
	 * Get the current Users instance. This method does not instantiate
	 * a Users subclass. If it has not already been instantiated, null
	 * is returned.
	 * @return the Users object or null if no Users instance exists.
	 */
	public static synchronized Users getInstance() {
		return users;
	}

	/**
	 * Get a specific user.
	 * This implementation returns null. Implementations that
	 * manage users must override this method.
	 * @param username the plaintext username
	 * @return the user or null if unable.
	 */
	public User getUser(String username) {
		return null;
	}

	/**
	 * Convert a plaintext password to the form used by this implementation.
	 * This implementation returns the password unmodified. Implementations
	 * that store passwords in encrypted forms must override this method.
	 * @param password the plaintext password
	 * @return the password converted to the form used by this implementation.
	 */
	public String convertPassword(String password) {
		return password;
	}

	/**
	 * Check whether a set of credentials matches a user in the system.
	 * This implementation returns null, indicating that users cannot
	 * be authenticated locally. Users class extensions that can authenticate
	 * users locally must override this method.
	 * @param username the plaintext username
	 * @param password the plaintext password
	 * @return the user who matches the credentials, or null if no matching user exists.
	 */
	public User authenticate(String username, String password) {
		return null;
	}

	/**
	 * Check whether a request comes from a user known to an external system.
	 * This implementation returns null, indicating that no external system can
	 * associate a user with this request. Single Sign On implementations must
	 * override this method.
	 * @param req the request to use in determining whether the user is known to 
	 * an external system.
	 * @return the user who matches the request, or null if no matching user exists.
	 */
	public User validate(HttpRequest req) {
		return null;
	}

	/**
	 * Get the URL of the external system's login servlet that provides authentication.
	 * This implementation returns the empty string, indicating that no external system provides
	 * authentication. Single Sign On implementations must override this method.
	 * @param redirectURL the URL of the external authentication system
	 * @return the URL of the external authentication system's login servlet, or
	 * the empty string if authentication is done locally.
	 */
	public String getLoginURL(String redirectURL) {
		return "";
	}

	/**
	 * Get the URL of the external system's logout servlet.
	 * This implementation returns the empty string, indicating that no external system
	 * provides authentication. Single Sign On implementations must override this method.
	 * @return the URL of the external authentication system's logout servlet, or
	 * the empty string if authentication is done locally.
	 */
	public String getLogoutURL() {
		return "";
	}

	/**
	 * Get whether the external system supports single signon.
	 * This implementation returns false, indicating that SSO is not supported.
	 * Single Sign On implementations must override this method.
	 * @return true if the external authentication system supports single signon;
	 * false otherwise.
	 */
	public boolean supportsSSO() {
		return false;
	}

	/**
	 * Get the name of the external system's single signon cookie.
	 * This implementation returns null, indicating that there is no SSO cookie.
	 * Single Sign On implementations must override this method.
	 * @return the name of the external authentication system's single signon cookie, or
	 * the empty string if there is no cookie name available.
	 */
	public String getSSOCookieName() {
		return "";
	}

	/**
	 * Get an alphabetized array of usernames.
	 * This method returns an empty array. Implementations that
	 * manage users must override this method.
	 * @return the array of usernames or a zero-length array if unable.
	 */
	public String[] getUsernames() {
		return new String[0];
	}

	/**
	 * Add a role to the list of standard roles.
	 * This method does nothing. Implementations
	 * that manage roles must override this method.
	 * @param role the name of the role.
	 */
	public void addRole(String role) { }

}
