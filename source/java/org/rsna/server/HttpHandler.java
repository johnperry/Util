/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import org.apache.log4j.Logger;
import org.rsna.servlets.Servlet;

/**
 * The thread that handles an individual HTTP connection.
 */
public class HttpHandler extends Thread {

	static final Logger logger = Logger.getLogger(HttpHandler.class);

	Socket socket;
	HttpServer server;
	ServletSelector selector;
	HttpResponse res = null;
	HttpRequest req = null;

	/**
	 * Construct an HttpHandler.
	 * @param socket the socket on which the connection was received.
	 */
	public HttpHandler(Socket socket, ServletSelector selector, HttpServer server) {
		super("HttpHandler");
		this.socket = socket;
		this.selector = selector;
		this.server = server;
	}

	/**
	 * Handle the connection in a separate Thread, getting the streams,
	 * selecting a Servlet to handle the request, and returning the response.
	 */
	public void run() {

		try {
			//Make a response
			res = new HttpResponse(socket);

			//Get the request
			req = new HttpRequest(socket, server);
			
			//Get the Servlet
			Servlet servlet = selector.getServlet(req);

			//Call the appropriate method
			if (req.method.equals("GET")) {
				servlet.doGet(req, res);
			}
			else if (req.method.equals("POST")) {
				servlet.doPost(req, res);
			}
			else if (req.method.equals("PUT")) {
				servlet.doPut(req, res);
			}
			else if (req.method.equals("DELETE")) {
				servlet.doDelete(req, res);
			}
			else if (req.method.equals("OPTIONS")) {
				servlet.doOptions(req, res);
			}
			else if (req.method.equals("")) {
				//Do not send a response
			}
			else {
				res.setResponseCode(res.notallowed);
				res.send();
				logger.debug("Unallowed method in request ("+req.method+") received from "+req.getRemoteAddress());
			}
		}
		catch (Exception ex) {
			if (req != null) {
				logger.error("Internal server error ("+req.toString()+")",ex);
				try {
					StringWriter sw = new StringWriter();
					ex.printStackTrace(new PrintWriter(sw));
					res = new HttpResponse(socket);
					res.setResponseCode(res.ok); //so the browser will display the page
					res.write("<html>");
					res.write("<head><title>ERROR</title></head>");
					res.write("<body><h1>Internal Server Error (HTTP 500)</h1><pre>"+sw.toString()+"</pre></body>");
					res.write("</html>");
					res.send();
				}
				catch (Exception ignore) { /*Don't log this; the real problem has been logged above.*/ }
			}
			else { logger.error("Internal server error (req==null)",ex); }
		}
		//Close everything.
		if (req != null) req.close();
		if (res != null) res.close();
		try { socket.close(); }
		catch (Exception ex) { logger.info("Unable to close the socket."); }
	}
}
