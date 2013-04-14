/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.ui;

import java.awt.AWTEvent;
import java.io.File;

/**
 * The event that passes the result of a file operation to FileListeners.
 */
public class FileEvent extends AWTEvent {

	public static final int FILE_EVENT = AWTEvent.RESERVED_ID_MAX + 4269;

	/** ATTENTION type */
	public static final int ATTENTION = -32768;
	/** DELETE type */
	public static final int DELETE = -1;
	/** SELECT type */
	public static final int SELECT = 0;
	/** RENAME type */
	public static final int RENAME = 1;
	/** MOVE type */
	public static final int MOVE = 2;
	/** NOMOREFILES type */
	public static final int NO_MORE_FILES = 3;

	/** The type of FileEvent */
	public int type;

	/** The file before the event. */
	public File before;
	/** The file after the event. */
	public File after;

	/**
	 * Class constructor capturing a file event with no files.
	 * @param object the source of the event.
	 * @param type the type of file event.
	 */
	public FileEvent(Object object, int type) {
		this(object,type,null,null);
	}

	/**
	 * Class constructor capturing a file event with only one file.
	 * @param object the source of the event.
	 * @param type the type of file event.
	 * @param after the file after the event (may be null in some applications).
	 */
	public FileEvent(Object object, int type, File after) {
		this(object,type,null,after);
	}

	/**
	 * Class constructor capturing a file event with two files.
	 * @param object the source of the event.
	 * @param type the type of file event.
	 * @param before the file before the event (may be null in some applications).
	 * @param after the file after the event (may be null in some applications).
	 */
	public FileEvent(Object object, int type, File before, File after) {
		super(object, FILE_EVENT);
		this.type = type;
		this.before = before;
		this.after = after;
	}

}
