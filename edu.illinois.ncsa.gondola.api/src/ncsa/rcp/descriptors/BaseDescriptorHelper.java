/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:
 *    Shawn Hampton:  original design and implementation
 *    Albert L. Rossi: additional methods
 ******************************************************************************/
package ncsa.rcp.descriptors;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ncsa.rcp.exceptions.UnknownExtensionException;

public class BaseDescriptorHelper {
	private static class ClassToTagEntry {
		String tag;
		String extensionPt;
	}

	private final static Logger logger = Logger.getLogger(BaseDescriptorHelper.class);
	private static Map cachedDescriptorMap = new HashMap();
	private static Map map = new HashMap();

	private static Map classToTagIndex = new HashMap();

	public static Object createFrom(BaseDescriptor descriptor) throws CoreException {
		return createFromAttribute(BaseDescriptor.TAG_CLASS, descriptor);
	}

	public static Object createFromAttribute(String attributeName, BaseDescriptor descriptor) throws CoreException {
		return descriptor.getElement().createExecutableExtension(attributeName);
	}

	public static Object createNew(String tag, String extensionId) throws UnknownExtensionException {
		Map descriptorMap = initializeTagMap(extensionId);

		BaseDescriptor md = (BaseDescriptor) descriptorMap.get(tag);
		if (md == null)
			throw new UnknownExtensionException(extensionId + " with tag " + tag + " not found.");

		Object m;
		try {
			m = md.getElement().createExecutableExtension(BaseDescriptor.TAG_CLASS);
		} catch (CoreException e) {
			logger.error("Failed to instantiate " + md.getClassName(), e);
			throw new UnknownExtensionException("Failed to instantiate " + md.getClassName());
		}

		return m;
	}

	public static Object createNewById(String id, String extensionId) throws UnknownExtensionException {
		BaseDescriptor descriptor = getDescriptorById(id, extensionId, BaseDescriptor.class);
		return createNew(descriptor.getTag(), extensionId);
	}

	public static boolean exists(String tag, String extensionId) {
		Map descriptorMap = initializeTagMap(extensionId);
		BaseDescriptor md = (BaseDescriptor) descriptorMap.get(tag);
		if (md == null)
			return false;
		return true;
	}

	public static Class getClass(String className, String extensionId) throws UnknownExtensionException {
		String tag = getTagForClass(className, extensionId);
		Object instance = createNew(tag, extensionId);
		return instance.getClass();
	}

	public static String getClassForExtension(String tag, String extensionId) throws UnknownExtensionException {
		Map descriptorMap = initializeTagMap(extensionId);
		BaseDescriptor md = (BaseDescriptor) descriptorMap.get(tag);
		if (md == null)
			throw new UnknownExtensionException("could not find class of " + tag + ", extpt " + extensionId);
		return md.getClassName();
	}

	public static IConfigurationElement[] getConfigElements(String extensionId) {
		IExtensionPoint extPt = Platform.getExtensionRegistry().getExtensionPoint(extensionId);

		if (extPt == null) {
			logger.error("Failed to find extension point: " + extensionId);

			IExtensionPoint[] extPts = Platform.getExtensionRegistry().getExtensionPoints();
			for (int i = 0; i < extPts.length; i++)
				logger.debug(extPts[i].getSimpleIdentifier());

			return new IConfigurationElement[0];
		}

		IExtension[] extensions = extPt.getExtensions();

		List found = new ArrayList();

		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
			found.addAll(Arrays.asList(configElements));
		}

		return (IConfigurationElement[]) found.toArray(new IConfigurationElement[found.size()]);
	}

	public static BaseDescriptor getDescriptor(String tag, String extensionId) {
		Map descriptorMap = initializeTagMap(extensionId);
		BaseDescriptor md = (BaseDescriptor) descriptorMap.get(tag);
		return md;
	}

	public static BaseDescriptor getDescriptor(String tag, String extensionId, Class descriptorClass)
			throws UnknownExtensionException {
		Map descriptorMap = initializeTagMap(extensionId);
		BaseDescriptor md = (BaseDescriptor) descriptorMap.get(tag);
		if (md == null)
			throw new UnknownExtensionException("Could not find descriptor " + tag);
		String id = md.getId();
		return getDescriptorById(id, extensionId, descriptorClass);
	}

	public static BaseDescriptor getDescriptorById(String id, String extensionId, Class descriptorClass)
			throws UnknownExtensionException {
		BaseDescriptor[] descriptorsForId = getDescriptorsForId(extensionId, descriptorClass);
		for (int i = 0; i < descriptorsForId.length; i++)
			if (descriptorsForId[i].getId().equals(id))
				return descriptorsForId[i];
		throw new UnknownExtensionException("Could not find descriptor " + id);
	}

	public static BaseDescriptor getDescriptorForAttribute(String attributeName, String attributeValue, String extensionId,
			Class descriptorClass) throws UnknownExtensionException {
		IConfigurationElement[] configElements = getConfigElements(extensionId);
		for (int i = 0; i < configElements.length; i++) {
			String value = configElements[i].getAttribute(attributeName);
			if (value != null && value.equals(attributeValue))
				return createDescriptor(descriptorClass, configElements[i]);
		}

		throw new UnknownExtensionException("Could not create " + descriptorClass.getName() + " from " + extensionId + " for ("
				+ attributeName + ", " + attributeValue + ")");
	}

	public static BaseDescriptor[] getDescriptorsForId(String extensionId) {
		return getDescriptorsForId(extensionId, BaseDescriptor.class);
	}

	public static BaseDescriptor[] getDescriptorsForId(String extensionId, Class descriptorClass) {
		BaseDescriptor[] array = (BaseDescriptor[]) cachedDescriptorMap.get(extensionId);
		if (array != null) {
			if (array.length == 0)
				return array;

			BaseDescriptor first = array[0];
			if ((first.getClass().equals(descriptorClass)) || (descriptorClass.equals(BaseDescriptor.class)))
				return array;
		}

		List found = new ArrayList();
		IConfigurationElement[] configElements = getConfigElements(extensionId);

		for (int j = 0; j < configElements.length; j++) {
			IConfigurationElement configurationElement = configElements[j];
			BaseDescriptor proxy = createDescriptor(descriptorClass, configurationElement);

			if (proxy != null) {
				found.add(proxy);
				// alr
				ClassToTagEntry entry = new ClassToTagEntry();
				entry.tag = proxy.getTag();
				entry.extensionPt = extensionId;
				classToTagIndex.put(proxy.getClassName(), entry);
			}
		}

		array = (BaseDescriptor[]) found.toArray(new BaseDescriptor[found.size()]);
		cachedDescriptorMap.put(extensionId, array);

		return array;
	}

	public static URL getResourceUrl(String resourceField, BaseDescriptor descriptor) {
		String attribute = descriptor.getElement().getAttribute(resourceField);
		String namespaceIdentifier = descriptor.getElement().getDeclaringExtension().getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(namespaceIdentifier);
		return bundle.getEntry(attribute);
	}

	/**
	 * Just like getResourceURL(String,BaseDescriptor) but this works for those
	 * odd times when you have the configuration element instead of the
	 * BaseDescriptor
	 * 
	 * @param resourceField
	 * @param config
	 * @return the URL
	 */
	public static URL getResourceUrl(String resourceField, IConfigurationElement config) {
		String namespaceIdentifier = config.getDeclaringExtension().getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(namespaceIdentifier);
		String fileName = config.getAttribute(resourceField);
		if (!(fileName.startsWith("/")))
			fileName = "/" + fileName; //$NON-NLS-1$
		URL entry = bundle.getEntry(fileName);
		return entry;
	}

	public static URL getResourceUrl(String id, String extensionId, String resourceField) throws UnknownExtensionException {
		Map descriptorMap = initializeIdMap(extensionId);

		BaseDescriptor descriptor = (BaseDescriptor) descriptorMap.get(id);
		if (descriptor == null)
			throw new UnknownExtensionException(extensionId + " with tag " + id + " not found.");

		return getResourceUrl(resourceField, descriptor);
	}

	/**
	 * Gets a list of the resourceURLS based on a resourceField from an
	 * extension point. Helper method for getting all data out of
	 * getResourceURL(String resourceField, IConfigurationElement config)
	 * 
	 * @see getResourceURL(String resourceField, IConfigurationElement config)
	 * @param extPointId
	 *            the id of the extension point to query
	 * @param resourceField
	 *            the field of the extension point that contains the desired
	 *            resource URLS
	 * @return a list of all resource URLS contained in extensions to this
	 *         extension point.
	 */
	public static URL[] getResourceUrls(String extPointId, String resourceField) {
		IExtensionPoint extPt = Platform.getExtensionRegistry().getExtensionPoint(extPointId);
		IConfigurationElement[] configurationElements = extPt.getConfigurationElements();
		URL[] urls = new URL[configurationElements.length];
		for (int i = 0; i < configurationElements.length; i++)
			urls[i] = getResourceUrl(resourceField, configurationElements[i]);
		return urls;
	}

	public static String getTagForClass(String className, String extensionPt) throws UnknownExtensionException {
		getDescriptorsForId(extensionPt);
		String tag = null;
		ClassToTagEntry entry = (ClassToTagEntry) classToTagIndex.get(className);
		if (entry != null && extensionPt.equals(entry.extensionPt))
			tag = entry.tag;

		if (tag == null)
			throw new UnknownExtensionException("could not find tag for " + className + ", extpt " + extensionPt);
		return tag;
	}

	private static BaseDescriptor createDescriptor(Class descriptorClass, IConfigurationElement configurationElement) {
		BaseDescriptor proxy;
		try {
			proxy = (BaseDescriptor) descriptorClass.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(descriptorClass.getName() + " could not be created: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(descriptorClass.getName() + " could not be created: " + e.getMessage());
		}

		proxy.setElement(configurationElement);
		proxy.init();

		return proxy;
	}

	private static Map initializeIdMap(String extensionId) {
		BaseDescriptor[] descriptors = getDescriptorsForId(extensionId);
		Map descriptorMap = (Map) map.get(extensionId);

		if (descriptorMap == null) {
			descriptorMap = new HashMap();
			for (int i = 0; i < descriptors.length; i++)
				descriptorMap.put(descriptors[i].getId(), descriptors[i]);
			map.put(extensionId, descriptorMap);
		}
		return descriptorMap;
	}

	private static Map initializeTagMap(String extensionId) {
		BaseDescriptor[] descriptors = getDescriptorsForId(extensionId);
		Map descriptorMap = (Map) map.get(extensionId);

		if (descriptorMap == null) {
			descriptorMap = new HashMap();
			for (int i = 0; i < descriptors.length; i++)
				descriptorMap.put(descriptors[i].getTag(), descriptors[i]);
			map.put(extensionId, descriptorMap);
		}
		return descriptorMap;
	}
}
