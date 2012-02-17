/*---------------------------------------------------------------
*  Copyright 2010 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.server;

import java.io.*;
import java.text.SimpleDateFormat;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.rsna.util.FileUtil;
import org.rsna.util.IPUtil;
import org.rsna.multipart.*;

/**
 * A simple HTTP request parser.
 */
public class HttpRequest {

	static final Logger logger = Logger.getLogger(HttpRequest.class);

	protected static SimpleDateFormat dateFormat = null;

	public final Socket socket;
	public final InputStream inputStream;
	public String method;
	public String path;
	public String query;
	public String content;
	public Path parsedPath;
	public User user;

	public Hashtable<String,String> headers = new Hashtable<String,String>();
	public Hashtable<String,String> cookies = new Hashtable<String,String>();
	public Hashtable<String,String> params = new Hashtable<String,String>();
	public Hashtable<String,List<String>> paramLists = new Hashtable<String,List<String>>();

	/**
	 * Construct an HttpRequest, connect it to an InputStream, and
	 * read the request from the stream.
	 * @param socket the socket on which to construct the request.
	 */
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
		inputStream = new BufferedInputStream(socket.getInputStream());
		parseRequestLine();
		getHeaders();
		if ( method.equals("POST")
				&& getContentType().toLowerCase().contains("application/x-www-form-urlencoded") ) {
			content = getContentText();
			getQueryParameters(content);
		}
		else getQueryParameters(query);
		this.user = Authenticator.getInstance().authenticate(this);
	}

	/**
	 * Add a parameter to the parameter tables.
	 * @param name the parameter name.
	 * @param value the parameter value.
	 */
	public void addParameter(String name, String value) {
		if (params.get(name) == null) {
			params.put(name,value);
		}
		List<String> list = paramLists.get(name);
		if (list == null) {
			list = new LinkedList<String>();
		}
		list.add(value);
		paramLists.put(name, list);
	}

	/**
	 * Get the InputStream associated with this request.
	 * (What is actually supplied is a BufferedInputStream.)
	 * @return the InputStream.
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Close the InputStream associated with this request.
	 */
	public void close() {
		FileUtil.close(inputStream);
	}

	/**
	 * Get the address of the remote socket as a String.
	 * @return the address of the remote socket.
	 */
	public String getRemoteAddress() {
		SocketAddress rsa = socket.getRemoteSocketAddress();
		String rsaString = "unknown";
		if ((rsa != null) && (rsa instanceof InetSocketAddress)) {
			rsaString = ((InetSocketAddress)rsa).getAddress().getHostAddress();
		}
		return rsaString;
	}

	/**
	 * Determine whether the request was initiated by the LocalHost.
	 * @return true if the request was initiated by the LocalHost; false otherwise.
	 */
	public boolean isFromLocalHost() {
		String remoteAddress = getRemoteAddress();
		String localAddress = IPUtil.getIPAddress();
		if (remoteAddress.equals(localAddress)) return true;
		if (remoteAddress.equals("127.0.0.1")) return true;
		return false;
	}

	/**
	 * Get the User associated with this request.
	 * @return the User.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Set the User associated with this request.
	 * @param user the User associated with this request,
	 * or null if no User has been authenticated.
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Determine whether the User associated with this request has been authenticated.
	 * @return true if the User is authenticated.
	 */
	public boolean isFromAuthenticatedUser() {
		return (user != null);
	}

	/**
	 * Determine whether the User associated with this request has a specified role.
	 * @return true if the User is authenticated and has the specified role; false otherwise.
	 */
	public boolean userHasRole(String role) {
		return (user != null) && user.hasRole(role);
	}

	/**
	 * Get the path associated with this request.
	 * @return the path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Get the Path object associated with this request.
	 * @return the Path object associated with this request.
	 */
	public Path getParsedPath() {
		return parsedPath;
	}

	/**
	 * Get a header value.
	 * @param name the case-insensitive name of the header.
	 * @return the string value of the header, or null if
	 * the header was not present in the request.
	 */
	public String getHeader(String name) {
		return headers.get(name.toLowerCase());
	}

	/**
	 * Get the conditional GET time value as specified
	 * in the "If-Modified-Since" header. The format of
	 * the time is "Thu, 16 Mar 2000 11:00:00 GMT".
	 * @return the millisecond time equivalent to the
	 * time defined in the header, or 0 if the
	 * header was not present in the request.
	 */
	public long getConditionalTime() {
		String time = headers.get( "if-modified-since" );
		if (time == null) return 0;
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			dateFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
		}
		try {
			Date date = dateFormat.parse(time);
			return date.getTime();
		}
		catch (Exception ex) { return 0; }
	}

	/**
	 * Get a cookie value.
	 * @param name the case-insensitive name of the cookie.
	 * @return the string value of the cookie, or null if
	 * the cookie was not present in the request.
	 */
	public String getCookie(String name) {
		return cookies.get(name.toLowerCase());
	}

	/**
	 * Get the String value of the Content-Type header.
	 * @return the Content-Type value, or the empty String
	 * if no value is available.
	 */
	public String getContentType() {
		String contentType = headers.get("content-type");
		if (contentType == null) return "";
		return contentType;
	}

	/**
	 * Get the int value of the Content-Length header.
	 * @return the Content-Length value, or -1 if it is missing or fails to parse.
	 */
	public int getContentLength() {
		int length = -1;
		String lengthString = headers.get("content-length");
		if (lengthString != null) {
			try { length = Integer.parseInt(lengthString); }
			catch (Exception didNotParse) { length = -1; }
		}
		return length;
	}

	/**
	 * Get the query string associated with this request.
	 * @return the query string associated with this request,
	 * or the empty string if no query string is present.
	 * The query string does not include the leading question-mark.
	 */
	public String getQueryString() {
		return query;
	}

	/**
	 * Get the names of the parameters.
	 * @return the names of the parameters in the params hashtable.
	 */
	public String[] getParameterNames() {
		return params.keySet().toArray(new String[0]);
	}

	/**
	 * Determine whether a query parameter was supplied.
	 * @param name the name of the parameter.
	 * @return true if the parameter was supplied in the request;
	 * false otherwise.
	 */
	public boolean hasParameter(String name) {
		return (params.get(name) != null);
	}

	/**
	 * Get a query parameter. If multiple parameters with the same name
	 * appear in a query string, this method returns the value of the
	 * first occurrence of the parameter.
	 * @param name the name of the parameter.
	 * @return the string value of the parameter, or null if
	 * the parameter does not exist in the query string.
	 */
	public String getParameter(String name) {
		return params.get(name);
	}

	/**
	 * Get a query parameter, supplying a default value if the parameter
	 * does not exist. If multiple parameters with the same name
	 * appear in a query string, this method returns the value of the
	 * first occurrence of the parameter.
	 * @param name the name of the parameter.
	 * @return the string value of the parameter, or null if
	 * the parameter does not exist in the query string.
	 */
	public String getParameter(String name, String defaultValue) {
		String param = params.get(name);
		return (param != null) ? param : defaultValue;
	}

	/**
	 * Get a List containing the values associated with the named
	 * query parameter.
	 * @param name the name of the parameter.
	 * @return the List of string values associated with the named parameter,
	 * or null if the parameter does not exist in the query string.
	 */
	public List<String> getParameterValues(String name) {
		return paramLists.get(name);
	}

	/**
	 * Parse the incoming multipart, storing files in the dir provided
	 * and updating the parameters.
	 * @param dir the directory in which to store the files.
	 * @param maxPostSize the maximum size POST to accept.
	 * @return the index of uploaded files.
	 */
	public LinkedList<UploadedFile> getParts(File dir, int maxPostSize) throws IOException {
		LinkedList<UploadedFile> files = new LinkedList<UploadedFile>();

		// Check the content type to make sure it's "multipart/form-data"
		String type = getContentType();
		if (!type.toLowerCase().contains("multipart/form-data")) {
		  return files; //wrong content type; return the empty files object
		}

		// Check the content length
		int length = getContentLength();
		if ((length == -1) || (length > maxPostSize)) {
			logger.warn("Attempt to parse multipart form with unacceptable length ("+length+" / "+maxPostSize+")");
			return files; //return an empty set of parts
		}

		MultipartInputStream mis = new MultipartInputStream( inputStream, length );
		FileRenamePolicy policy = new DefaultFileRenamePolicy();

		MultipartParser parser =
				new MultipartParser(type, mis);

		Part part;
		while ((part = parser.readNextPart()) != null) {
			String name = part.getName();
			if (part.isParam()) {
				ParamPart paramPart = (ParamPart) part;
				String value = paramPart.getStringValue();
				addParameter(name, value);
			}
			else if (part.isFile()) {
				FilePart filePart = (FilePart) part;
				String fileName = filePart.getFileName();
				if (fileName != null) {
					filePart.setRenamePolicy(policy);
					filePart.writeTo(dir);
					files.add(
							new UploadedFile(
									name,
									new File(dir, filePart.getFileName()), //note: the policy might have changed the filename
									filePart.getPath(),
									filePart.getContentType() ) );
				}
			}
		}
		return files;
	}

	//Get the method and path from the first line of the request.
	private void parseRequestLine() {
		query = "";
		path = "";
		method = "";
		parsedPath = new Path("");
		String line = getLine();
		line = line.trim();
		int k = line.indexOf(" ");
		if (k < 0) return;
		method = line.substring(0,k).toUpperCase();
		int kk = line.indexOf(" HTTP");
		if (kk < 0) return;
		path = line.substring(k,kk).trim();
		kk = path.indexOf("?");
		if (kk >= 0) {
			query = path.substring(kk+1);
			path = path.substring(0,kk).trim();
		}
		try { path = URLDecoder.decode(path,"UTF-8"); }
		catch (Exception ex) { logger.warn("Undecodable path: \""+path+"\""); }
		path = filterPath(path);
		parsedPath = new Path(path);
	}

	//Make sure that a path cannot reference anything above its root
	private String filterPath(String p) {
		String [] elements = p.split("/");
		LinkedList<String> pList = new LinkedList<String>();
		for (int i=0; i<elements.length; i++) {
			String e = elements[i].trim();
			if (e.equals("") || e.equals(".")) ; //do nothing
			else if (e.equals("..")) {			 //back up one level
				try { pList.removeLast(); }
				catch (Exception ignore) {
					logger.debug("Unable to remove a backstep path element in the request path.");
				}
			}
			else pList.add(e);					 //accept this element
		}
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = pList.iterator();
		while (it.hasNext()) sb.append("/" + it.next());
		if (sb.length() == 0) return "/";
		return sb.toString();
	}

	//Parse the headers, creating the headers and cookies Hashtables.
	//positioning the stream to the beginning of the data.
	private Hashtable<String,String> getHeaders() {
		String line;
		while (!((line=getLine()).equals(""))) {
			int k = line.indexOf(":");
			if (k != -1) {
				String headerName = line.substring(0,k).trim().toLowerCase();
				String value = line.substring(k+1).trim();
				headers.put(headerName, value);
				if (headerName.equals("cookie")) addCookies(value);
			}
		}
		return headers;
	}

	//Get one header line from the stream,
	//using \r\n as the delimiter.
	private String getLine() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b = 'x';
		try {
			while ( (b != '\n') && ((b=inputStream.read()) != -1) ) baos.write(b);
			byte[] bytes = baos.toByteArray();
			try { return ( new String(bytes, "UTF-8") ).trim(); }
			catch (Exception useDefault) { return ( new String(bytes) ).trim(); }
		}
		catch (Exception ex) { logger.debug("Exception reading a line from the request."); }
		return "";
	}

	//Add a set of cookies to a Hashtable<String,String>
	private void addCookies(String value) {
		String[] parts = value.split(";");
		for (int i=0; i<parts.length; i++) {
			String[] cookie = parts[i].split("=");
			if (cookie.length > 0) {
				String name = cookie[0].trim().toLowerCase();
				if (!name.startsWith("$")) {
					String val = "";
					if (cookie.length > 1) val = cookie[1].trim();
					cookies.put(name,val);
				}
			}
		}
	}

	//Construct the hashtables of the query parameters.
	//This method decodes the query string as UTF-8.
	private void getQueryParameters(String query) {
		try {
			if (query.trim().equals("")) return;
			String[] paramStrings = query.split("&");
			for (String param : paramStrings) {
				String[] paramParts = param.split("=");
				String name = URLDecoder.decode(paramParts[0].trim(),"UTF-8");
				String value = "";
				if (paramParts.length == 2) {
					value = URLDecoder.decode(paramParts[1].trim(),"UTF-8");
				}
				addParameter(name, value);
			}
		}
		catch (Exception quit) {
			logger.debug("Exception caught while parsing query parameters.", quit);
		}
	}

	//Read the content after the headers and return it as text.
	//This method should only be called if the content-type is
	//application/x-www-form-urlencoded and the content is
	//encoded as UTF-8.
	private String getContentText() {
		try {
			byte[] bytes = getContentBytes();
			String content = new String(bytes,"UTF-8");
			return content;
		}
		catch (Exception nothingThere) { return ""; }
	}

	//Read the content after the headers.
	private byte[] getContentBytes() {
		byte[] bytes = new byte[0];
		try {
			int contentLength = getContentLength();
			bytes = new byte[contentLength];
			int totalBytesRead = 0;
			while (totalBytesRead < contentLength) {
				totalBytesRead += inputStream.read(bytes, totalBytesRead, contentLength - totalBytesRead);
			}
		}
		catch (Exception done) {
			logger.debug("Exception caught while getting the content.", done);
		}
		return bytes;
	}

	/**
	 * Get a String representation of this HttpRequest
	 * @return the text value of the request, including the method, path, and query or content.
	 */
	public String toString() {
		return method + " " + path
				+ (query.equals("") ? "" : "?" + query)
				+ ( (method.equals("POST") && (content != null) && (content.length() > 0)) ? "\n" + content : "");
	}

	/**
	 * List the headers from this HttpRequest
	 */
	public String listHeaders(String margin) {
		StringBuffer sb = new StringBuffer();
		for (String key : headers.keySet()) {
			sb.append(margin + key + ": " + headers.get(key) + "\n");
		}
		if (sb.length() == 0) sb.append(margin + "none\n");
		return sb.toString();
	}

	/**
	 * List the cookies from this HttpRequest
	 */
	public String listCookies(String margin) {
		StringBuffer sb = new StringBuffer();
		for (String key : cookies.keySet()) {
			sb.append(margin + key + ": " + cookies.get(key) + "\n");
		}
		if (sb.length() == 0) sb.append(margin + "none\n");
		return sb.toString();
	}

}
