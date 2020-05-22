/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ExcelWorksheet {
	
	Hashtable<String,String> cells;
	Hashtable<Integer,String> shared;
	String name;
	int lastRow = 0;
	String lastColumn = "";

	/**
	 * Constructor: get a worksheet from an xlsx file.
	 * @param file the Excel spreadsheet file (must be an xlsx file)
	 * @param worksheet name (e.g., "sheet.xml", case-sensitive)
	 * @throws Exception if the worksheet cannot be obtained.
	 */
	public ExcelWorksheet(File file, String worksheet) throws Exception {
		this.name = worksheet;
		ZipFile zipFile = new ZipFile(file);
		String stext = getEntryText(zipFile, "xl/sharedStrings.xml");
		String sheet = getEntryText(zipFile, "xl/worksheets/"+worksheet);
		zipFile.close();
		process(stext, sheet);
	}
		
	/**
	 * Constructor: get a worksheet from an input stream.
	 * @param in the input stream pointing to the Excel resource (must be an xlsx structure)
	 * @param worksheet name (e.g., "sheet.xml", case-sensitive)
	 * @throws Exception if the worksheet cannot be obtained.
	 */
	public ExcelWorksheet(String resource, String worksheet) throws Exception {
		this.name = worksheet;
		String stext = getEntryText(resource, "xl/sharedStrings.xml");
		String sheet = getEntryText(resource, "xl/worksheets/"+worksheet);
		process(stext, sheet);
	}
		
	private void process(String stext, String sheet) throws Exception {
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
		
		Document doc = XmlUtil.getDocument(sheet);
		Element root = doc.getDocumentElement();
		cells = new Hashtable<String,String>();
		NodeList cellList = root.getElementsByTagName("c");
		for (int i=0; i<cellList.getLength(); i++) {
			Element c = (Element)cellList.item(i);
			String r = c.getAttribute("r");
			
			int row = StringUtil.getInt( r.replaceAll("[A-Z]", "") );
			if (row > lastRow) lastRow = row;
			String column = r.replaceAll("[0-9]","");
			if (column.compareTo(lastColumn) > 0) lastColumn = column;
			
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
	 * Get the name of this worksheet.
	 * @return the name of this worksheet
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the number of cells in this worksheet.
	 * @return the number of cells in this worksheet
	 */
	public int getSize() {
		return cells.size();
	}

	/**
	 * Get the last row number in this worksheet.
	 * @return the last row number in this worksheet
	 */
	public int getLastRow() {
		return lastRow;
	}

	/**
	 * Get the last column identifier in this worksheet.
	 * @return the last column identifier in this worksheet
	 */
	public String getLastColumn() {
		return lastColumn;
	}

	/**
	 * Get a column identifier from an integer, with column A being integer 0.
	 * @param column integer
	 * @return the alphabetic identifier of the column.
	 */
	public static String getColumnID(int column) {
		LinkedList<String> stack = new LinkedList<String>();
		do {
			char c = (char)('A' + (column%26));
			stack.push(Character.toString(c));
		}
		while ( (column/=26) != 0 );
		StringBuffer sb = new StringBuffer();
		while (stack.size() > 0) sb.append(stack.pop());
		return sb.toString();		
	}

	/**
	 * Get an integer corresponding to a column identifier, with column A being integer 0.
	 * @param columnID integer
	 * @return the int corresponding to the alphabetic identifier of the column.
	 */
	public static int getColumn(String columnID) {
		char[] cArray = columnID.toCharArray();
		int c = 0;
		for (int k=0; k<cArray.length; k++) {
			c = 26 * c + (cArray[k] - 'A');
		}
		int n = 26;
		for (int i=1; i<cArray.length; i++) {
			c += n;
			n *= 26;
		}
		return c;
	}

	/**
	 * Get the list of worksheet names in an xlsx file.
	 * @param file the xlsx file
	 * @return the list of worksheet names.
	 */
	public static LinkedList<String> getWorksheetNames(File file) {
		LinkedList<String> list = new LinkedList<String>();
		try {
			ZipFile zipFile = new ZipFile(file);
			ZipEntry ze;
			Enumeration<? extends ZipEntry> e = zipFile.entries();
			while (e.hasMoreElements()) {
				ze = e.nextElement();
				if (!ze.isDirectory()) {
					String name = ze.getName();
					if (name.startsWith("xl/worksheets/") && name.endsWith(".xml")) {
						File sheet = new File(ze.getName());
						list.add( sheet.getName() );
					}
				}
			}
			zipFile.close();
		}
		catch (Exception ex) { }
		return list;
	}
	
	/**
	 * Get the list of worksheet names in an xlsx file.
	 * @param file the xlsx file
	 * @return the list of worksheet names.
	 */
	public static LinkedList<String> getWorksheetNames(String resource) {
		LinkedList<String> list = new LinkedList<String>();
		try {
			InputStream in = ExcelWorksheet.class.getResourceAsStream(resource);
			ZipInputStream zis = new ZipInputStream(in, FileUtil.utf8);
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				if (!ze.isDirectory()) {
					String name = ze.getName();
					if (name.startsWith("xl/worksheets/") && name.endsWith(".xml")) {
						File sheet = new File(ze.getName());
						list.add( sheet.getName() );
					}
				}
			}
			zis.close();
		}
		catch (Exception ex) { ex.printStackTrace(); }
		return list;
	}
	
	/**
	 * Search a row and return the column identifier (e.g., "A", "B", etc.) of the
	 * first cell containing the specified text. Note: this method only searches the first
	 * 26 columns ("A" through "Z").
	 * @param row the row to search
	 * @param text the text to match (case-sensitive)
	 * @return the column identifier, or the empty string if no cell matches the
	 * specified text.
	 */
	public String findColumn(int row, String text) {
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
		BufferedReader in = new BufferedReader(
					new InputStreamReader(zipFile.getInputStream(entry), FileUtil.utf8));
		int n = 0;
		char[] cbuf = new char[1024];
		while ((n = in.read(cbuf, 0, cbuf.length)) != -1) sw.write(cbuf, 0, n);
		in.close();
		return sw.toString();
	}
	
	private String getEntryText(String resource, String name) throws Exception {
		InputStream in = ExcelWorksheet.class.getResourceAsStream(resource);
		ZipInputStream zis = new ZipInputStream(in);
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			if (entry.getName().equals(name)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[2048];
				int n = 0;
				while ((n=zis.read(buf)) > 0) {
					baos.write(buf, 0, n);
				}
				zis.close();
				return baos.toString("UTF-8");
			}
		}
		zis.close();
		return "";
	}
	
	/**
	 * Get the contents of a cell
	 * @param adrs the column and row of the cell in the standard format (e.g, C41).
	 * @return the contents of the specified cell.
	 */
	public String getCell(String adrs) {
		String value = cells.get(adrs);
		return (value != null) ? value : "";
	}
}