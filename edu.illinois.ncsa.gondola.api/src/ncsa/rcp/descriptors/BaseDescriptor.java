/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Shawn Hampton:  original design and implementation
 ******************************************************************************/
package ncsa.rcp.descriptors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseDescriptor {
	protected final static Logger logger = LoggerFactory.getLogger(BaseDescriptor.class);

	public final static String TAG_ID = "id";
	public final static String TAG_NAME = "name";
	public final static String TAG_TAG = "tag";
	public final static String TAG_CLASS = "class";

	protected static String extensionId = "not set";
	protected static String elementName = "not set";

	protected String id;
	protected String name;
	protected String tag;
	protected String className;

	protected IConfigurationElement element;

	public BaseDescriptor() {

	}

	public BaseDescriptor(IConfigurationElement element) {
		this.element = element;
		init();
	}

	public String getClassName() {
		return className;
	}

	public IConfigurationElement getElement() {
		return element;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getTag() {
		return tag;
	}

	public void init() {
		id = getAttribute(element, TAG_ID, null);
		name = getAttribute(element, TAG_NAME, null);
		className = getAttribute(element, TAG_CLASS, null);
		tag = getAttribute(element, TAG_TAG, null);
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setElement(IConfigurationElement element) {
		this.element = element;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	protected String getAttribute(IConfigurationElement e, String name, String defaultValue) {
		String value = e.getAttribute(name);
		if (value != null)
			return value;

		if (defaultValue != null)
			return defaultValue;

		throw new IllegalArgumentException("Missing " + name + " attribute");
	}
}
