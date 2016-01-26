/*---------------------------------------------------------------
 *  Copyright 2016 by the Radiological Society of North America
 *
 *  This source software is released under the terms of the
 *  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
 *----------------------------------------------------------------*/

package org.rsna.util;

/**
 * Class to encapsulate a token.
 */
public class Token {
	
	String text;
	TokenType type;
	char delimiter;
	
	/**
	 * Construct a Token.
	 * @param text the text of the token
	 * @param delimiter the delimiter of the token
	 */
	public Token(String text, char delimiter) {
		this.text = text.trim();
		this.delimiter = delimiter;

		if (text.equals("")) {
			type = TokenType.getEmptyInstance();
		}
		else if (text.startsWith("\"") && text.endsWith("\"")) {
			type = TokenType.getLiteralInstance();
		}
		else type = TokenType.getIdentifierInstance();
	}
	
	/**
	 * Get the text of the token.
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Get the delimiter of the token.
	 */
	public char getDelimiter() {
		return delimiter;
	}
	
	/**
	 * Get the type of the token.
	 */
	public TokenType getType() {
		return type;
	}
	
	/**
	 * Determine whether the token is empty.
	 */
	public boolean isEmpty() {
		return type.isEmpty();
	}
	
	/**
	 * Determine whether the token is a literal text string
	 * (delimited by double-quotes or possibly by nothing).
	 */
	public boolean isLiteral() {
		return type.isLiteral();
	}
	
	/**
	 * Determine whether the token is an element identifier.
	 */
	public boolean isIdentifier() {
		return type.isIdentifier();
	}
	
	/**
	 * Determine whether the token is a tag.
	 */
	public boolean isTag() {
		return type.isTag();
	}
	
}
