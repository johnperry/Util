/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.service;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.log4j.Logger;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;

/**
 * A Thread that implements a single HTTP Service.
 */
public class HttpService extends Thread {

	static final Logger logger = Logger.getLogger(HttpService.class);

	final int maxThreads = 4; //max concurrent threads
	final ThreadPoolExecutor execSvc;
	final LinkedBlockingQueue<Runnable> queue;
	final ServerSocket serverSocket;
	final boolean ssl;
	final int port;
	final Service service;
	final String name;

    public HttpService(boolean ssl, int port, Service service) throws Exception {
		this(ssl, port, service, null);
	}

    public HttpService(boolean ssl, int port, Service service, String name) throws Exception {
		super("HttpService");
		this.ssl = ssl;
		this.port = port;
		this.service = service;
		this.name = name;
		
		queue = new LinkedBlockingQueue<Runnable>();
		ServerSocketFactory serverSocketFactory =
			ssl ? SSLServerSocketFactory.getDefault() : ServerSocketFactory.getDefault();
		serverSocket = serverSocketFactory.createServerSocket(port); //use the default backlog of 50
		execSvc = new ThreadPoolExecutor( maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, queue );
	}

	// Start the HttpService and accept connections.
	public void run() {
		logger.info("HttpService open on port "+port + ((name!=null)?" ("+name+")":"") );
		while (!this.isInterrupted()) {
			try {
				final Socket socket = serverSocket.accept();
				Handler handler = new Handler(socket, service);
				execSvc.execute( handler );
			}
			catch (Exception ex) { break; }
		}
		try { serverSocket.close(); }
		catch (Exception ex) { logger.warn("Unable to close the server socket."); }
	}

	// Stop the HttpReceiver.
	public void stopServer() {
		execSvc.shutdown();
		this.interrupt();
	}

	//Class to handle one connection and service the request.
	class Handler extends Thread {

		Socket socket;
		Service service;

		public Handler(Socket socket, Service service) {
			super();
			this.socket = socket;
			this.service = service;
		}

		public void run() {
			HttpResponse res = null;
			HttpRequest req = null;
			try {
				res = new HttpResponse(socket);
				req = new HttpRequest(socket);
				service.process(req, res);
			}
			catch (Exception ex) {
				if (ex instanceof javax.net.ssl.SSLException) {
					logger.warn("SSL connection error, plaintext?");
				}
				else {
					logger.error("Internal server error.", ex);
					try {
						res = new HttpResponse(socket);
						res.setResponseCode(500); //internal server error
						res.send();
					}
					catch (Exception ignore) { /*Don't log; the real error is logged above.*/ }
				}
			}

			//Close everything.
			if (req != null) req.close();
			if (res != null) res.close();
			try { socket.close(); }
			catch (Exception ex) { logger.warn("Unable to close the socket."); }
		}
	}
}

