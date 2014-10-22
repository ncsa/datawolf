/******************************************************************************
 * Copyright 2004-2011 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.gondola;

import edu.illinois.ncsa.gondola.types.submission.IdListType;
import edu.illinois.ncsa.gondola.types.submission.JobStatusListType;
import edu.illinois.ncsa.gondola.types.submission.JobStatusType;
import edu.illinois.ncsa.gondola.types.submission.JobSubmissionType;


public interface ISubmissionEngineImpl extends Runnable, IStoppable {
	void cancel(String key) throws Throwable;

	void delete(String key) throws Throwable;

	void deleteAll(Long before) throws Throwable;

	JobStatusListType getJobStatus(IdListType keyList) throws Throwable;

	JobStatusType submit(JobSubmissionType scriptXML) throws Throwable;
}
