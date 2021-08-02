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
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import org.apache.log4j.Logger;

/**
 * A helper class to load the ImageIO Tools DLLs.
 */
public class ImageIOTools {

	static final Logger logger = Logger.getLogger(ImageIOTools.class);
	static File installedJAI = null;
	
	public static boolean areInstalled() {
		String javaHome = System.getProperty("java.home");
		File javaDir = new File(javaHome);
		File libDir = new File(javaDir, "lib");
		File extDir = new File(libDir, "ext");
		if (!extDir.exists()) return false;
		File installedCLIB = FileUtil.getFile(extDir, "clibwrapper_jiio", ".jar");
		installedJAI = FileUtil.getFile(extDir, "jai_imageio", ".jar");
		return ((installedCLIB != null) && (installedJAI != null));
	}

	public static String load(File dir) {
		if (areInstalled()) {
			return getVersion(installedJAI) + " (installed)";
		}
		else if (dir.exists()) {
			String thisOS = System.getProperty("os.name");
			String thisJavaBits = System.getProperty("sun.arch.data.model");
			File clib = FileUtil.getFile(dir, "clibwrapper_jiio", ".jar");
			File jai = FileUtil.getFile(dir, "jai_imageio", ".jar");
			boolean haveJARs = (clib != null) && (jai != null);
			if (haveJARs) return getVersion(jai);
		}
		return "not available";
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
			reader = reader.toUpperCase().replace(" ","");
			if (reader.equals("TIF")) reader = "TIFF";
			codecs.put(reader, "R");
		}
		for (String writer : ImageIO.getWriterFormatNames()) {
			writer = writer.toUpperCase().replace(" ","");
			if (writer.equals("TIF")) writer = "TIFF";
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
	
	public static String listAvailableReadersAndWriters() {
		StringBuffer sb = new StringBuffer();
		sb.append("Readers:\n");
		for (String name : ImageIO.getReaderFormatNames()) {
			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(name);
			while (readers.hasNext()) {
				sb.append(String.format("%15s: %s\n", name, readers.next()));
			}
		}
		sb.append("\nWriters:\n");
		for (String name : ImageIO.getWriterFormatNames()) {
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(name);
			while (writers.hasNext()) {
				sb.append(String.format("%15s: %s\n", name, writers.next()));
			}
		}
		return sb.toString();
	}
	
	public static String getVersion(File jai) {
		try {
			Hashtable<String,String> jaiManifest = JarUtil.getManifestAttributes(jai);
			return jaiManifest.get("Implementation-Version");
		}
		catch (Exception unable) { return "?"; }
	}

}
