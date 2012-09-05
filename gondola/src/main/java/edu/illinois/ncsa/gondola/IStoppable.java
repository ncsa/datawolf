/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  API
 ******************************************************************************/
package edu.illinois.ncsa.gondola;

/**
 * Threaded object which needs to be, or can be, shut down.
 * 
 * @author Albert L. Rossi
 */
public interface IStoppable {
	/**
	 * User-defined method for allowing the run() method (or some other
	 * continuous looping process) to exit peacefully.
	 */
	public void halt();
}
