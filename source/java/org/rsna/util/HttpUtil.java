/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.*;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

/**
 * A class to encapsulate HTTP utilities.
 */
public class HttpUtil {

	/**
	 * Get an HttpURLConnection for a specified URL String. The
	 * connection is returned set up for input and output, with
	 * any required proxy authentication parameters included,
	 * and with the the request method set to "POST" and the
	 * content type set to "application/x-mirc".
	 * (These can be overwritten if necessary.)
	 * @param urlString the absolute URL, starting with the protocol.
	 * @return the HttpURLConnection.
	 * @throws Exception if the protocol is neither "http" nor "https",
	 * or if an error occurs in initializing the connection.
	 */
	public static HttpURLConnection getConnection(String urlString) throws Exception {
		return getConnection(new URI(urlString).toURL());
	}


	/**
	 * Get an HttpURLConnection for a specified URL. The
	 * connection is returned set up for input and output, with
	 * any required proxy authentication parameters included,
	 * and with the the request method set to "POST" and the
	 * content type set to "application/x-mirc".
	 * (These can be overwritten if necessary.)
	 * @param url the absolute URL, including the protocol.
	 * @return the HttpURLConnection.
	 * @throws Exception if the protocol is neither "http" nor "https",
	 * or if an error occurs in initializing the connection.
	 */
	public static HttpURLConnection getConnection(URL url) throws Exception {
		HttpURLConnection conn;
		Proxy proxy = null;

		String protocol = url.getProtocol().toLowerCase();
		if (!protocol.equals("https") && !protocol.equals("http")) {
			throw new Exception("Unsupported protocol ("+protocol+")");
		}

		//If the connection is through a proxy server, create a Proxy object.
		ProxyServer proxyServer = ProxyServer.getInstance();
		if ((proxyServer != null) && proxyServer.isEnabled()) {
			proxy = new Proxy( Proxy.Type.HTTP, 
							   new InetSocketAddress(
									proxyServer.proxyIPAddress,
									proxyServer.getPort()) );
		}
		else proxy = Proxy.NO_PROXY;
		
		//Instantiate the connection.
		if (protocol.equals("https")) {
			HttpsURLConnection httpsConn = (HttpsURLConnection)url.openConnection(proxy);
			
			//Accept all hosts
			httpsConn.setHostnameVerifier(new AcceptAllHostnameVerifier());
			httpsConn.setUseCaches(false);
			httpsConn.setDefaultUseCaches(false);

			//Accept all certs
			TrustManager[] trustAllCerts = new TrustManager[] { new AcceptAllX509TrustManager() };
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			httpsConn.setSSLSocketFactory(sc.getSocketFactory());

			conn = httpsConn;
		}
		else conn = (HttpURLConnection)url.openConnection(proxy);

		//If proxy authentication credentials are available,
		//add the Proxy-Authorization header.
		//Note: The default Authenticator was set when the ProxyServer was first instantiated.
		if ((proxyServer != null) && proxyServer.hasCredentials()) {
			conn.setRequestProperty(
				"Proxy-Authorization",
				"Basic "+proxyServer.getEncodedCredentials());
		}
		
		//If user credentials are embedded in the URL, add the Authorization header.
		String userinfo = url.getUserInfo();
		if (userinfo != null) {
			String[] creds = userinfo.split(":");
			if (creds.length == 2) {
				conn.setRequestProperty(
					"Authorization",
					"Basic "+Base64.encodeToString((creds[0].trim() + ":" + creds[1].trim()).getBytes()));
			}
		}
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-mirc");

		//Return the connection.
		return conn;
	}

	/**
	 * Initialize the CookieHandler and set it as the default.
	 * @return the CookieManager
	 */
	public static CookieManager initializeCookieManager() {
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
		return cookieManager;
	}
}
