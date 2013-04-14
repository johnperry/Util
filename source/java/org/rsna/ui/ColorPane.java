/*---------------------------------------------------------------
*  Copyright 2013 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * A JTextPane that supports text color and includes thread-safe methods.
 */
public class ColorPane extends JTextPane {

	public int lineHeight;
	boolean trackWidth = true;

	/**
	 * Create a ColorPane.
	 */
	public ColorPane() {
		super();
		Font font = new Font("Monospaced",Font.PLAIN,12);
		FontMetrics fm = getFontMetrics(font);
		lineHeight = fm.getHeight();
		setFont(font);
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	}

	/**
	 * Get the flag that indicates whether the pane is to track the width
	 * of its container.
	 */
	public boolean getScrollableTracksViewportWidth() {
		return trackWidth;
	}

	/**
	 * Set the flag that indicates whether the pane is to track the width
	 * of its container.
	 */
	public void setScrollableTracksViewportWidth(boolean trackWidth) {
		this.trackWidth = trackWidth;
	}

	/**
	 * Remove all the text from the pane.
	 */
	public void clear() {
		setText("");
	}

	/**
	 * Append a string with the current color, and add a newline. This method is not thread safe.
	 * This method is public only for backward compatibility; use println instead.
	 */
	public void appendln(String s) {
		append(s + "\n");
	}

	/**
	 * Append a string with the specified color, and add a newline. This method is not thread safe.
	 * This method is public only for backward compatibility; use println instead.
	 */
	public void appendln(Color c, String s) {
		append(c, s + "\n");
	}

	/**
	 * Append a string with the current color. This method is not thread safe.
	 * This method is public only for backward compatibility; use println instead.
	 */
	public synchronized void append(String s) {
		int len = getDocument().getLength(); // same value as getText().length();
		setCaretPosition(len);  // place caret at the end (with no selection)
		replaceSelection(s); // there is no selection, so inserts at caret
	}

	/**
	 * Append a string with the specified color. This method is not thread safe.
	 * This method is public only for backward compatibility; use println instead.
	 */
	public synchronized void append(Color c, String s) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
		int len = getDocument().getLength();
		setCaretPosition(len);
		setCharacterAttributes(aset, false);
		replaceSelection(s);
	}

	/**
	 * Append a string with the current color. This method is thread safe.
	 */
	public void print(String s) {
		if (SwingUtilities.isEventDispatchThread()) {
			append(s);
		}
		else {
			final String ss = s;
			Runnable display = new Runnable() {
				public void run() {
					append(ss);
				}
			};
			SwingUtilities.invokeLater(display);
		}
	}

	/**
	 * Append a string with the specified color. This method is not thread safe.
	 */
	public void print(Color c, String s) {
		if (SwingUtilities.isEventDispatchThread()) {
			append(c, s);
		}
		else {
			final Color cc = c;
			final String ss = s;
			Runnable display = new Runnable() {
				public void run() {
					append(cc, ss);
				}
			};
			SwingUtilities.invokeLater(display);
		}
	}

	/**
	 * Append a string with the current color, and add a newline. This method is thread safe.
	 */
	public void println(String s) {
		print(s + "\n");
	}

	/**
	 * Append a string with the specified color, and add a newline. This method is thread safe.
	 */
	public void println(Color c, String s) {
		print(c, s + "\n");
	}

}
