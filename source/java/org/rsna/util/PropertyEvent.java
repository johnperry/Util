/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.awt.AWTEvent;

/**
 * The event that passes a property change to PropertyListeners.
 */
public class PropertyEvent extends AWTEvent {

	public static final int PROPERTY_EVENT = AWTEvent.RESERVED_ID_MAX + 4267 + 33;

	/** The property that has changed. */
	public String key = null;
	/** The new value of the property. */
	public String newValue = null;
	/** The old value of the property. */
	public String oldValue = null;

	/**
	 * Class constructor providing no information about the property
	 * that has changed, allowing a generic event to be sent when multiple
	 * property changes have been made.
	 * @param source the source of the event.
	 */
	public PropertyEvent(Object source) {
		this(source, null, null, null);
	}

	/**
	 * Class constructor capturing information about a property
	 * that has changed.
	 * @param source the source of the event.
	 * @param key the name of the property that has changed of the event.
	 * @param newValue the new value of the property.
	 * @param oldValue the old value of the property.
	 */
	public PropertyEvent(Object source, String key, String newValue, String oldValue) {
		super(source, PROPERTY_EVENT);
		this.key = key;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

}
