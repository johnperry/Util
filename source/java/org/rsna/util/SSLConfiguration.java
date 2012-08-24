/*---------------------------------------------------------------
*  Copyright 2009 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.File;
import org.w3c.dom.Element;

/**
 * A singleton class encapsulating a proxy server.
 */
public class SSLConfiguration {

	String keystore			= "";
	String keystorePassword	= "";
	String truststore		= "";
	String truststorePassword = "";

	static SSLConfiguration sslConfiguration = null;

	protected SSLConfiguration(String keystore, String keystorePassword,
							   String truststore, String truststorePassword) {

		this.keystore = StringUtil.trim(keystore);
		this.keystorePassword = StringUtil.trim(keystorePassword);

		this.truststore = StringUtil.trim(truststore);
		this.truststorePassword = StringUtil.trim(truststorePassword);

		if (keystore.equals("")) keystore = "keystore";
		if (keystorePassword.equals("")) keystorePassword = "ctpstore";
	}


	/**
	 * Set a new SSLConfiguration, creating an instance of the SSLConfiguration
	 * class from a DOM element containing four attributes:
	 * <ul>
	 * <li>keystore
	 * <li>keystorePassword
	 * <li>truststore
	 * <li>truststorePassword
	 * </ul>
	 * @param element the element specifying the SSL parameters.
	 * @return the singleton instance of the SSLConfiguration.
	 */
	public static synchronized SSLConfiguration getInstance(Element element) {
		return getInstance(
					element.getAttribute("keystore"),
					element.getAttribute("keystorePassword"),
					element.getAttribute("truststore"),
					element.getAttribute("truststorePassword") );
	}

	/**
	 * Set a new SSLConfiguration, creating an instance of the SSLConfiguration
	 * class from four Strings.
	 * @param keystore the path to the keystore file
	 * @param keystorePassword the password to the keystore.
	 * @param truststore the path to the truststore file
	 * @param truststorePassword the password to the truststore.
	 * @return the singleton instance of the SSLConfiguration.
	 */
	public static synchronized SSLConfiguration getInstance(
													String keystore, String keystorePassword,
						  							String truststore, String truststorePassword) {
		sslConfiguration = new SSLConfiguration(
									keystore, keystorePassword,
									truststore, truststorePassword);
		return sslConfiguration;
	}

	/**
	 * Get the singleton instance of the SSLConfiguration, or null if one doesn't exist.
	 * @return the singleton instance of the SSLConfiguration.
	 */
	public static synchronized SSLConfiguration getInstance() {
		return sslConfiguration;
	}

	/**
	 * Set the System properties based on the parameters in the SSLConfiguration.
	 */
	public void setSystemParameters() {
        System.setProperty("javax.net.ssl.keyStore", keystore);
        System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);

        System.setProperty("javax.net.ssl.trustStore", truststore);
        System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
	}
}

