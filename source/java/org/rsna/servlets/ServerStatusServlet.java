/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.servlets;

import java.io.File;
import org.rsna.server.*;
import org.rsna.servlets.Servlet;
import org.rsna.util.StringUtil;

/**
 * The ServerStatusServlet. This servlet returns the status of the
 * server, including the number of active threads currently
 * servicing requests, the size of the server thread pool, and
 * the number of requests waiting in the queue.
 */
public class ServerStatusServlet extends Servlet {

	/**
	 * Construct a ServerStatusServlet.
	 * @param root the root directory of the server.
	 * @param context the path identifying the servlet.
	 */
	public ServerStatusServlet(File root, String context) {
		super(root, context);
	}

	/**
	 * The GET handler: return the date/time.
	 * @param req the request object
	 * @param res the response object
	 */
	public void doGet(HttpRequest req, HttpResponse res) {
		if (req.userHasRole("admin")) {
			Authenticator auth = Authenticator.getInstance();
			auth.removeInactiveSessions();
			HttpServer server = req.getServer();
			int maxThreads = server.getMaxThreads();
			int activeThreads = server.getActiveThreads();
			int queuedThreads = server.getQueuedThreads();
			int sessionCount = auth.getActiveSessionCount();

			StringBuffer sb = new StringBuffer();
			sb.append( activeThreads + " of " + maxThreads + " server threads are currently active.\n"
								+ queuedThreads + " thread"
								+ ((queuedThreads == 1) ? " is" : "s are")
								+ " waiting in the queue.\n"
								+ sessionCount + " session"
								+ ((sessionCount == 1) ? " is" : "s are")
								+ " currently active" + ((sessionCount > 0) ? ":" : ".")
								+ "\n");
			
			if (sessionCount > 0) {
				sb.append( String.format("\n    %-10s  %s", "User name", "Last Access") );
				sb.append( String.format("\n    %-10s  %s", "---------", "-----------") );
			}
			for (Session session : auth.getActiveSessions()) {
				String n = session.user.getUsername();
				String t = StringUtil.getTime(session.lastAccess, ":");
				sb.append( String.format("\n    %-10s  %s", n, t) );
			}

			res.write(sb.toString());
			res.setContentType("txt");
			res.disableCaching();
			res.send();
		}
	}
}
