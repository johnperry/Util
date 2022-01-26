/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.servlets;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.rsna.util.ClasspathUtil;
import org.rsna.util.FileUtil;
import org.rsna.util.XmlUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The Logger Level Servlet.
 * This servlet provides a browser-accessible
 * user interface for setting logger levels.
 */
public class LoggerLevelServlet extends Servlet {

	static final Logger logger = Logger.getLogger(LoggerLevelServlet.class);
	String home = "/";
	static HashSet<String> classes = null;


	/**
	 * Construct a LoggerLevelServlet.
	 * @param root the root directory of the server.
	 * @param context the path identifying the servlet.
	 */
	public LoggerLevelServlet(File root, String context) {
		super(root, context);
		if (classes == null) classes = ClasspathUtil.getInstance().getClassNames();
	}

	/**
	 * The servlet method that responds to an HTTP GET.
	 * This method returns an HTML page containing a form for
	 * specifying a class and logger level.
	 * @param req the request object.
	 * @param res the response object.
	 */
	public void doGet(HttpRequest req, HttpResponse res) throws Exception {
		res.setContentEncoding(req);

		//Require that the user have the admin role
		if (!req.userHasRole("admin")) {
			res.setResponseCode(res.notfound);
			res.send();
			return;
		}

		if (req.hasParameter("suppress")) home = "";

		//Make the page and return it.
		Document xml = XmlUtil.getDocument();
		Element root = xml.createElement("Classes");
		xml.appendChild(root);

		//Put in the class names
		String[] paths = new String[classes.size()];
		paths = classes.toArray(paths);
		Arrays.sort(paths, new NameComparator());
		for (String path : paths) {
			String name = path.substring( path.lastIndexOf(".")+1 );
			Element c = xml.createElement("Class");
			c.setAttribute("name", name);
			c.setAttribute("path", path);
			root.appendChild(c);
		}

		Document xsl = getDocument("LoggerLevelServlet.xsl");
		String[] params = new String[] { "home", home };
		res.write( XmlUtil.getTransformedText( xml, xsl, params ) );
		res.setContentType("html");
		res.disableCaching();
		res.send();
	}

	/**
	 * The servlet method that responds to an HTTP POST.
	 * This method interprets the posted parameters as a
	 * fully qualified class name and a logger level.
	 * It loads the class, sets the logger level, and
	 * then returns an HTML page containing a new form.
	 * @param req the request object.
	 * @param res the response object.
	 */
	public void doPost(HttpRequest req, HttpResponse res) throws Exception {

		//Make sure the user is authorized to do this.
		if (!req.userHasRole("admin") || !req.isReferredFrom(context)) {
			res.setResponseCode(res.notfound);
			res.send();
			return;
		}

		//Get the class and level
		String theClass = req.getParameter("class", "").trim();
		String theLevel = req.getParameter("level", "INFO");
		if (!theClass.equals("")) {
			Logger.getLogger(theClass).setLevel( Level.toLevel(theLevel) );
			logger.warn(theClass+" logger level set to "+theLevel);
		}

		//Make a new page and return it.
		doGet(req, res);
	}

	private Document getDocument(String name) throws Exception {
		File file = new File(root, name);
		InputStream in = FileUtil.getStream(file, "/"+name);
		return XmlUtil.getDocument(in);
	}

	class NameComparator implements Comparator<String> {
		public int compare(String s1, String s2) {
			String n1 = s1.substring( s1.lastIndexOf(".")+1 );
			String n2 = s2.substring( s2.lastIndexOf(".")+1 );
			return n1.compareTo(n2);
		}
	}

}











