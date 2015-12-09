/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.File;
import java.util.Hashtable;
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
				if (thisOS.contains("Windows") && thisJavaBits.equals("32")) {
					boolean ok = true;
					for (File file : dir.listFiles()) {
						if (file.getName().endsWith(".dll")) {
							try { System.load(file.getAbsolutePath()); }
							catch (Exception ex) {
								ok = false;
								logger.warn("Unable to load "+file.getName());
							}
						}
					}
					if (ok) logger.info("ImageIO Tools loaded (with DLLs): version "+thisImageIOVersion);
					else logger.info("ImageIO Tools loaded (JARs only): version "+thisImageIOVersion);
				}
				else logger.info("ImageIO Tools loaded (JARs only): version "+thisImageIOVersion);
			}
		}
	}
}
