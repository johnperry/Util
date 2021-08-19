/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * A class to handle authentication requests from a proxy server,
 * supplying credentials from the ProxyServer instance.
 */
public class ProxyAuthenticator extends Authenticator {

	public ProxyAuthenticator() {
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
	}
	
	protected PasswordAuthentication getPasswordAuthentication() {
		if (getRequestorType() == RequestorType.PROXY) {
			ProxyServer proxyServer = ProxyServer.getInstance();
			if ((proxyServer != null) && proxyServer.hasCredentials()) {
				return new PasswordAuthentication( 
					proxyServer.proxyUsername, proxyServer.proxyPassword.toCharArray() );
			}
		}
		return null;
	}
}
