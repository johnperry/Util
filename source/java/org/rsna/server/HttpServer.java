/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.log4j.Logger;


/**
 * A simple HTTP Server.
 */
public class HttpServer extends Thread {

	static final Logger logger = Logger.getLogger(HttpServer.class);

	final int port;
	final boolean ssl;
	final ServletSelector selector;
	final ServerSocket serverSocket;
	final ExecutorService execSvc;

	/**
	 * Class constructor; creates a new instance of
	 * the HttpServer thread on the specified port.
	 * @param ssl true if connections to this server require SSL
	 * @param port the port on which this server listens for connections
	 * @param selector the translator from requested resources to servlets
	 * @throws Exception if the ServerSocket cannot be created.
	 */
    public HttpServer(boolean ssl, int port, ServletSelector selector) throws Exception {
		this.ssl = ssl;
		this.port = port;
		this.selector = selector;
		ServerSocketFactory serverSocketFactory =
			ssl ? SSLServerSocketFactory.getDefault() : ServerSocketFactory.getDefault();
		serverSocket = serverSocketFactory.createServerSocket(port);
		execSvc = Executors.newFixedThreadPool( 4 ); //max concurrent threads
	}

	/**
	 * Start the HttpServer and accept connections.
	 */
	public void run() {
		logger.info((ssl?"SSL ":"")+"HttpServer started on port "+port);
		while (!this.isInterrupted()) {
			try {
				//Wait for a connection
				final Socket socket = serverSocket.accept();

				//Handle the connection in a separate thread
				execSvc.execute( new HttpHandler(socket, selector) );
			}
			catch (Exception ex) { break; }
		}
		try { serverSocket.close(); }
		catch (Exception ignore) { logger.warn("Unable to close the server socket."); }
	}

	/**
	 * Stop the HttpServer.
	 */
	public void stopServer() {
		execSvc.shutdown();
		this.interrupt();
		selector.shutdown();
	}

	/**
	 * Get the ServletSelector.
	 * @return the ServletSelector for this HttpServer
	 */
	public ServletSelector getServletSelector() {
		return selector;
	}

	/**
	 * Get the port.
	 * @return the port for this HttpServer
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Get the SSL flag.
	 * @return true if this server's port requires SSL; false otherwise.
	 */
	public boolean getSSL() {
		return ssl;
	}

}
