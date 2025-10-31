/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.rsna.util.FileUtil;
import org.rsna.util.HttpUtil;
import org.rsna.util.XmlUtil;

/**
 * A singleton log of server attacks.
 */
public class AttackLog {

	static final Logger logger = Logger.getLogger(AttackLog.class);
	static AttackLog attackLog = null;
	private Hashtable<String,Attack> attackTable;
	final int readTimeout = 60000;

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
		getInfo(attack);
		attackTable.put(ip, attack);
		logger.info("Attack logged:\n" + attack.toString());		
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

	private void getInfo(Attack attack) {
		if ((attack != null) && attack.getCountry().equals("")) {
			String ip = attack.getIP();
			String url = "https://secure.geobytes.com/GetCityDetails?key=7c756203dbb38590a66e01a5a3e1ad96&fqcn="+ip;
			try {
				HttpURLConnection conn = HttpUtil.getConnection(url);
				conn.setReadTimeout(readTimeout);
				conn.setRequestMethod("GET");
				conn.connect();
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					String result = FileUtil.getText( conn.getInputStream(), FileUtil.latin1 );
					JSONTable table = new JSONTable(result);
					attack.setCity(table.get("geobytescity"));
					attack.setRegion(table.get("geobytesregion"));
					attack.setCountry(table.get("geobytescountry"));
				}
			}
			catch (Exception skip) { }
		}
	}
	
/* Example result from geobytes (with newlines added for readability):

{"geobytesinternet":"AU","geobytescountry":"Australia","geobytesregionlocationcode":"AUSA",
"geobytesregion":"South Australia","geobytescode":"SA","geobyteslocationcode":"AUSAADEL",
"geobytescity":"Adelaide","geobytescityid":"1312","geobytesfqcn":"Adelaide, SA, Australia",
"geobyteslatitude":"-34.932999","geobyteslongitude":"138.600006","geobytescapital":"Canberra ",
"geobytestimezone":"138.6","geobytesnationalitysingular":"Australian","geobytespopulation":"19357594",
"geobytesnationalityplural":"Australians","geobytesmapreference":"Oceania ",
"geobytescurrency":"Australian dollar ","geobytescurrencycode":"AUD","geobytestitle":"Australia"}

*/
	class JSONTable extends Hashtable<String,String> {
		public JSONTable(String text) throws Exception {
			super();
			Pattern pattern = Pattern.compile("\"([^\"]*)\":\"([^\"]*)\"");
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				String key = matcher.group(1);
				String value = matcher.group(2);
				put(key, value);
			}
		}
	}
}
