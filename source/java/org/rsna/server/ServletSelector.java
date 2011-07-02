/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import org.apache.log4j.Logger;
import org.rsna.servlets.LoginServlet;
import org.rsna.servlets.Servlet;

/**
 * A class to select a servlet to process a request..
 */
public class ServletSelector {

	static final Logger logger = Logger.getLogger(ServletSelector.class);

	File root;
	boolean requireAuthentication;
	Hashtable<String,Class> servlets;

	/**
	 * Class constructor; creates a new instance of the ServletSelector
	 * using the first path element to select a Servlet,
	 * @param root the root directory of the servlet collection.
	 * @param requireAuthentication true if all accesses must be authenticated.
	 */
    public ServletSelector(
					File root,
					boolean requireAuthentication) {
		this.root = root;
		this.requireAuthentication = requireAuthentication;
		this.servlets = new Hashtable<String,Class>();
		Servlet.init(root, ""); //Initialize the base servlet.
	}

	/**
	 * Add a Servlet.
	 * @param path the path string associated with the servlet.
	 * @param servlet the class to be instantiated for the servlet.
	 */
	public void addServlet(String path, Class servlet) {
		//See if this path is already in use
		if (servlets.get(path) != null) {
			logger.warn("Installing a servlet on a path already in use:");
			logger.warn("   path:  "+path);
			logger.warn("   class: "+servlet.getName());
		}

		//Put the servlet in the table, even if it is a duplicate.
		servlets.put(path, servlet);

		//Initialize the servlet
		Class[] signature = { File.class, String.class };
		Object[] args = { root, path };
		try { servlet.getMethod("init", signature).invoke(null, args); }
		catch (Exception skip) {
			logger.warn("Unable to initialize "+servlet.getName()+" for path "+path);
		}
	}

	/**
	 * Get the servlet which is associated with the requested path.
	 * If authentication is required and the user is not authenticated,
	 * return an instance of the LoginServlet. Otherwise, return an
	 * instance of the servlet class identified by the appropriate path element
	 * in the request path. If no servlet matches the path, return an
	 * instance of the Servlet class.
	 * @param req the request.
	 * @return an instance of the servlet class which matches the request path.
	 */
    public Servlet getServlet(HttpRequest req) {

		//First make sure the user is authenticated if necessary.
		if (requireAuthentication && (req.getUser() == null)) {
			return new LoginServlet(root, "");
		}

		//Okay, it is permissable to serve this request.
		//Get the path element on which to search.
		Path path = req.getParsedPath();

		String pathElement = path.element(0);

		//Find a matching servlet.
		Class servlet = servlets.get(pathElement);
		if (servlet != null) {
			//A matching servlet was found, instantiate it.
			Class[] signature = { File.class, String.class };
			Object[] args = { root, pathElement };
			try { return (Servlet)servlet.getConstructor(signature).newInstance(args); }
			catch (Exception ex) {
				logger.warn("Unable to instantiate "+servlet.getName());
			}
		}
		//No matching servlet could be instantiated;
		//return an instance of the Servlet base class.
		return new Servlet(root, "");
	}

	/**
	 * Execute the destroy method calls for all servlets. This method
	 * tells servlets to clean up before the server shuts down.
	 */
    public void shutdown() {
		Class[] signature = { File.class, String.class };
		for (String path : servlets.keySet()) {
			Class cl = servlets.get(path);
			try {
				Object[] args = { root, path };
				Servlet servlet = (Servlet)cl.getConstructor(signature).newInstance(args);
				servlet.destroy();
			}
			catch (Exception skip) {
				logger.warn("Unable to destroy the servlet "+cl.getName());
			}
		}
	}

	/**
	 * Get the root directory for all servlets managed by this instance.
	 * @return root the root directory for all servlets managed by this instance.
	 */
	public File getRoot() {
		return root;
	}

}
