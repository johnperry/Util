/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

/**
 * A helper class to load the ImageIO Tools DLLs.
 */
public class ImageIOTools {

	static final Logger logger = Logger.getLogger(ImageIOTools.class);
	
	public static boolean areInstalled() {
		String javaHome = System.getProperty("java.home");
		File javaDir = new File(javaHome);
		File libDir = new File(javaDir, "lib");
		File extDir = new File(libDir, "ext");
		if (!extDir.exists()) return false;
		File clib = FileUtil.getFile(extDir, "clibwrapper_jiio", ".jar");
		File jai = FileUtil.getFile(extDir, "jai_imageio", ".jar");
		return ((clib != null) && (jai != null));
	}

	public static void load(File dir) {
		if (!areInstalled() && dir.exists()) {
			String thisOS = System.getProperty("os.name");
			String thisJavaBits = System.getProperty("sun.arch.data.model");
			File clib = FileUtil.getFile(dir, "clibwrapper_jiio", ".jar");
			File jai = FileUtil.getFile(dir, "jai_imageio", ".jar");
			boolean haveJARs = (clib != null) && (jai != null);
			if (haveJARs) {
				Hashtable<String,String> jaiManifest = JarUtil.getManifestAttributes(jai);
				String thisImageIOVersion  = jaiManifest.get("Implementation-Version");
				boolean ok = true;
				if (thisOS.contains("Windows") && thisJavaBits.equals("32")) {
					for (File file : dir.listFiles()) {
						if (file.getName().endsWith(".dll")) {
							ok &= loadNativeLib(file);
						}
					}
				}
				else if (thisOS.contains("Linux") && thisJavaBits.equals("32")) {
					File file = new File(dir, "libclib_jiio-32.so");
					ok = loadNativeLib(file);					
				}
				else if (thisOS.contains("Linux") && thisJavaBits.equals("64")) {
					File file = new File(dir, "libclib_jiio-64.so");
					ok = loadNativeLib(file);										
				}
				if (ok) logger.info("ImageIO Tools loaded (with native libraries): version "+thisImageIOVersion);
				else logger.info("ImageIO Tools loaded (JARs only): version "+thisImageIOVersion);
			}
			else logger.info("ImageIO Tools are not available");
		}
	}
	
	public static boolean loadNativeLib(File lib) {
		try { 
			System.load(lib.getAbsolutePath());
			return true;
		}
		catch (Exception ex) {
			logger.warn("Unable to load "+lib.getName());
			return false;
		}
	}
	
	public static String listAvailableCodecs() {
		Hashtable<String,String> codecs = new Hashtable<String,String>();
		for (String reader : ImageIO.getReaderFormatNames()) {
			reader = reader.toUpperCase();
			codecs.put(reader, "R");
		}
		for (String writer : ImageIO.getWriterFormatNames()) {
			writer = writer.toUpperCase();
			String rw = codecs.get(writer);
			if (rw == null) codecs.put(writer, "W");
			else codecs.put(writer, "R/W");
		}
		String[] keys = new String[codecs.size()];
		keys = codecs.keySet().toArray(keys);
		Arrays.sort(keys);
		StringBuffer sb = new StringBuffer();
		for (String key : keys) {
			String rw = codecs.get(key);
			sb.append(String.format("%-4s%s\n",rw,key));
		}
		return sb.toString();
	}

}
