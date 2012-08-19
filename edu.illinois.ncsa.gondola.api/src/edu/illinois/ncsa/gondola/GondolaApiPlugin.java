/******************************************************************************
 * Copyright 2004-2011 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Albert L. Rossi:  original design and implementation
 ******************************************************************************/
package edu.illinois.ncsa.gondola;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import ncsa.NonNLSConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @author arossi
 * 
 */
public class GondolaApiPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.illinois.ncsa.gondola.api"; //$NON-NLS-1$

	// The shared instance
	private static GondolaApiPlugin fPlugin;

	/**
	 * The constructor
	 */
	public GondolaApiPlugin() {
		fPlugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		//LogInitializer.initialize(PLUGIN_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		fPlugin = null;
	}

	/**
	 * Raise core exception.
	 * 
	 * @param message
	 * @return exception
	 */
	public static CoreException coreErrorException(String message) {
		return new CoreException(new Status(IStatus.ERROR, GondolaApiPlugin.getDefault().getBundle().getSymbolicName(), message));
	}

	/**
	 * Raise core exception.
	 * 
	 * @param message
	 * @param t
	 * @return exception
	 */
	public static CoreException coreErrorException(String message, Throwable t) {
		return new CoreException(new Status(IStatus.ERROR, GondolaApiPlugin.getDefault().getBundle().getSymbolicName(), message,
				t));
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static GondolaApiPlugin getDefault() {
		return fPlugin;
	}

	public static GondolaApiPlugin getfPlugin() {
		return fPlugin;
	}

	public static URL getResource(String resource) throws IOException {
		URL url = null;
		if (getDefault() != null) {
			Bundle bundle = getDefault().getBundle();
			url = FileLocator.find(bundle, new Path(NonNLSConstants.PATH_SEP + resource), null);
		} else
			url = new File(resource).toURI().toURL();
		return url;
	}

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null)
			return PLUGIN_ID;
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Create log entry from an IStatus
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Create log entry from a string
	 * 
	 * @param msg
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	/**
	 * Create log entry from a Throwable
	 * 
	 * @param e
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}
}
