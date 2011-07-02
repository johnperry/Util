/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.server;

import org.apache.log4j.Logger;

/**
 * A class to provide access to path segments in a URL.
 */
public class Path {

	static final Logger logger = Logger.getLogger(Path.class);

	public String[] elements;

	/**
	 * Construct a Path from a string.
	 * @param path the path string to parse.
	 */
	public Path(String path) {
		if (path == null) {
			elements = new String[0];
		}
		else {
			path = path.trim();
			path = path.replace( "\\", "/" );
			if (path.startsWith("/")) path = path.substring(1);
			elements = path.split("/");
		}
	}

	/**
	 * Get the number of segments in the path.
	 * @return the length of the elements array.
	 */
	public int length() {
		return elements.length;
	}

	/**
	 * Get the full path of this Path.
	 * This is a convenience method that just calls getSubpath(0).
	 * @return the full path.
	 */
	public String path() {
		return subpath(0);
	}

	/**
	 * Get a path element.
	 * @param index the index of the requested path element.
	 * @return elements[index], or the empty string if index is out of bounds.
	 */
	public String element(int index) {
		if ((index>=0) && (index<elements.length)) return elements[index];
		else return "";
	}

	/**
	 * Get a path string starting at a specified element.
	 * @param startingElement the index of the first element in the subpath.
	 * @return the subpath starting at the specified element, always with a leading slash
	 * and no trailing slash (unless the path contains no elements, in which case a single
	 * slash is returned).
	 */
	public String subpath(int startingElement) {
		if (elements.length == 0) return "/";
		String path = "";
		for (int i=startingElement; i<elements.length; i++) {
			path += "/" + elements[i];
		}
		return path;
	}

	/**
	 * Get a path string starting at a specified element and ending at a specified element,
	 * (including both the first and last element specified).
	 * @param first the index of the first element in the subpath.
	 * @param last the index of the last element in the subpath.
	 * @return the subpath, always with a leading slash and no trailing slash
	 * (unless the path contains no elements, in which case a single
	 * slash is returned).
	 */
	public String subpath(int first, int last) {
		if (elements.length == 0) return "/";
		if (first < 0) first = 0;
		if (first >= elements.length) first = elements.length - 1;
		if (last >= elements.length) last = elements.length - 1;
		if (last < first) last = first;
		String path = "";
		for (int i=first; i<=last; i++) {
			path += "/" + elements[i];
		}
		return path;
	}
}
