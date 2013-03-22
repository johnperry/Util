/*---------------------------------------------------------------
*  Copyright 2013 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.util.*;
import javax.swing.*;

/**
 * Static methods for creating HTML elements.
 */
public class HtmlUtil {

	/**
	 * Generate a close box linking to the home page (/),
	 * using the _self target. This method calls the
	 * more general getCloseBox method.
	 * @return the HTML code for the close box div.
	 */
	public static String getCloseBox() {
		return getCloseBox("/", "_self", "Return to the home page");
	}

	/**
	 * Generate a close box linking to a specified URL,
	 * using the _self target. This method calls the
	 * more general getCloseBox method.
	 * @return the HTML code for the close box div.
	 */
	public static String getCloseBox(String url) {
		return getCloseBox(url, "_self", null);
	}

	/**
	 * Generate an HTML div with a close box.
	 * The div floats right and links to a new page.
	 * @param url the URL to load.
	 * @param target the target window.
	 * @param title the title for the close box.
	 * @return the HTML code for the close box div.
	 */
	public static String getCloseBox(String url, String target, String title) {
		if (url == null) url = "/";
		if (target == null) target = "_self";
		if (title == null) title = "Return to the home page";
		return
			"<div style=\"float:right;\">\n"
		  + " <img src=\"/icons/home.png\"\n"
		  + "  onclick=\"window.open('"+url+"','"+target+"');\"\n"
		  + "  title=\""+title+"\"\n"
		  + "  style=\"margin:2\"/>\n"
		  + "</div>\n";
	}

}