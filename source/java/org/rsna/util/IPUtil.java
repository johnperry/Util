/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * A class for getting the IP address of the host computer.
 */
public class IPUtil {

	static final String def = "127.0.0.1";
	
	protected static String ipAddress = null;
	
	/**
	 * Set the IP address to be returned by all subsequent
	 * calls to getIPAddress.
	 * @param ip the IP Address to be returned by getIPAddress
	 * @return the IP Address string.
	 */
	public static String getIPAddress(String ip) {
		ipAddress = ip;
		//System.out.println("IPUtil.getIPAddress(ip) returning "+ipAddress);
		return ipAddress;
	}
	
	/**
	 * Get the IP address of the host computer, or the loopback address
	 * (127.0.0.1) if the operation fails.
	 * @return the IP Address string.
	 */
	public static String getIPAddress() {
		if (ipAddress == null) {
			ipAddress = getInet4Address();
			//System.out.println("IPUtil.getInet4Address returned "+ipAddress);
		}
		//System.out.println("IPUtil.getIPAddress() returning "+ipAddress);
		return ipAddress;
	}
	
	/**
	 * Get the IP address of the specified network interface, or the loopback address
	 * (127.0.0.1) if the operation fails. This value will be returned for all subsequent
	 * calls to getIPAddress.
	 * @param macAddress the MAC Address of the network interface.
	 * @return the IP Address string.
	 */
	public static String getIPAddressForMAC(String macAddress) {
		//System.out.println("IPUtil.getIPAddressForMAC(\""+macAddress+"\")");
		ipAddress = getInet4Address(macAddress);
		//System.out.println("IPUtil.getInet4Address() returned "+ipAddress);
		return getIPAddress();
	}
	
	//Return the first IPv4 address that is not a loopback address.
	//If anything goes wrong, return the default loopback address.
	protected static String getInet4Address() {
		try {
			//Get all the network interfaces
			Enumeration<NetworkInterface> nwEnum = NetworkInterface.getNetworkInterfaces();
			while (nwEnum.hasMoreElements()) {
				NetworkInterface nw = nwEnum.nextElement();
				Enumeration<InetAddress> ipEnum = nw.getInetAddresses();
				while (ipEnum.hasMoreElements()) {
					InetAddress ina = ipEnum.nextElement();
					if ((ina instanceof Inet4Address) && !ina.isLoopbackAddress()) {
						return ina.getHostAddress();
					}
				}
			}
		}
		catch (Exception ignore) { }
		return def;
	}

	//Return the IPv4 address associated with the specified MAC address.
	//If anything goes wrong, return null.
	protected static String getInet4Address(String macAddress) {
		//System.out.println("IPUtil.getInet4Address(\""+macAddress+"\")");
		try {
			//Convert the MAC adddress to a byte array
			String[] macS = macAddress.split("-");
			if (macS.length != 6) return null;
			byte[] macB = new byte[6];
			for (int i=0; i<6; i++) {
				int k = Integer.parseInt(macS[i].trim(), 16);
				macB[i] = (byte)(k & 0xff);
			}
			
			//Get all the network interfaces
			Enumeration<NetworkInterface> nwEnum = NetworkInterface.getNetworkInterfaces();

			//Search for a network interface matching the mac address, and return 
			//its IPv4 address if it is not a loopback address.
			while (nwEnum.hasMoreElements()) {
				NetworkInterface nw = nwEnum.nextElement();
				byte[] hwAdrs = nw.getHardwareAddress();
				if ((hwAdrs != null) && checkAdrs(hwAdrs, macB, 6)) {
					Enumeration<InetAddress> ipEnum = nw.getInetAddresses();
					while (ipEnum.hasMoreElements()) {
						InetAddress ina = ipEnum.nextElement();
						if ((ina instanceof Inet4Address) && !ina.isLoopbackAddress()) {
							//System.out.println("IPUtil.getInet4Address(\""+macAddress+"\") found "+ina.getHostAddress());
							return ina.getHostAddress();
						}
					}
				}
			}
		}
		catch (Exception ignore) { ignore.printStackTrace(); }
		return null;
	}
	
	protected static boolean checkAdrs(byte[] a, byte[] b, int len) {
		//System.out.println("checkAdrs comparing:");
		//System.out.println("   "+listArray(a));
		//System.out.println("   "+listArray(b));
		try {
			for (int i=0; i<len; i++) {
				if (a[i] != b[i]) return false;
			}
			return true;
		}
		catch (Exception ex) { }
		return false;
	}
	
	protected static String listArray(byte[] x) {
		StringBuffer sb = new StringBuffer();
		for (byte b : x) {
			if (sb.length() > 0) sb.append("-");
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}
}

