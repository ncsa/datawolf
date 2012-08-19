/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package ncsa;

import java.io.Serializable;

public class BaseCommonException extends Exception implements Serializable {
	private static final long serialVersionUID = 1031L;

	public BaseCommonException() {
		super();
	}

	public BaseCommonException(String message) {
		super(message);
	}

	public BaseCommonException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseCommonException(Throwable cause) {
		super(cause);
	}
}
