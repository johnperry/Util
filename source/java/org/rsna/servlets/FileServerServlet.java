/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.servlets;

import java.io.File;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;
import org.rsna.server.Path;
import org.rsna.util.FileUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The FileServerServlet.
 * This servlet provides a browser-accessible listing of files 
 * under the servlet's context directory under the root of the server.
 */
public class FileServerServlet extends Servlet {

	static final Logger logger = Logger.getLogger(FileServerServlet.class);
	String home = "/";


	/**
	 * Construct a FileServerServlet.
	 * @param root the root directory of the server.
	 * @param context the path identifying the servlet.
	 */
	public FileServerServlet(File root, String context) {
		super(root, context);
	}

	/**
	 * The servlet method that responds to an HTTP GET.
	 * This method returns an HTML page containing a table
	 * with links to all the files under the context.
	 * @param req the request object.
	 * @param res the response object.
	 */
	public void doGet(HttpRequest req, HttpResponse res) throws Exception {

		if (req.hasParameter("suppress")) home = "";
		
		Path path = req.getParsedPath();
		if (path.length() == 1) {
			//Make the page and return it.
			Document xml = XmlUtil.getDocument();
			Element xmlRoot = xml.createElement("Files");
			xml.appendChild(xmlRoot);

			//Put in the files
			File dir = new File(root, context);
			appendFiles(xmlRoot, dir, "/");
			
			if (req.hasParameter("xml")) {
				res.write(XmlUtil.toString(xmlRoot));
				res.setContentType("xml");
 			}
			else {
				Document xsl = getDocument("FileServerServlet.xsl");
				String[] params = new String[] { "home", home };
				res.write( XmlUtil.getTransformedText( xml, xsl, params ) );
				res.setContentType("html");
			}
			res.disableCaching();
			res.send();
		}
		else super.doGet(req, res);
	}

	private void appendFiles(Element el, File dir, String path) {
		Document doc = el.getOwnerDocument();
		File[] files = dir.listFiles();
		Element dirEl = doc.createElement("Directory");
		dirEl.setAttribute("name", dir.getName());
		dirEl.setAttribute("url", path + dir.getName() + "/");
		el.appendChild(dirEl);
		for (File file : files) {
			if (file.isFile()) {
				String name = file.getName();
				if (!file.isHidden() && !name.startsWith("~$")) {
					Element fileEl = doc.createElement("File");
					fileEl.setAttribute("name", name);
					fileEl.setAttribute("url", path + dir.getName() + "/" + name);
					long len = file.length();
					String lenString = "";
					if (len >= 1000000000L) lenString = String.format("%,d MB", len/1000000);
					else if (len >= 100000L) lenString = String.format("%,d KB", len/1000);
					else lenString = String.format("%,d B", len);
					fileEl.setAttribute("size", lenString);
					dirEl.appendChild(fileEl);
				}
			}
			else appendFiles(dirEl, file, path + dir.getName() + "/");			
		}
	}

	private Document getDocument(String name) throws Exception {
		File file = new File(root, name);
		InputStream in = FileUtil.getStream(file, "/"+name);
		return XmlUtil.getDocument(in);
	}

}











