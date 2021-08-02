/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * A JTextPane that supports text color and includes thread-safe methods.
 */
public class ColorPane extends JTextPane {

	int lineHeight;
	boolean trackWidth = true;

	/**
	 * Create a ColorPane.
	 */
	public ColorPane() {
		this("");
	}

	/**
	 * Create a ColorPane with the specified initial text.
	 * @param text the text
	 */
	public ColorPane(String text) {
		super();
		setFont(new Font("Monospaced",Font.PLAIN,12));
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		setText(text);
	}

	/**
	 * Set the font and update the line height.
	 */
	public void setFont(Font font) {
		FontMetrics fm = getFontMetrics(font);
		lineHeight = fm.getHeight();
		super.setFont(font);
	}

	/**
	 * Get the line height of the current font.
	 * @return the line height of the current font.
	 */
	public int getLineHeight() {
		return lineHeight;
	}

	/**
	 * Get the flag that indicates whether the pane is to track the width
	 * of its container.
	 * @return true if the ColorPane tracks the viewport width; false otherwise.
	 */
	public boolean getScrollableTracksViewportWidth() {
		return trackWidth;
	}

	/**
	 * Set the flag that indicates whether the pane is to track the width
	 * of its container.
	 * @param trackWidth true if the ColorPane is to track the viewport width; false otherwise.
	 */
	public void setScrollableTracksViewportWidth(boolean trackWidth) {
		this.trackWidth = trackWidth;
	}

	/**
	 * Remove all the text from the pane. This method is thread safe.
	 */
	public void clear() {
		if (SwingUtilities.isEventDispatchThread()) {
			super.setText("");
		}
		else {
			final JTextPane jtp = this;
			Runnable r = new Runnable() {
				public void run() {
					jtp.setText("");
				}
			};
			SwingUtilities.invokeLater(r);
		}
	}

	/**
	 * Set text with the current color. This method is thread safe.
	 * @param s the string to set in the ColorPane.
	 */
	public void setText(String s) {
		clear();
		print(s);
	}

	/**
	 * Set text with the specified color. This method is thread safe.
	 * @param c the color to be used for this and subsequent insertions.
	 * @param s the string to set in the ColorPane.
	 */
	public void setText(Color c, String s) {
		clear();
		print(c, s);
	}

	/**
	 * Append a string with the current color. This method is thread safe.
	 * @param s the string to set in the ColorPane.
	 */
	public void print(String s) {
		if (SwingUtilities.isEventDispatchThread()) {
			append(s);
		}
		else {
			final String ss = s;
			Runnable r = new Runnable() {
				public void run() {
					append(ss);
				}
			};
			SwingUtilities.invokeLater(r);
		}
	}

	/**
	 * Append a string with the specified color. This method is thread safe.
	 * @param c the color to be used for this and subsequent insertions.
	 * @param s the string to set in the ColorPane.
	 */
	public void print(Color c, String s) {
		if (SwingUtilities.isEventDispatchThread()) {
			append(c, s);
		}
		else {
			final Color cc = c;
			final String ss = s;
			Runnable r = new Runnable() {
				public void run() {
					append(cc, ss);
				}
			};
			SwingUtilities.invokeLater(r);
		}
	}

	/**
	 * Append a string with the current color, and add a newline. This method is thread safe.
	 * @param s the string to set in the ColorPane.
	 */
	public void println(String s) {
		print(s + "\n");
	}

	/**
	 * Append a string with the specified color, and add a newline. This method is thread safe.
	 * @param c the color to be used for this and subsequent insertions.
	 * @param s the string to set in the ColorPane.
	 */
	public void println(Color c, String s) {
		print(c, s + "\n");
	}

	/**
	 * Append a string with the current color, and add a newline. This method is not thread safe.
	 * This method is public only for backward compatibility; use println instead.
	 * @param s the string to set in the ColorPane.
	 */
	public void appendln(String s) {
		append(s + "\n");
	}

	/**
	 * Append a string with the specified color, and add a newline. This method is not thread safe.
	 * This method is public only for backward compatibility; use println instead.
	 * @param c the color to be used for this and subsequent insertions.
	 * @param s the string to set in the ColorPane.
	 */
	public void appendln(Color c, String s) {
		append(c, s + "\n");
	}

	/**
	 * Append a string with the current color. This method is not thread safe.
	 * This method is public only for backward compatibility; use print instead.
	 * @param s the string to set in the ColorPane.
	 */
	public synchronized void append(String s) {
		int len = getDocument().getLength(); // same value as getText().length();
		setCaretPosition(len);  // place caret at the end (with no selection)
		replaceSelection(s); // there is no selection, so inserts at caret
	}

	/**
	 * Append a string with the specified color. This method is not thread safe.
	 * This method is public only for backward compatibility; use print instead.
	 * @param c the color to be used for this and subsequent insertions.
	 * @param s the string to set in the ColorPane.
	 */
	public synchronized void append(Color c, String s) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
		int len = getDocument().getLength();
		setCaretPosition(len);
		setCharacterAttributes(aset, false);
		replaceSelection(s);
	}
}
