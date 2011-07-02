/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.servlets;

import java.io.File;
import java.util.*;
import org.apache.log4j.Logger;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;
import org.rsna.util.HtmlUtil;

/**
 * The SysPropsServlet.
 */
public class SysPropsServlet extends Servlet {

	static final Logger logger = Logger.getLogger(SysPropsServlet.class);
	private static final Runtime runtime = Runtime.getRuntime();
	private static long usedMemory() {
		return runtime.totalMemory() - runtime.freeMemory();
	}

	/**
	 * Construct a SysPropsServlet.
	 * @param root the root directory of the server.
	 * @param context the path identifying the servlet.
	 */
	public SysPropsServlet(File root, String context) {
		super(root, context);
	}

	/**
	 * The GET handler: return a page displaying the system properties.
	 * @param req the request object
	 * @param res the response object
	 */
	public void doGet(HttpRequest req, HttpResponse res) {
		res.disableCaching();
		boolean admin = req.userHasRole("admin");
		if (admin && (req.getParameter("gc") != null)) collect();
		String home = req.getParameter("home", "/");
		res.write(getPage(admin, home));
		res.setContentType("html");
		res.send();
	}

	//Create an HTML page containing the data.
	private String getPage(boolean admin, String home) {
		String page =
				"<html>\n"
			+	" <head>\n"
			+	"  <title>System Properties</title>\n"
			+	"  <link rel=\"Stylesheet\" type=\"text/css\" media=\"all\" href=\"/JSPopup.css\"></link>\n"
			+	"  <style>\n"
			+	"   body {background-color:#c6d8f9; margin:0; padding:0;}\n"
			+	"   h1 {text-align:center;}\n"
			+	"   td {padding-left:5; padding-right:5;}\n"
			+	"  </style>\n"
			+	"   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/JSUtil.js\">;</script>\n"
			+	"   <script language=\"JavaScript\" type=\"text/javascript\" src=\"/JSPopup.js\">;</script>\n"
			+	"	<script>\n"
			+	"	 var tools = new Array( new PopupTool(\"/icons/home.png\", \"Return to the home page\", \""+home+"\", null) );\n"
			+	"	 function loaded() { setPopupToolPanel( tools ); }\n"
			+	"    window.onload = loaded;\n"
			+	"   </script>\n"
			+	" </head>\n"
			+	" <body>\n"
			+	HtmlUtil.getCloseBox(home)
			+ 	" <h1>System Properties</h1>\n"
			+	"  <center>\n"
			+	"   <table border=\"1\">\n"
			+	     displayProperties(admin, home)
			+	"   </table>\n"
			+	"  </center>\n"
			+	" </body>\n"
			+	"</html>\n";
		return page;
	}

	//Return a String containing an HTML table row containing
	//the current memory in use, with a link allowing garbage
	//collection if the user has admin privileges.
	private void displayMemory(StringBuffer sb, boolean admin, String home) {
		Formatter formatter = new Formatter(sb);
		sb.append( "<tr><td>MEMORY IN USE</td><td>" );
		formatter.format("%,d", usedMemory());
		sb.append( (admin ? " (<a href=\"?gc&home="+home+"\">collect garbage</a>)\n" : "\n") );
		sb.append( "</td></tr>\n" );
		sb.append( "<tr><td>TOTAL MEMORY</td><td>"  );
		formatter.format("%,d", runtime.totalMemory());
		sb.append( "</td></tr>" );
	}

	//Return a String containing the HTML rows of a table
	//displaying all the Java System properties.
	private String displayProperties(boolean admin, String home) {
		String v;
		String sep = System.getProperty("path.separator",";");
		StringBuffer sb = new StringBuffer();
		displayMemory(sb, admin, home);

		Properties p = System.getProperties();
		String[] n = new String[p.size()];
		Enumeration e = p.propertyNames();
		for (int i=0; i< n.length; i++) n[i] = (String)e.nextElement();
		Arrays.sort(n);
		for (int i=0; i<n.length; i++) {
			v = p.getProperty(n[i]);

			//Make path and dirs properties more readable by
			//putting each element on a separate line.
			if (n[i].endsWith(".path") ||
				n[i].endsWith(".dirs"))
					v = v.replace(sep, sep+"<br/>");

			//Make definition, access, and loader properties more
			//readable by putting each element on a separate line.
			if (n[i].endsWith(".definition") ||
				n[i].endsWith(".access") ||
				n[i].endsWith(".loader"))
					v = v.replace(",", ",<br>");

			sb.append( "<tr><td>" + n[i] + "</td><td>" + v + "</td></tr>\n" );
		}
		return sb.toString();
	}

	//Collect the garbage.

	private static void collect() {
		int i;
		long usedMemory1 = usedMemory();
		long usedMemory2 = Long.MAX_VALUE;
		for (i=0; (usedMemory1 < usedMemory2) && (i < 40); i++) {
			runtime.runFinalization();
			runtime.gc();
			Thread.currentThread().yield();
			usedMemory2 = usedMemory1;
			usedMemory1 = usedMemory();
		}
	}
}
