/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import javax.swing.Timer;
import org.apache.log4j.Logger;
import org.rsna.servlets.Servlet;

/**
 * The thread that handles an individual HTTP connection.
 */
public class HttpHandler extends Thread implements ActionListener {

	static final Logger logger = Logger.getLogger(HttpHandler.class);
	static final int timeout = 60000;

	Socket socket;
	ServletSelector selector;
	HttpResponse res = null;
	HttpRequest req = null;
	Timer timer = null;

	/**
	 * Construct an HttpHandler.
	 * @param socket the socket on which the connection was received.
	 */
	public HttpHandler(Socket socket, ServletSelector selector) {
		super("HttpHandler");
		this.socket = socket;
		this.selector = selector;
	}

	/**
	 * Handle the connection in a separate Thread, getting the streams,
	 * selecting a Servlet to handle the request, and returning the response.
	 */
	public void run() {
		//Initialize a timer to log request timeouts
		timer = new Timer(timeout, this);
		timer.setRepeats(false);

		try {
			//Make a response
			res = new HttpResponse(socket);

			//Get the request
			req = new HttpRequest(socket);

			//Get the Servlet
			Servlet servlet = selector.getServlet(req);

			//Start the timeout timer
			timer.start();

			//Call the appropriate method
			if (req.method.equals("GET")) {
				servlet.doGet(req,res);
			}
			else if (req.method.equals("POST")) {
				servlet.doPost(req,res);
			}
			else {
				res.setResponseCode(res.notimplemented);
				res.send();
				logger.debug("Unimplemented request ("+req.method+") received from "+req.getRemoteAddress());
			}

			//Stop the timer; the timeout didn't happen
			timer.stop();
		}
		catch (Exception ex) {
			timer.stop();
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
			else {
				logger.error("Internal server error (req==null)",ex);
			}
		}
		//Close everything.
		if (req != null) req.close();
		if (res != null) res.close();
		try { socket.close(); }
		catch (Exception ex) { logger.debug("Unable to close the socket."); }
	}

	public void actionPerformed(ActionEvent event) {
		String reqString = (req != null) ? ": "+req.toString() : "";
		logger.warn("Request timed out ("+(timeout/1000)+" sec)" + reqString);
	}

}
