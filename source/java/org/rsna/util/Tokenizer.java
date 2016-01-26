/*---------------------------------------------------------------
 *  Copyright 2016 by the Radiological Society of North America
 *
 *  This source software is released under the terms of the
 *  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
 *----------------------------------------------------------------*/

package org.rsna.util;

/**
 * Class to parse a string into Tokens.
 */
public class Tokenizer {

	static final char escapeChar = '\\';
	
	String text;
	int k = 0;
	
	/**
	 * Construct a Tokenizer.
	 * @param text the text to be parsed.
	 */
	public Tokenizer(String text) {
		this.text = text;
	}
	
	/**
	 * Get the next Token. This method skips leading whitespace and
	 * comments.
	 * @param delimiters the string of chars, any one of which is a delimiter
	 * of a token.
	 * @return the token, leaving the text position at the first character <b>after</b>
	 * the delimiter.
	 */
	public Token getNextToken(String delimiters) {
		
		skipWhitespace();
		skipComment();
		
		boolean inEscape = false;
		boolean inQuote = false;
		boolean inBracket = false;
		boolean inParen = false;
		
		char[] cArray = new char[1];
		StringBuffer sb = new StringBuffer();
		while (hasText()) {
			char c = text.charAt(k);
			cArray[0] = c;
			String cString = new String(cArray);
			if (inEscape) inEscape = false;
			else if (!inQuote && !inBracket && !inParen) {
				if ( delimiters.contains(cString) ) break;
			}
			if (c == escapeChar) inEscape = true;
			else {
				sb.append(c);
				if (c == '"') {
					inQuote = !inQuote;
				}
				else if (c == '[') {
					inBracket = true;
				}
				else if (inBracket && (c == ']')) {
					inBracket = false;
				}
				else if (c == '(') {
					inParen = true;
				}
				else if (inParen && (c == ')')) {
					inParen = false;
				}
			}
			k++;
		}
		
		String s = sb.toString().trim();
		
		if (s.equals("") && !hasText()) return null;
		
		//get the delimiter and skip it (if it's there)
		char delimiter = hasText() ? text.charAt(k++) : 0;
		
		return new Token(s, delimiter);
	}
	
	private void skipWhitespace() {
		while (hasText() && Character.isWhitespace(text.charAt(k))) {
			k++;
		}
	}
	
	private void skipLine() {
		while (hasText() && (text.charAt(k) != '\n') && (text.charAt(k) != '\r')) {
			k++;
		}
		while (hasText() && ((text.charAt(k) == '\n') || (text.charAt(k) == '\r'))) {
			k++;
		}
	}
	
	private boolean hasText() {
		return (k < text.length());
	}
	
	private void skipComment() {
		if (isCommentStart()) skipLine();
	}
	
	private boolean isCommentStart() {
		if (k < (text.length() - 1)) {
			return (text.charAt(k) == '/') && (text.charAt(k+1) != '/');
		}
		return false;
	}
	
}
