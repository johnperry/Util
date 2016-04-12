package org.rsna.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

public class ClasspathUtil {

	private static final Class<?>[] parameters = new Class[] { URL.class };
	private static final Class<?>[] noparams = new Class[0];

	/**
	 * Add all the jar files in a directory tree to the classpath.
	 * @param dir the top-level directory in the tree
	 */
	public static void addJARs(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					if (file.getName().toLowerCase().endsWith(".jar") && !addFile(file)) {
						System.out.println("Unable to add "+file+" to the classpath.");
					}
				}
				else if (file.isDirectory()) addJARs(file);
			}
		}
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
	 * List the current classpath.
	 * @return the current classpath in a readable string.
	 */
	public static String listClasspath() {
		StringBuffer sb = new StringBuffer();
		URL[] urls = getClasspath();
		for (URL url : urls) {
			sb.append(url.toString()+"\n");
		}
		return sb.toString();
	}

	/**
	 * Get a Set of class names available on the current classpath.
	 * @return the set of class names.
	 */
	public static HashSet<String> getClassNames() {
		HashSet<String> names = new HashSet<String>();
		URL[] urls = getClasspath();
		for (URL url : urls) {
			try {
				JarFile jar = new JarFile( new File( url.toURI() ) );
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.endsWith(".class")
						&& !name.contains("$")
							&& (name.startsWith("org/rsna") || name.startsWith("mirc"))) {
						name = name.substring(0, name.length() - ".class".length());
						name = name.replace("/", ".");
						names.add(name);
					}
				}
			}
			catch (Exception ignore) { }
		}
		return names;
	}



}
