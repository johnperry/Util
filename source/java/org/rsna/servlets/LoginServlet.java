/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.servlets;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;
import org.apache.log4j.Logger;
import org.rsna.server.Authenticator;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;
import org.rsna.server.User;
import org.rsna.server.Users;
import org.rsna.server.UsersOpenAMImpl;
import org.rsna.util.FileUtil;
import org.rsna.util.StringUtil;

/**
 * The LoginServlet.
 */
public class LoginServlet extends Servlet {

	static final Logger logger = Logger.getLogger(LoginServlet.class);

	/**
	 * Construct a LoginServlet.
	 * @param root the root directory of the server.
	 * @param context the path identifying the servlet.
	 */
	public LoginServlet(File root, String context) {
		super(root, context);
	}

	/**
	 * The GET handler: display a page containing a form allowing
	 * the user to log in. The page is not actually stored in the
	 * root directory of the servlet. It is instead stored in the
	 * root directory of the program. This makes the file available
	 * for all server instances, even ones on other ports.
	 * <p>
	 * This method can also be used by Ajax applications to log in.
	 * The path must end in /ajax, and the username and password
	 * must be passed in the query string. This call returns a
	 * response code of either 200 if the login is successful or
	 * 403 if it fails. If the Ajax application wishes to log out,
	 * it must supply the /ajax path element and the logout query string.
	 * @param req the request object
	 * @param res the response object
	 */
	public void doGet(HttpRequest req, HttpResponse res) {

		logger.debug("Request received:\n"+req.toVerboseString());

 		//Get the possible query parameters.
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		String logout = req.getParameter("logout");
		
		//See if this is an OpenAM login that requires authentication.
		Users users = Users.getInstance();
		if (!req.isFromAuthenticatedUser() 
				&& (users instanceof UsersOpenAMImpl)
				&& req.getServer().getServletSelector().getRequireAuthentication()) {
			
			String redirectURL = req.getProtocol()+"://"+req.getHost()+"/";
			String url = ((UsersOpenAMImpl)users).getLoginURL(redirectURL);
			res.redirect(url);
		}

		//See if this is an ajax call or a web page request
		else if (req.getPath().endsWith("/ajax")) {
			//It's an ajax call.
			if (logout == null) {
				res.setResponseCode( login(req, res, username, password) ? 200 : 403 );
				res.send();
			}
			else {
				Authenticator.getInstance().closeSession(req, res);
				res.send();
			}
		}
		else {
			//It's a web page request.
			if (logout == null) {

				//See if it's a direct login
				if ((username != null) && (password != null)) {
					login(req, res, username, password);
					redirect(req, res);
					return;
				}

				//No, see if we are to suppress the login if the
				//user is already authenticated and just go directly
				//to the requested URL (or the default):
				if (req.hasParameter("skip") && req.isFromAuthenticatedUser()) {
					redirect(req, res);
					return;
				}

				//No, display the login page
				String loginPage =
							FileUtil.getText(
								FileUtil.getStream( new File(root, "login.html"), "/login.html" ) );

				//Set the redirect URL for the post.
				Properties props = new Properties();
				String url = req.getParameter("url", "");
				if (isAttack(req, url)) url = "";
				props.put("url", url);
				loginPage = StringUtil.replace(loginPage, props);

				res.write(loginPage);
				res.disableCaching();
				res.setContentType("html");
				res.send();
			}
			else {
				Authenticator.getInstance().closeSession(req, res);
				redirect(req, res);
			}
		}
	}

	/**
	 * The POST handler: authenticate the user from the form parameters.
	 * @param req the request object
	 * @param res the response object
	 */
	public void doPost(HttpRequest req, HttpResponse res) {

		logger.debug("Request received:\n"+req.toVerboseString());

		String username = req.getParameter("username");
		String password = req.getParameter("password");
		login(req, res, username, password);
		redirect(req, res);
	}

	//Attempt a login and return true if it succeeded.
	private boolean login(
						HttpRequest req, HttpResponse res,
						String username, String password) {

		logger.debug("username = "+username);
		logger.debug("password = "+password);
		boolean passed = false;
		Authenticator authenticator = Authenticator.getInstance();
		if ((username != null) && (password != null)) {
			User user = Users.getInstance().authenticate(username, password);
			if (user != null) {
				passed = authenticator.createSession(user, req, res);
				logger.debug("Response headers:\n"+res.listHeaders("  "));
			}
		}
		if (!passed) authenticator.closeSession(req, res);
		logger.debug("passed = "+passed);
		return passed;
	}

	//Redirect to a specified URL or to a URL related to the path
	//by which the servlet was accessed. If the url query param
	//is supplied, redirect to it. Otherwise, if the path ends at
	//the context, go to the parent of the last path element.
	//If the path does not end at the context, go to the full path.
	private void redirect(HttpRequest req, HttpResponse res) {
		String url = req.getParameter("url");
		if (url == null) {
			url = req.getPath();
			if (url.endsWith("/"+context)) {
				url = url.substring(0, url.length() - context.length());
			}
		}
		logger.debug("Redirect URL before test: \""+url+"\"");
		if (url.equals("") || isAttack(req, url) || !isSameHost(req, url)) url = "/";
		logger.debug("Redirect URL after test: \""+url+"\"");
		res.redirect(url);
	}

	//Check that a URL string points to the same host as an HttpRequest
	//to defeat a kind of phishing attack
	private boolean isSameHost(HttpRequest req, String urlString) {
		if (!urlString.startsWith("/") && urlString.contains("://")) {
			try {
				URI uri = new URI(urlString);
				URL url = uri.toURL();
				String urlHost = url.getHost();
				String reqHost = req.getHost();
				logger.debug("isSameHost: req; \""+urlHost+"\" url: \""+urlHost+"\"");
				if (reqHost.contains(":")) reqHost = reqHost.substring(0, reqHost.indexOf(":"));
				return urlHost.equals(reqHost);
			}
			catch (Exception ex) { logger.debug("Unable to parse URL:", ex); }
			return false;
		}
		return true;
	}

	//Check a path for characters that indicate a cross-site scripting attack
	private boolean isAttack(HttpRequest req, String path) {
		boolean attack =  path.contains("\n")
							|| path.contains("\r")
							|| path.contains("<")
							|| path.contains(">")
							|| path.contains("%")
							|| path.contains("javascript");
		if (attack) {
			logger.warn("Attack detected from "+req.getRemoteAddress());
		}
		return attack;
	}
}
