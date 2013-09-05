/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * A class providing a decoder for encrypted elements in a DICOM file.
 */
public class CipherUtil {

	static final String transform = "Blowfish";
	Cipher cipher = null;

	/**
	 * Decode an encrypted string.
	 * @param key the encryption key as a Base-64 String.
	 */
	public CipherUtil(String key) throws Exception {
		cipher = getCipher(key.trim());
	}

	/**
	 * Get the maximum key length allowed by the Java Cryptography Extension.
	 * @return the maximum allowed key length, or zero if the transform is not available.
	 */
	public static int getMaxAllowedKeyLength() {
		return getMaxAllowedKeyLength(transform);
	}

	/**
	 * Get the maximum key length allowed by the Java Cryptography Extension.
	 * @param name the name of the transform.
	 * @return the maximum allowed key length, or zero if the transform is not available.
	 */
	public static int getMaxAllowedKeyLength(String name) {
		try { return Cipher.getMaxAllowedKeyLength(name); }
		catch (Exception ex) { return 0; }
	}

	/**
	 * A static method to decrypt an encrypted UTF-8 string. This method is intended
	 * for low-volume decoding since it instantiates the cipher. For high-volume
	 * decoding where the key is always the same, it is best to instantiate this
	 * class and call the decipher(String) method.
	 * @param text the base-64 text representation of the encrypted UTF-8 string.
	 * @param key the encryption key as a Base-64 string.
	 * @return the decrypted string.
	 */
	public static String decipher(String text, String key) throws Exception {
		Cipher cipher = getCipher(key.trim());
		byte[] encrypted = Base64.decode(text);
		byte[] decrypted = cipher.doFinal(encrypted);
		return new String(decrypted,"UTF-8");
	}

	/**
	 * Decrypt an encrypted UTF-8 string. This method uses the cipher defined in the
	 * constructor, so all calls to this method use the same key.
	 * @param text the base-64 text representation of the encrypted UTF-8 string.
	 * @return the decrypted string.
	 */
	public String decipher(String text) throws Exception {
		byte[] encrypted = Base64.decode(text);
		byte[] decrypted = cipher.doFinal(encrypted);
		return new String(decrypted,"UTF-8");
	}

	//Get a Cipher initialized with the specified key.
	private static Cipher getCipher(String keyText) throws Exception {
		Provider sunJce = new com.sun.crypto.provider.SunJCE();
		Security.addProvider(sunJce);
		byte[] key = getEncryptionKey(keyText, 128);
		SecretKeySpec skeySpec = new SecretKeySpec(key, transform);

		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		byte[] seed = random.generateSeed(8);
		random.setSeed(seed);

		Cipher cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, random);
		return cipher;
	}

	static String nonce = "tszyihnnphlyeaglle";
	static String pad = "===";
	private static byte[] getEncryptionKey(String keyText, int size) throws Exception {
		if (keyText == null) keyText = "";
		keyText = keyText.trim();

		//Now make it into a base-64 string encoding the right number of bits.
		keyText = keyText.replaceAll("[^a-zA-Z0-9+/]","");

		//Figure out the number of characters we need.
		int requiredChars = (size + 5) / 6;
		int requiredGroups = (requiredChars + 3) / 4;
		int requiredGroupChars = 4 * requiredGroups;

		//If we didn't get enough characters, then throw some junk on the end.
		while (keyText.length() < requiredChars) keyText += nonce;

		//Take just the right number of characters we need for the size.
		keyText = keyText.substring(0,requiredChars);

		//And return the string padded to a full group.
		keyText = (keyText + pad).substring(0,requiredGroupChars);
		return Base64.decode(keyText);
	}

}
