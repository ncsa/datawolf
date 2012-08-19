/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Shawn Hampton:  original design and implementation
 ******************************************************************************/
package ncsa.rcp.exceptions;

import ncsa.BaseCommonException;

public class UnknownExtensionException extends BaseCommonException {
	private static final long serialVersionUID = 2022L;

	public UnknownExtensionException() {

	}

	public UnknownExtensionException(String message) {
		super(message);
	}

	public UnknownExtensionException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownExtensionException(Throwable cause) {
		super(cause);
	}
}
