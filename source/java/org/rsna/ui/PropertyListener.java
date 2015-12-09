/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.ui;

import java.util.EventListener;

/**
 * The interface for listeners to PropertyEvents.
 */
public interface PropertyListener extends EventListener {

	public void propertyChanged (PropertyEvent event);

}
