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
	static ClasspathUtil cpu = new ClasspathUtil();

	/**
	 * Get the current classpath.
	 * @return the current classpath or the empty array if an error occurs.
	 */
	public static URL[] getClasspath() {
		URLClassLoader cl = (URLClassLoader) cpu.getClass().getClassLoader();
		try {
			return cl.getURLs();
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
