/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.datawolf.executor.hpc.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    /**
     * Tries to match the given String by recurring on nested exception
     * messages.
     * 
     * @param t
     *            a Throwable
     * @param toMatch
     *            message being sought.
     * @return true if message occurs in some nested exception; false otherwise.
     */
    public static boolean checkException(Throwable t, String toMatch) {
        String message = null;
        Throwable throwable = t;
        while (throwable != null) {
            message = throwable.getMessage();
            if (message != null && -1 < message.indexOf(toMatch))
                return true;
            throwable = throwable.getCause();
        }
        return false;
    }

    public static String getFormattedErrorString(String message, Throwable t) {
        StringBuffer b = new StringBuffer();

        b.append(message);
        b.append(" -- "); //$NON-NLS-1$
        b.append(t.getClass().getName()).append(" -- "); //$NON-NLS-1$
        b.append(t.getLocalizedMessage());

        return b.toString();
    }

    /**
     * Redirects a stack trace into a String.
     * 
     * @param t
     *            from which to get trace.
     * @return the stack trace as String.
     */
    public static String getStackTrace(Throwable t) {
        if (t == null)
            return null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Extracts the root cause.
     * 
     * @param t
     *            from which to get trace.
     * @return the bottommost "cause" of the exception..
     */
    public static Throwable rootCause(Throwable t) {
        while (t.getCause() != null)
            t = t.getCause();
        return t;
    }
}
