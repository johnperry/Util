/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.util;

import java.util.EventListener;

/**
 * The interface for listeners to PropertyEvents.
 */
public interface PropertyListener extends EventListener {

	public void propertyChanged (PropertyEvent event);

}
