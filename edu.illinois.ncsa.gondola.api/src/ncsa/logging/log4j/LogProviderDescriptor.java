/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Shawn Hampton:  original design and implementation
 ******************************************************************************/
package ncsa.logging.log4j;

import java.net.URL;

import ncsa.rcp.descriptors.BaseDescriptor;
import ncsa.rcp.descriptors.BaseDescriptorHelper;

public class LogProviderDescriptor extends BaseDescriptor {
	public final static String EXT_PT = "ncsa.logging.log4j.logProviders";

	public final static String TAG_PROFILE = "profile";
	public final static String TAG_FILE = "file";

	// ATTRIBUTES
	protected String profile;
	protected String file;

	public String getFile() {
		return file;
	}

	public URL getFileUrl() {
		return BaseDescriptorHelper.getResourceUrl(TAG_FILE, this);
	}

	public String getProfile() {
		return profile;
	}

	@Override
	public void init() {
		id = getAttribute(element, TAG_ID, null);
		file = getAttribute(element, TAG_FILE, null);
		profile = getAttribute(element, TAG_PROFILE, null);
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setProfile(String level) {
		this.profile = level;
	}
}
