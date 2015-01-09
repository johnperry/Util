/*---------------------------------------------------------------
*  Copyright 2014 by the Radiological Society of North America
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

	public final File file;

	/**
	 * Class constructor capturing a file event with two files.
	 * @param source the source of the event.
	 * @param file the file on which the event occurred.
	 */
	public FileEvent(Object source, File file) {
		super(source, FILE_EVENT);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

}
