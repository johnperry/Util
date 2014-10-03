/*---------------------------------------------------------------
*  Copyright 2014 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.servlets;

import java.io.File;
import java.util.*;
import org.rsna.server.*;

/**
 * The PingServlet. This servlet returns a text/plain
 * response containing the date/time.
 */
public class PingServlet extends Servlet {

	/**
	 * Construct a PingServlet.
	 * @param root the root directory of the server.
	 * @param context the path identifying the servlet.
	 */
	public PingServlet(File root, String context) {
		super(root, context);
	}

	/**
	 * The GET handler: return the date/time.
	 * @param req the request object
	 * @param res the response object
	 */
	public void doGet(HttpRequest req, HttpResponse res) {
		Date date = new Date();
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);

		TimeZone tz = TimeZone.getDefault();
		boolean inDST = tz.inDaylightTime(date);
		int zoneOffset = tz.getOffset(date.getTime());
		String zoneName = tz.getDisplayName(inDST, TimeZone.SHORT);
		String zoneID = tz.getID();

		Calendar gmt = Calendar.getInstance();
		gmt.setTimeInMillis(date.getTime() - zoneOffset);
		int gmtHour = gmt.get(Calendar.HOUR_OF_DAY);

		String response = String.format(
			"%04d.%02d.%02d %02d:%02d:%02d %s %s [%02d%02dZ]",
			year, month, day, hour, minute, second, zoneName, zoneID, gmtHour, minute
		);

		res.write(response);
		res.setContentType("txt");
		res.disableCaching();
		res.send();
	}
}
