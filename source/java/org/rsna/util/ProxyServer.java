/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.net.Authenticator;
import java.util.Properties;
import org.w3c.dom.Element;

/**
 * A singleton class encapsulating proxy server parameters.
 */
public class ProxyServer {

	final String proxyIPAddress;
	final String proxyPort;
	final String proxyUsername;
	final String proxyPassword;

	static ProxyServer proxyServer = null;

	protected ProxyServer(String proxyIPAddress, String proxyPort,
						  String proxyUsername, String proxyPassword) {
		this.proxyIPAddress = StringUtil.trim(proxyIPAddress);
		this.proxyPort 		= StringUtil.trim(proxyPort);
		this.proxyUsername 	= StringUtil.trim(proxyUsername);
		this.proxyPassword 	= StringUtil.trim(proxyPassword);
	}

	/**
	 * Get the ProxyServer, or create a new one if one doesn't exist.
	 * If necessary, this method creates an instance of the ProxyServer
	 * class from a DOM element containing four attributes:
	 * <ul>
	 * <li>proxyIPAddress
	 * <li>proxyPort
	 * <li>proxyUsername
	 * <li>proxyPassword
	 * </ul>
	 * @param element the element specifying the proxy parameters. The
	 * element must have a first-generation child element with the tag
	 * name "ProxyServer" containing the proxy attributes.
	 * @return the singleton instance of the proxy server.
	 */
	public static synchronized ProxyServer getInstance(Element element) {
		if (proxyServer == null) {
			if (!element.getTagName().equals("ProxyServer")) {
				element = XmlUtil.getFirstNamedChild(element, "ProxyServer");
			}
			if (element != null) {
				return getInstance(
							element.getAttribute("proxyIPAddress"),
							element.getAttribute("proxyPort"),
							element.getAttribute("proxyUsername"),
							element.getAttribute("proxyPassword") );
			}
			else return getInstance("", "", "", "");
		}
		return proxyServer;
	}

	/**
	 * Get the ProxyServer, or create a new one if one doesn't exist.
	 * If necessary, this method creates an instance of the ProxyServer
	 * class from four Strings.
	 * @param proxyIPAddress the IP address of the proxy server
	 * @param proxyPort the port of the proxy server
	 * @param proxyUsername the username to be provided to the proxy
	 * server if proxy authentication is required.
	 * @param proxyPassword the password to be provided to the proxy
	 * server if proxy authentication is required.
	 * @return the singleton instance of the proxy server.
	 */
	public static synchronized ProxyServer getInstance(String proxyIPAddress, String proxyPort,
						  			  String proxyUsername, String proxyPassword) {
		if (proxyServer == null) {
			proxyServer = new ProxyServer(
									proxyIPAddress, proxyPort, proxyUsername, proxyPassword);
			proxyServer.setSystemParameters();
			if (proxyServer.hasCredentials()) {
				Authenticator.setDefault(new ProxyAuthenticator());
			}
		}
		return proxyServer;
	}

	/**
	 * Get the singleton instance of the ProxyServer, or null if one doesn't exist.
	 * @return the singleton instance of the proxy server.
	 */
	public static synchronized ProxyServer getInstance() {
		return proxyServer;
	}

	/**
	 * Set the System properties based on the parameters in the ProxyServer.
	 * If the proxy is enabled, set the System properties.
	 * If the proxy is not enabled, clear the System properties.
	 */
	public void setSystemParameters() {
		if (isEnabled()) setProxyProperties();
		else clearProxyProperties();
	}

	/**
	 * Determine whether the proxy parameters indicate that the
	 * proxy server is enabled.
	 * @return true if the proxy parameters indicate that the
	 * proxy server is enabled.
	 */
	public boolean isEnabled() {
		return !proxyIPAddress.equals("") && !proxyPort.equals("");
	}

	/**
	 * Determine whether the parameters indicate
	 * that proxy user authentication is to be used.
	 * @return true if the parameters indicate
	 * that proxy user authentication is to be used.
	 */
	public boolean hasCredentials() {
		return isEnabled() && !proxyUsername.equals("") && !proxyPassword.equals("");
	}
	
	/**
	 * Get the proxyPort as an integer
	 * @return the proxyPort, or 0 if no port is available.
	 */
	public int getPort() {
		return StringUtil.getInt(proxyPort, 0);
	}

	/*
	 * Set the Java System properties that apply to the proxy server.
	 */
	private void setProxyProperties() {
		System.setProperty("http.proxyHost",proxyIPAddress);
		System.setProperty("http.proxyPort",proxyPort);
		System.setProperty("https.proxyHost",proxyIPAddress);
		System.setProperty("https.proxyPort",proxyPort);
		System.setProperty("ftp.proxyHost",proxyIPAddress);
		System.setProperty("ftp.proxyPort",proxyPort);
		
		if (!proxyUsername.equals("")) {
			System.setProperty("http.proxyUser",proxyUsername);
			System.setProperty("http.proxyPassword",proxyPassword);
			System.setProperty("https.proxyUser",proxyUsername);
			System.setProperty("https.proxyPassword",proxyPassword);
			System.setProperty("ftp.proxyUser",proxyUsername);
			System.setProperty("ftp.proxyPassword",proxyPassword);
		}			
	}

	/*
	 * Clear the Java System properties that apply to the proxy server.
	 */
	private void clearProxyProperties() {
		Properties sys = System.getProperties();
		sys.remove("http.proxyHost");
		sys.remove("http.proxyPort");
		sys.remove("http.proxyUser");
		sys.remove("http.proxyPassword");
		sys.remove("https.proxyHost");
		sys.remove("https.proxyPort");
		sys.remove("https.proxyUser");
		sys.remove("https.proxyPassword");
 		sys.remove("ftp.proxyHost");
		sys.remove("ftp.proxyPort");
 		sys.remove("ftp.proxyUser");
		sys.remove("ftp.proxyPassword");
	}

	/**
	 * Get the base-64 encoded value of the proxy user credentials
	 * in Basic auth form for an HTTP Proxy-Authorization header.
	 * @return the credentials encoded in Base64
	 */
	public String getEncodedCredentials() {
		return Base64.encodeToString((proxyUsername + ":" + proxyPassword).getBytes());
	}
}
