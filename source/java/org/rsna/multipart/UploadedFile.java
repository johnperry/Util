/*---------------------------------------------------------------
*  Copyright 2010 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.multipart;

import java.io.File;

/**
 * Encapsulate a single uploaded file.
 */
public class UploadedFile {

	private String paramName;
	private File file;
	private String path;
	private String type;

	/**
	 * Encapsulate information about an uploaded file in a multipart request.
	 *
	 * @param paramName the name of the parameter for the file in the request.
	 * @param file a File object pointing to the stored file.
	 * @param path the path information supplied in the request.
	 * @param type the content type specified for the file in the request.
	 */
	public UploadedFile(String paramName, File file, String path, String type) {
		this.paramName = paramName;
		this.file = file;
		this.path = path;
		this.type = type;
	}

	/**
	 * Get the multipart form's parameter name for the file..
	 *
	 * @return the parameter name of the uploaded file.
	 */
	public String getParameterName() {
		return paramName;
	}

	/**
	 * Get a File object for the specified file saved on the server's
	 * filesystem, or null if the file was not included in the upload.
	 *
	 * @return a File object for the named file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Get the original path information of the specified file (before any
	 * renaming policy was applied), or null if the file was not included in
	 * the upload.  A filesystem name is the name specified by the user.
	 *
	 * @return the path information from the content-disposition header of the file.parameter.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Get the content type of the specified file (as supplied by the
	 * client browser), or null if the file was not included in the upload.
	 *
	 * @return the content type of the file.
	 */
	public String getContentType() {
		return type;
	}
}
