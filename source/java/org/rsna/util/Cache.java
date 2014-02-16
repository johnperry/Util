/*---------------------------------------------------------------
*  Copyright 2012 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.*;

/**
 * A singleton cache for files that are served from the classpath jars.
 */
public class Cache {

	static Cache cache = null;
	static File dir = null;

	/**
	 * The protected constructor to prevent instantiation of
	 * the class except through the getInstance() method.
	 * @param dir the base directory of the cache.
	 */
	protected Cache(File dir) {
		this.dir = dir;
	}

	/**
	 * Get the Cache instance, creating it if it does not exist.
	 * This method is intended only for use during initialization.
	 * If the cache has already been created, this method simply
	 * returns the existing instance; it does not change the
	 * cache directory.
	 * @param dir the base directory of the cache.
	 * @return the Cache instance.
	 */
	public synchronized static Cache getInstance(File dir) {
		if (cache == null) cache = new Cache(dir);
		return cache;
	}

	/**
	 * Get the Cache instance, returning null if it does not exist.
	 * @return the Cache instance, or null, if the Cache has not
	 * been instantiated..
	 */
	public synchronized static Cache getInstance() {
		return cache;
	}

	/**
	 * Clear the cache.
	 */
	public synchronized void clear() {
		FileUtil.deleteAll(dir);
	}

	/**
	 * Get a File pointing to a resource in the cache, loading the
	 * resource from the classpath if the resource is not in the cache.
	 * @param path the relative path to the resource. This is the same path
	 * that is used to load the resource if it does not exist in the cache.
	 * @return the File pointing to the resource in the cache, or null if
	 * the resource cannot be obtained from the classpath.
	 */
    public synchronized File getFile(String path) {
		if (path.startsWith("/")) path = path.substring(1);
		File file = new File(dir, path);
		return FileUtil.getFile(file, path);
	}

	/**
	 * Load all the files in a zip file into the cache, preserving the
	 * directory structure of the zip file, and ignoring any .class files.
	 * @param file the zip file to unpack.
	 */
	public synchronized int load(File file) {
		int count = 0;
		ZipFile zipFile = null;
		try {
			if (file.exists()) {
				zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
				while (zipEntries.hasMoreElements()) {
					ZipEntry entry = zipEntries.nextElement();
					if (!entry.isDirectory()) {
						String name = entry.getName().replace('/', File.separatorChar);
						if (!name.endsWith(".class") && !name.startsWith("META-INF")) {
							File outFile = new File(dir, name);
							outFile.getParentFile().mkdirs();
							OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
							InputStream in = zipFile.getInputStream(entry);
							if (FileUtil.copy( in, out, -1 )) count++;;
						}
					}
				}
			}
		}
		catch (Exception ignore) { }
		FileUtil.close(zipFile);
		return count;
	}

}
