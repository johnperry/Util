package org.rsna.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClasspathUtil {

	private static final Class<?>[] parameters = new Class[] { URL.class };
	private static final Class<?>[] noparams = new Class[0];

	/**
	 * Add all the jar files in a directory to the classpath.
	 * @param dir the directory
	 * @return true if all the jar files were successfully added; false otherwise.
	 */
	public static boolean addJARs(File dir) {
		boolean result = true;
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.getName().toLowerCase().endsWith(".jar") && !addFile(file)) {
					System.out.println("Unable to add "+file+" to the classpath.");
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * Add a file to the classpath.
	 * @param file the file to add
	 * @return true if the file was successfully added; false otherwise.
	 */
	public static boolean addFile(File file) {
		try { return addURL(file.toURI().toURL()); }
		catch (Exception ex) { return false; }
	}

	/**
	 * Add a URL to the classpath.
	 * @param url the URL to add
	 * @return true if the URL was successfully added; false otherwise.
	 */
	public static boolean addURL(URL url) {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke( sysloader, new Object[] { url } );
			return true;
		}
		catch (Throwable t) { return false; }
	}

	/**
	 * Get the current classpath.
	 * @return the current classpath or the empty array if an error occurs.
	 */
	public static URL[] getClasspath() {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("getURLs", noparams);
			method.setAccessible(true);
			return (URL[])method.invoke( sysloader, new Object[0] );
		}
		catch (Throwable t) { return new URL[0]; }
	}

	/**
	 * List the current classpath on System.out.
	 */
	public static void listClasspath() {
		URL[] urls = getClasspath();
		System.out.println("Classpath:");
		for (URL url : urls) {
			System.out.println("  "+url.toString());
		}
		System.out.println("end of classpath list");
	}

}
