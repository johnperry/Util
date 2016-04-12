/*---------------------------------------------------------------
 *  Copyright 2016 by the Radiological Society of North America
 *
 *  This source software is released under the terms of the
 *  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
 *----------------------------------------------------------------*/

package org.rsna.util;

/**
 * Class to encapsulate a type identifier for a Token.
 */
public class TokenType {
	
	String type;
	
	static final String emptyType		= "empty";
	static final String literalType		= "literal";
	static final String identifierType	= "identifier";
	static final String tagType			= "tag";
	
	/**
	 * Construct a TokenType.
	 * @param type the type of the token
	 */
	protected TokenType(String type) {
		this.type = type;
	}
	
	/**
	 * Construct an <code>empty</code> TokenType.
	 * @return an empty type
	 */
	public static TokenType getEmptyInstance() {
		return new TokenType(emptyType);
	}
	
	/**
	 * Construct a <code>literal</code> TokenType.
	 * @return a literal type
	 */
	public static TokenType getLiteralInstance() {
		return new TokenType(literalType);
	}
	
	/**
	 * Construct an <code>identifier</code> TokenType.
	 * @return an identifier type
	 */
	public static TokenType getIdentifierInstance() {
		return new TokenType(identifierType);
	}
	
	/**
	 * Construct a <code>tag</code> TokenType.
	 * @return a code type
	 */
	public static TokenType getTagInstance() {
		return new TokenType(tagType);
	}
	
	/**
	 * Get the type.
	 * @return the type name
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Determine whether the token is a literal.
	 * @return true if this is a literal type; false otherwise
	 */
	public boolean isLiteral() {
		return type.equals(literalType);
	}
	
	/**
	 * Determine whether the token is an element identifier.
	 * @return true if this is an identifier type; false otherwise
	 */
	public boolean isIdentifier() {
		return type.equals(identifierType);
	}
	
	/**
	 * Determine whether the token is a tag.
	 * @return true if this is a tag type; false otherwise
	 */
	public boolean isTag() {
		return type.equals(tagType);
	}
	
	/**
	 * Determine whether the token is empty.
	 * @return true if this is an empty type; false otherwise
	 */
	public boolean isEmpty() {
		return type.equals(emptyType);
	}
	
}
	