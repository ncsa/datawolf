/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Shawn Hampton:  original design and implementation
 ******************************************************************************/
package ncsa.logging.log4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ncsa.rcp.descriptors.BaseDescriptor;
import ncsa.rcp.descriptors.BaseDescriptorHelper;

public class LogInitializer {
	public static void initialize() {
		Properties properties = new Properties();
		createLogProviderProperties(properties);

		try {
			PropertyConfigurator.configure(properties);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void initialize(String symbolicNameForBundle) {
		Bundle b = Platform.getBundle(symbolicNameForBundle);
		if (b == null)
			return;
		URL url = b.getResource("log4j.properties");
		if (url == null)
			url = b.getResource("src/log4j.properties");
		if (url != null)
			LogInitializer.initialize(url, b);
	}

	public static void initialize(URL url, Bundle bundle) {
		if (url == null) {
			System.out.println("No log4j properties found");
			return;
		}

		System.out.println("Loading log4j properties from: " + bundle.getSymbolicName());

		try {
			LogManager.getLoggerRepository().resetConfiguration();
			PropertyConfigurator.configure(url);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		if (Logger.getRootLogger().getLevel() != org.apache.log4j.Level.DEBUG)
			LogInitializer.turnOffJavaUtilLogger();
	}

	public static void turnOffJavaUtilLogger() {
		java.util.logging.LogManager manager = java.util.logging.LogManager.getLogManager();

		for (Enumeration e = manager.getLoggerNames(); e.hasMoreElements();) {
			java.util.logging.Logger jlogger = manager.getLogger((String) e.nextElement());
			if (jlogger != null)
				jlogger.setLevel(java.util.logging.Level.OFF);
		}
	}

	private static void createLogProviderProperties(Properties p) {
		BaseDescriptor[] descriptors = BaseDescriptorHelper.getDescriptorsForId(LogProviderDescriptor.EXT_PT,
				LogProviderDescriptor.class);

		for (int i = 0; i < descriptors.length; i++) {
			LogProviderDescriptor d = (LogProviderDescriptor) descriptors[i];
			URL url = d.getFileUrl();

			InputStream s = null;
			try {
				s = url.openStream();
				p.load(s);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					s.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
