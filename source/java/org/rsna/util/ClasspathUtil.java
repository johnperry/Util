package org.rsna.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

public class ClasspathUtil {
	
	static ClasspathUtil cpu = null;
	
	HashSet<URL> urls = null;
	
	/**
	 * The protected constructor.
	 */
	protected ClasspathUtil() {
		urls = new HashSet<URL>();
	}
	
	/**
	 * Get the singleton instance of ClasspathUtil,
	 * instantiating it if it doesn't exist.
	 * @return the singleton instance of ClasspathUtil.
	 */
	public static ClasspathUtil getInstance() {
		if (cpu == null) {
			cpu = new ClasspathUtil();
		}
		return cpu;
	}

	/**
	 * Add the Class-Path from a jar file to the set of URLs.
	 * @param url the URL of the jar file from whose manifest
	 * the Class-Path attribute is to be accessed.
	 */
	public void addJarClasspath(URL url) {
		try {
			File file = new File( url.toURI() );
			if (file.exists()) {
				JarFile jar = new JarFile( file );
				Manifest manifest = jar.getManifest();
				urls.add(file.toURI().toURL()); //include the jar file itself
				String classPath = manifest.getMainAttributes().getValue("Class-Path");
				String[] paths = classPath.split("[\\s]+");
				File parent = file.getParentFile();
				for (String path : paths) {
					urls.add( (new File(parent, path)).toURI().toURL() );
				}
			}
		}
		catch (Exception ex) { }
	}

	/**
	 * Add the urls from a URLClassLoader to the set of URLs.
	 * @param urlClassLoader the classloader whose URLs are to be included.
	 */
	public void addClassLoaderURLs(URLClassLoader urlClassLoader) {
		for (URL url : urlClassLoader.getURLs()) {
			urls.add(url);
		}
	}

	/**
	 * List the URL resources.
	 * @return the URLs in a readable string.
	 */
	public String listResources() {
		String[] resources = new String[urls.size()];
		int k = 0;
		for (URL url : urls) resources[k++] = url.toString();
		Arrays.sort(resources);
		StringBuffer sb = new StringBuffer();
		for (String resource : resources) {
			sb.append(resource + "\n");
		}
		return sb.toString();
	}

	/**
	 * Get a Set of class names available in the resources.
	 * @return the set of class names found in the libraries.
	 */
	public HashSet<String> getClassNames() {
		HashSet<String> names = new HashSet<String>();
		for (URL url : urls) {
			try {
				JarFile jar = new JarFile( new File( url.toURI() ) );
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.endsWith(".class")
						&& !name.contains("$")
							&& (name.startsWith("org/rsna") || name.startsWith("mirc") || name.startsWith("edu"))) {
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
