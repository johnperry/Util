/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.log4j.Logger;
import org.rsna.util.IPUtil;


/**
 * A simple HTTP Server.
 */
public class HttpServer extends Thread {

	static final Logger logger = Logger.getLogger(HttpServer.class);

	final int maxThreads; //max concurrent threads
	final int port;
	final boolean ssl;
	final ServletSelector selector;
	final ServerSocket serverSocket;
	final ThreadPoolExecutor execSvc;
	final LinkedBlockingQueue<Runnable> queue;

	/**
	 * Class constructor; creates a new instance of
	 * the HttpServer thread on the specified port.
	 * @param ssl true if connections to this server require SSL
	 * @param port the port on which this server listens for connections
	 * @param maxThreads the maximum number of concurrent threads allowed.
	 * @param selector the translator from requested resources to servlets
	 * @throws Exception if the ServerSocket cannot be created.
	 */
    public HttpServer(boolean ssl, int port, int maxThreads, ServletSelector selector) throws Exception {
		super("HttpServer");
		this.ssl = ssl;
		this.port = port;
		this.maxThreads = maxThreads;
		this.selector = selector;
		
		queue = new LinkedBlockingQueue<Runnable>();
		ServerSocketFactory serverSocketFactory =
			ssl ? SSLServerSocketFactory.getDefault() : ServerSocketFactory.getDefault();
		serverSocket = serverSocketFactory.createServerSocket(port);
		execSvc = new ThreadPoolExecutor( maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, queue );
	}

	/**
	 * Start the HttpServer and accept connections.
	 */
	public void run() {
		logger.info((ssl?"SSL ":"")+"HttpServer started on port "+port+" [maxThreads="+maxThreads+"]");
		logger.info((ssl?"SSL ":"")+"HttpServer IP adddress: "+IPUtil.getIPAddress());
		while (!this.isInterrupted()) {
			try {
				//Wait for a connection
				final Socket socket = serverSocket.accept();

				//Handle the connection in a separate thread
				execSvc.execute( new HttpHandler(socket, selector, this) );
			}
			catch (Exception ex) { break; }
		}
		try { serverSocket.close(); }
		catch (Exception ignore) { logger.warn("Unable to close the server socket."); }
	}

	/**
	 * Stop the HttpServer.
	 */
	public void shutdown() {
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
	 * Get maxThreads.
	 * @return the maximum number of active Threads allowed in this HttpServer
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	/**
	 * Get the number of active Threads.
	 * @return the number of active Threads currently servicing requests in this HttpServer
	 */
	public int getActiveThreads() {
		return execSvc.getActiveCount();
	}

	/**
	 * Get the number of Threads waiting in the queue.
	 * @return the number of Threads currently waiting in the queue
	 */
	public int getQueuedThreads() {
		return queue.size();
	}

	/**
	 * Get the SSL flag.
	 * @return true if this server's port requires SSL; false otherwise.
	 */
	public boolean getSSL() {
		return ssl;
	}

}
