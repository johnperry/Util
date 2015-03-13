/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.*;
import java.util.Hashtable;
import java.util.zip.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ExcelWorksheet {
	
	Hashtable<String,String> cells;
	Hashtable<Integer,String> shared;

	/**
	 * Constructor: get a worksheet from an xlsx file.
	 * @param file the Excel spreadsheet file (must be an xlsx file)
	 * @param worksheet name (e.g., "sheet.xml", case-sensitive)
	 * @throws Exception if the worksheet cannot be obtained.
	 */
	public ExcelWorksheet(File file, String worksheet) throws Exception {
		ZipFile zipFile = new ZipFile(file);
		String stext = getEntryText(zipFile, "xl/sharedStrings.xml");
		String sheet = getEntryText(zipFile, "xl/worksheets/"+worksheet);
		zipFile.close();
		
		Document sdoc = XmlUtil.getDocument(stext);
		Element sroot = sdoc.getDocumentElement();
		shared = new Hashtable<Integer,String>();
		NodeList texts = sroot.getElementsByTagName("si");
		for (int i=0; i<texts.getLength(); i++) {
			Element si = (Element)texts.item(i);
			NodeList tList = si.getElementsByTagName("t");
			String t = (tList.getLength() > 0) ? tList.item(0).getTextContent().trim() : "";
			shared.put( new Integer(i), t);
		}		
		
		Document doc = XmlUtil.getDocument();
		Element root = doc.getDocumentElement();
		cells = new Hashtable<String,String>();
		NodeList cellList = root.getElementsByTagName("c");
		for (int i=0; i<cellList.getLength(); i++) {
			Element c = (Element)cellList.item(i);
			String r = c.getAttribute("r");
			String t = c.getAttribute("t");
			NodeList vList = c.getElementsByTagName("v");
			String v = (vList.getLength() > 0) ? vList.item(0).getTextContent().trim() : "";
			if (t.equals("s")) {
				try { v = shared.get( new Integer(v) ); }
				catch (Exception ignore) { }
			}
			cells.put(r, v);
		}
	}
	
	/**
	 * Search a row and return the column identifier (e.g., "A", "B", etc.) of the
	 * cell containing the specified text. Note: this method only searches the first
	 * 26 columns ("A" through "Z").
	 * @param row the row to search
	 * @param text the text to match (case-sensitive)
	 * @return the column identifier, or the empty string if no cell matches the
	 * specified text.
	 */
	private String getColumn(int row, String text) {
		for (int i=0; i<26; i++) {
			String col = Character.toString( (char)('A'+i) );
			String cell = getCell( col + row );
			if ((cell != null) && cell.equals(text)) return col;
		}
		return "";
	}

	private String getEntryText(ZipFile zipFile, String name) throws Exception {
		ZipEntry entry = zipFile.getEntry(name);
		StringWriter sw = new StringWriter();
		BufferedReader in = null;
		in = new BufferedReader(
					new InputStreamReader(zipFile.getInputStream(entry), FileUtil.utf8));
		int n = 0;
		char[] cbuf = new char[1024];
		while ((n = in.read(cbuf, 0, cbuf.length)) != -1) sw.write(cbuf, 0, n);
		in.close();
		return sw.toString();
	}
	
	public String getCell(String adrs) {
		String value = cells.get(adrs);
		return (value != null) ? value : "";
	}
}