/*---------------------------------------------------------------
*  Copyright 2014 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.util.Arrays;
import java.util.Hashtable;

/**
 * A singleton log of server attacks.
 */
public class AttackLog {

	static AttackLog attackLog = null;
	private Hashtable<String,Attack> attackTable;

	/**
	 * The protected constructor to prevent instantiation of
	 * the class except through the getInstance() method.
	 */
	protected AttackLog() {
		this.attackTable = new Hashtable<String,Attack>();
	}

	/**
	 * Get the AttackLog instance, creating it if it does not exist.
	 * @return the AttackLog instance.
	 */
	public synchronized static AttackLog getInstance() {
		if (attackLog == null) attackLog = new AttackLog();
		return attackLog;
	}

	/**
	 * Add an attack to the AttackLog.
	 * @param ip the IP address of the attacker.
	 */
    public synchronized void addAttack(String ip) {
		Attack attack = attackTable.get(ip);
		if (attack == null) attack = new Attack(ip);
		attack.increment();
		attack.setLast(System.currentTimeMillis());
		attackTable.put(ip, attack);
	}

	/**
	 * Get the array of attacks, sorted in reverse chronological order by last attack.
	 * @return the sorted array of attacks; where the array is populated with new
	 * instances, protecting the ones in the AttackLog.
	 */
	public synchronized Attack[] getAttacks() {
		Attack[] attacks = new Attack[attackTable.size()];
		attacks = attackTable.values().toArray(attacks);
		Attack[] atks = new Attack[attacks.length];
		for (int i=0; i<atks.length; i++) {
			atks[i] = new Attack(attacks[i]);
		}
		Arrays.sort(atks);
		return atks;
	}

}
