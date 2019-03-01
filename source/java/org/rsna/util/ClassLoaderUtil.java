/*---------------------------------------------------------------
*  Copyright 2019 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;

/**
 * A utility to instantiate a URLClassLoader that finds classes 
 * in an array of JARs found in a directory tree.
 */
public class ClassLoaderUtil {
	
	/**
	 * Get a URLClassLoader initialized to a set of files and directories.
	 * This method places all individual files in the array of JARs.
	 * It searches any directories to find all the files that end in ".jar"
	 * (not case sensitive) and places them in the array as well.
	 * @param files the array of File objects to include in the array of JARs.
	 * The items in the array can be individual JAR files or directories.
	 * @return a URLClassLoader initialized to the set of JARs found in the
	 * files array.
	 */
	public static URLClassLoader getInstance(File[] files) {
		URL[] urls = getJars(files);
		return new URLClassLoader(urls);
	}

	private static URL[] getJars(File[] files) {
		LinkedList<URL> urlList = new LinkedList<URL>();
		for (File file : files) addJars(urlList, file);
		URL[] urls = new URL[urlList.size()];
		urls = urlList.toArray(urls);
		return urls;
	}
	
	private static void addJars(LinkedList<URL> urlList, File file) {
		if (file.exists()) {
			if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
				try { urlList.add( file.toURI().toURL() ); }
				catch (Exception skip) {
					System.out.println("Unable to add file to classpath: "+file);
				}
			}
			else if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) addJars(urlList, f);
			}
		}
	}
}
