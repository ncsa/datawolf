/******************************************************************************
 * Copyright 2004-2011 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.gondola;

public interface ISubmissionEngine {
	void cancel(String key) throws Throwable;

	void delete(String key) throws Throwable;

	void deleteAll(Long before) throws Throwable;

	String getJobStatus(String keyList);

	String submit(String scriptXML) throws Throwable;
}
