/*---------------------------------------------------------------
*  Copyright 2011 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A helper class for working with dates.
 */
public class DateUtil {

	/**
	 * Get a GregorianCalendar for a specific date in DICOM format (YYYYMMDD).
	 * @param date the date in DICOM format.
	 * @return the calendar for the date string.
	 * @throws Exception if the date is in an illegal format.
	 */
	public static GregorianCalendar getCalendar(String date) throws Exception {
		//do a little filtering to protect against the most common booboos
		date = date.replaceAll("\\D","");
		if (date.length() != 8) throw new Exception("Illegal date: "+date);
		if (date.startsWith("00")) date = "19" + date.substring(2);
		//now make the calendar
		int year = Integer.parseInt(date.substring(0,4));
		int month = Integer.parseInt(date.substring(4,6));
		int day = Integer.parseInt(date.substring(6,8));
		return new GregorianCalendar(year,month-1,day);
	}

	/**
	 * Get a time in time in DICOM format.
	 * @return the time in seconds for the time string,
	 * or zero if the string cannot be parsed as a time.
	 * @throws Exception if the time is in an illegal format.
	 */
	public static long getTime(String time) {
		time.replaceAll(":", "");
		long hr = Long.parseLong(time.substring(0, 2));
		long min = Long.parseLong(time.substring(2, 4));
		long sec = (long)Float.parseFloat(time.substring(4));
		return sec + (60 * min) + (60 * 60 * hr);
	}

}
