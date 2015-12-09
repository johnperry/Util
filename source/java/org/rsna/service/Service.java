/*---------------------------------------------------------------
*  Copyright 2015 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense.pdf)
*----------------------------------------------------------------*/

package org.rsna.service;

import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;

/**
 * The interface specifying a Service which can be passed to the HttpService.
 */
public interface Service {

	/**
	 * The handler for service requests.
	 * IMPORTANT: Since the HttpService is passed only one of these
	 * objects and it is possible that multiple service requests will
	 * be active at one time, any class implementing this interface
	 * must be thread safe.
	 * @param req the request object
	 * @param res the response object
	 */
	public void process(HttpRequest req, HttpResponse res);

}