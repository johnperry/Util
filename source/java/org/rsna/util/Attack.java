/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

/**
 * An AttackLog entry, capturing the IP of the attack, the number
 * of attacks from that IP, and the time of the most recent attack.
 */
public class Attack implements Comparable<Attack> {

	private String ip = "";
	private String city = "";
	private String region = "";
	private String country = "";
	private int count = 0;
	private long last = 0;

	/**
	 * Create a new Attack for an IP address, initializing
	 * the count and the last time to zero.
	 */
	public Attack(String ip) {
		this.ip = ip;
		this.count = 0;
		this.last = 0;
	}

	/**
	 * Create a new Attack by copying the parameters from
	 * another Attack.
	 */
	public Attack(Attack attack) {
		this.ip = attack.getIP();
		this.city = attack.getCity();
		this.region = attack.getRegion();
		this.country = attack.getCountry();
		this.count = attack.getCount();
		this.last = attack.getLast();
	}

	/**
	 * Get the IP address of the attacker.
	 */
	public String getIP() {
		return ip;
	}

	/**
	 * Get the country of the attacker.
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Get the city of the attacker.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Get the city of the attacker.
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * Get the number of attacks received from the attacker.
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Get the system time of the last attack from the attacker.
	 */
	public long getLast() {
		return last;
	}

	/**
	 * Increment the attack count.
	 */
	public void increment() {
		count++;
	}

	/**
	 * Set the city.
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Set the region.
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * Set the country.
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * Set the country.
	 */
	public void setLast(long last) {
		this.last = last;
	}

	/**
	 * Implement the Comparable<Attack> interface, sorting in reverse
	 * chronological order by last attack time..
	 */
	public int compareTo(Attack attack) {
		long x = attack.getLast();
		if (last < x) return 1;
		if (last > x) return -1;
		return 0;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("IP: "+ip+"\n");
		sb.append("  city:    "+city+"\n");
		sb.append("  region:  "+region+"\n");
		sb.append("  country: "+country+"\n");
		sb.append("  count:   "+count+"\n");
		sb.append("  last:    "+StringUtil.getDateTime(last, " ")+"\n");
		return sb.toString();
	}

}

