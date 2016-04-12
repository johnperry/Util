/*---------------------------------------------------------------
*  Copyright 2016 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.ui;

import java.awt.*;
import java.util.Hashtable;
import java.net.URL;
import java.net.URI;

/**
 * A class to create and manage custom cursors.
 */
public class CustomCursors {
	
	private static CustomCursors instance = null;
	private Hashtable<String,Cursor> cursors = null;

	/**
	 * Protected constructor.
	 */
	protected CustomCursors() {
		if (cursors == null) {
			cursors = new Hashtable<String,Cursor>();
		}
	}

	/**
	 * Get the singleton instance of the CustomCursors.
	 * @return the singleton instance of this class, instantiating
	 * it if necessary.
	 */
	public static CustomCursors getInstance() {
		if (instance == null) {
			instance = new CustomCursors();
		}
		return instance;
	}

	/**
	 * Get a custom cursor, creating it if necessary from files
	 * in the cursors directory in the Util jar.
	 * @param name the name of the cursor (which is the name of 
	 * the PNG file in the cursors directory - without the ".png"
	 * extension.
	 * @return the cursor
	 */
	public Cursor getCursor(String name) {
		return getCursor(name, 0, 0);
	}
	
	/**
	 * Get a custom cursor, creating it if necessary from files
	 * in the cursors directory in the Util jar.
	 * @param name the name of the cursor (which is the name of 
	 * the PNG file in the cursors directory - without the ".png"
	 * extension.
	 * @param hotspotX the x coordinate of the cursor's hot spot
	 * @param hotspotY the y coordinate of the cursor's hot spot
	 * @return the cursor
	 */
	public Cursor getCursor(String name, int hotspotX, int hotspotY) {
		Cursor c = cursors.get(name);
		if (c == null) {
			Toolkit t = Toolkit.getDefaultToolkit();
				try {
				String path = "/cursors/"+name+".png";
				URL imgURL = this.getClass().getResource(path);
				Image img = t.getImage(imgURL);
				c = t.createCustomCursor(img, new Point(hotspotX,hotspotY), name);
				cursors.put(name, c);
			}
			catch (Exception ex) { ex.printStackTrace(); }
		}
		return c;
	}
	
}
