/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Digests files, byte arrays, and strings.
 */
public class DigestUtil {

	/**
	 * Digest a file with a specified provider.
	 * @param provider the name of the provider (e.g., "MD5")
	 * for use in digesting the file.
	 * @param file the file to digest.
	 * @param radix the radix of the returned result.
	 * @return the digest of the file, converted to an integer
	 * string in the specified radix, or the empty string if
	 * an error occurs.
	 */
	public static String digest(String provider, File file, int radix) {
		String result;
		BufferedInputStream bis = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(provider);
			bis = new BufferedInputStream( new FileInputStream( file ) );
			byte[] buffer = new byte[8192];
			int n;
			while ( (n=bis.read(buffer)) != -1) messageDigest.update(buffer, 0, n);
			byte[] hashed = messageDigest.digest();
			BigInteger bi = new BigInteger(1, hashed);
			result = bi.toString(radix);
		}
		catch (Exception ex) { result = ""; }
		finally {
			try { bis.close(); }
			catch (Exception ignore) { }
		}
		return result;
	}

	/**
	 * Generate an MD5 hash of an element text,
	 * producing a base-10 digit string.
	 * @param string the string to hash.
	 * @return the MD5 hash of the string,
	 * or the empty string if the hash fails
	 */
	public static String hash(String string) {
		return hash(string, Integer.MAX_VALUE);
	}

	/**
	 * Generate an MD5 hash of an element text,
	 * producing a base-10 digit string.
	 * @param string the string to hash.
	 * @param maxlen the maximum number of characters to return.
	 * @return the MD5 hash of the string,
	 * or the empty string if the hash fails
	 */
	public static String hash(String string, int maxlen) {
		String result;
		try {
			if (string == null) string = "null";
			if (maxlen < 1) maxlen = Integer.MAX_VALUE;
			result = getUSMD5(string);
			if (result.length() > maxlen) {
				result = result.substring(0,maxlen);
			}
		}
		catch (Exception ex) { result = ""; }
		return result;
	}

	/**
	 * Hash a text string and return the result as a base 10 numeric string.
	 * @param string the string to hash.
	 * @return the MD5 hash of the string.
	 * @throws Exception if the hash fails
	 */
	public static String getUSMD5(String string) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		byte[] hashed = messageDigest.digest(string.getBytes("UTF-8"));
		BigInteger bi = new BigInteger(1,hashed);
		return bi.toString();
	}

}

