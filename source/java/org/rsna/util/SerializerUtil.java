/*---------------------------------------------------------------
*  Copyright 2010 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.log4j.Logger;

/**
 * A class encapsulating methods to serialize a class to a file.
 */
public class SerializerUtil {

	static final Logger logger = Logger.getLogger(SerializerUtil.class);

	/**
	 * Deserialize an object stored in a specified file.
	 * @param file the file containing the object.
	 * @return the deserialized instance.
	 */
	public static Object deserialize(File file) {
		if (!file.exists()) return null;
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(file));
			Object object = in.readObject();
			in.close();
			return object;
		}
		catch (Exception ex) {
			logger.warn("Unable to deserialize "+file, ex);
			if (in != null) {
				try { in.close(); }
				catch (Exception ignore) { }
			}
			return null;
		}
	}

	/**
	 * Serialize the supplied object in a specified file.
	 * @param file the directory in which to store the serialized object.
	 * @param object the instance to be serialized.
	 */
	public static void serialize(File file, Object object) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(object);
			out.close();
		}
		catch(Exception ex) {
			logger.warn("Unable to serialize the object in "+file, ex);
			if (out != null) {
				try { out.close(); }
				catch (Exception ignore) { }
			}
		}
	}

}

