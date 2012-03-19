/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.illinois.ncsa.cyberintegrator.executor.java;

//----------------------------------------------------------------------
// This code is based on WebappClassLoader as distributed by Apache
// Tomcat. This code has been modified to fit in the Cyberintegrator
// architecture.
// http://svn.apache.org/viewvc/tomcat/trunk/java/org/apache/catalina/loader/
// Following is the official Apache License that was in the file.
//----------------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.CodeSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DisposeClassLoader extends URLClassLoader {
    private static Log               log                     = LogFactory.getLog(DisposeClassLoader.class);

    private static final boolean     ENABLE_CLEAR_REFERENCES = true;

    /**
     * Set of package names which are not allowed to be loaded from a tool class
     * loader without delegating first.
     */
    private static final Set<String> packageTriggers         = new HashSet<String>();

    static {
        addTriggerPackage("org.tupeloproject");
        addTriggerPackage("edu.uiuc.ncsa.cyberintegrator.executor.cyberintegrator.tool");
    }

    public static void addTriggerPackage(String pkg) {
        packageTriggers.add(pkg);
    }

    private final List<JarFileFile>              jarFiles          = new ArrayList<JarFileFile>();

    /**
     * The cache of ResourceEntry for classes and resources we have loaded,
     * keyed by resource name.
     */
    private final HashMap<String, ResourceEntry> resourceEntries   = new HashMap<String, ResourceEntry>();

    /**
     * The list of not found resources.
     */
    private final HashMap<String, String>        notFoundResources = new HashMap<String, String>();

    /**
     * Should this class loader delegate to the parent class loader
     * <strong>before</strong> searching its own repositories (i.e. the usual
     * Java2 delegation model)? If set to <code>false</code>, this class loader
     * will search its own repositories first, and delegate to the parent only
     * if the class or resource is not found locally.
     */
    private final boolean                        delegate          = false;

    /**
     * The parent class loader.
     */
    private ClassLoader                          parent            = null;

    /**
     * The system class loader.
     */
    private ClassLoader                          system            = null;

    /**
     * Path where resources loaded from JARs will be extracted.
     */
    protected File                               loaderDir         = null;

    /**
     * Use anti JAR locking code, which does URL rerouting when accessing
     * resources.
     */
    boolean                                      antiJARLocking    = true;

    public DisposeClassLoader() {
        super(new URL[0]);

        this.system = getSystemClassLoader();
    }

    public DisposeClassLoader(ClassLoader parent) {
        super(new URL[0], parent);

        this.system = getSystemClassLoader();
        this.parent = parent;
    }

    public void addJarFile(File file) {
        if (file.getName().toLowerCase().endsWith(".jar")) {
            JarFileFile jff = new JarFileFile();
            jff.file = file;
            jarFiles.add(jff);
        }
    }

    /**
     * Change the work directory.
     */
    public void setWorkDir(File workDir) {
        this.loaderDir = new File(workDir, "loader");
    }

    /**
     * Dispose the class loader. This will try and remove any loaded classes
     * from the cache. It will set all references in the classes loaded to null.
     * It will also try and unload any drivers loaded in the DiverManager.
     */
    public void dispose() {
        // kill mysql thread
        ResourceEntry mysqlcon = resourceEntries.get("com.mysql.jdbc.ConnectionImpl"); //$NON-NLS-1$
        if ((mysqlcon != null) && (mysqlcon.getClass() != null)) {
            log.debug("Killing mysql thread.");
            try {
                Class<?> clzz = mysqlcon.getClass();
                Field f = clzz.getDeclaredField("cancelTimer"); //$NON-NLS-1$
                f.setAccessible(true);
                Timer timer = (Timer) f.get(null);
                timer.cancel();
            } catch (Exception e) {
                log.debug("Could not kill timerthread.", e);
            }
        }

        // Clearing references should be done before setting started to
        // false, due to possible side effects
        clearReferences();

        for (JarFileFile jff : jarFiles) {
            try {
                if (jff.jarFile != null) {
                    jff.jarFile.close();
                }
                jff.file = null;
                jff.jarFile = null;
            } catch (IOException e) {
                log.info("Could not close jarfile.", e);
            }
        }

        notFoundResources.clear();
        resourceEntries.clear();
        jarFiles.clear();
        parent = null;
        system = null;
    }

    /**
     * Clear references.
     */
    @SuppressWarnings("unchecked")
    protected void clearReferences() {
        // Unregister any JDBC drivers loaded by this classloader
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == this) {
                log.trace("Unloading driver : " + driver);
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    log.warn("SQL driver deregistration failed", e);
                }
            }
        }

        // Null out any static or final fields from loaded classes,
        // as a workaround for apparent garbage collection bugs
        if (ENABLE_CLEAR_REFERENCES) {
            Iterator<ResourceEntry> loadedClasses = ((HashMap<String, ResourceEntry>) resourceEntries.clone()).values().iterator();
            while (loadedClasses.hasNext()) {
                ResourceEntry entry = loadedClasses.next();
                if (entry.loadedClass != null) {
                    Class<?> clazz = entry.loadedClass;
                    try {
                        Field[] fields = clazz.getDeclaredFields();
                        for (int i = 0; i < fields.length; i++) {
                            Field field = fields[i];
                            int mods = field.getModifiers();
                            if (field.getType().isPrimitive() || (field.getName().indexOf("$") != -1)) {
                                continue;
                            }
                            if (Modifier.isStatic(mods)) {
                                try {
                                    field.setAccessible(true);
                                    if (Modifier.isFinal(mods)) {
                                        if (!((field.getType().getName().startsWith("java.")) || (field.getType().getName().startsWith("javax.")))) {
                                            nullInstance(field.get(null));
                                        }
                                    } else {
                                        field.set(null, null);
                                        log.trace("Set field " + field.getName() + " to null in class " + clazz.getName());

                                    }
                                } catch (Throwable t) {
                                    log.debug("Could not set field " + field.getName() + " to null in class " + clazz.getName(), t);

                                }
                            }
                        }
                    } catch (Throwable t) {
                        log.debug("Could not clean fields for class " + clazz.getName(), t);

                    }
                }
            }
        }

        // Clear the IntrospectionUtils cache.
        // IntrospectionUtils.clear();

        // Clear the classloader reference in common-logging
        // log = null;

        // Clear the classloader reference in the VM's bean introspector
        java.beans.Introspector.flushCaches();

    }

    protected void nullInstance(Object instance) {
        if (instance == null) {
            return;
        }
        Field[] fields = instance.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            int mods = field.getModifiers();
            if (field.getType().isPrimitive() || (field.getName().indexOf("$") != -1)) {
                continue;
            }
            try {
                field.setAccessible(true);
                if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
                    // Doing something recursively is too risky
                    continue;
                } else {
                    Object value = field.get(instance);
                    if (null != value) {
                        Class<? extends Object> valueClass = value.getClass();
                        if (!loadedByThisOrChild(valueClass)) {
                            log.trace("Not setting field " + field.getName() + "=" + value + " to null in object of class " + instance.getClass().getName()
                                    + " because the referenced object was of type " + valueClass.getName() + " which was not loaded by this WebappClassLoader.");
                        } else {
                            field.set(instance, null);
                            log.trace("Set field " + field.getName() + " to null in class " + instance.getClass().getName());
                        }
                    }
                }
            } catch (Throwable t) {
                log.debug("Could not set field " + field.getName() + " to null in object instance of class " + instance.getClass().getName(), t);

            }
        }
    }

    /**
     * Determine whether a class was loaded by this class loader or one of its
     * child class loaders.
     */
    protected boolean loadedByThisOrChild(Class<? extends Object> clazz) {
        boolean result = false;
        for (ClassLoader classLoader = clazz.getClassLoader(); null != classLoader; classLoader = classLoader.getParent()) {
            if (classLoader.equals(this)) {
                result = true;
                break;
            }
        }
        return result;
    }

    // ----------------------------------------------------------------------
    // find resources
    // ----------------------------------------------------------------------

    /**
     * Find the resource with the given name. A resource is some data (images,
     * audio, text, etc.) that can be accessed by class code in a way that is
     * independent of the location of the code. The name of a resource is a
     * "/"-separated path name that identifies the resource. If the resource
     * cannot be found, return <code>null</code>.
     * <p>
     * This method searches according to the following algorithm, returning as
     * soon as it finds the appropriate URL. If the resource cannot be found,
     * returns <code>null</code>.
     * <ul>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     * call the <code>getResource()</code> method of the parent class loader, if
     * any.</li>
     * <li>Call <code>findResource()</code> to find this resource in our locally
     * defined repositories.</li>
     * <li>Call the <code>getResource()</code> method of the parent class
     * loader, if any.</li>
     * </ul>
     * 
     * @param name
     *            Name of the resource to return a URL for
     */
    @Override
    public URL getResource(String name) {
        log.trace("getResource(" + name + ")");
        URL url = null;

        // (1) Delegate to parent if requested
        if (delegate) {
            log.trace("  Delegating to parent classloader " + parent);

            ClassLoader loader = parent;
            if (loader == null) {
                loader = system;
            }
            url = loader.getResource(name);
            if (url != null) {
                log.trace("  --> Returning '" + url.toString() + "'");
                return url;
            }
        }

        // (2) Search local repositories
        url = findResource(name);
        if (url != null) {
            // Locating the repository for special handling in the case
            // of a JAR
            if (antiJARLocking) {
                ResourceEntry entry = resourceEntries.get(name);
                try {
                    String repository = entry.codeBase.toString();
                    if ((repository.endsWith(".jar")) && (!(name.endsWith(".class")))) {
                        // Copy binary content to the work directory if not
// present
                        File resourceFile = new File(loaderDir, name);
                        url = getURI(resourceFile);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            log.trace("  --> Returning '" + url.toString() + "'");

            return url;
        }

        // (3) Delegate to parent unconditionally if not already attempted
        if (!delegate) {
            ClassLoader loader = parent;
            if (loader == null) {
                loader = system;
            }
            url = loader.getResource(name);
            if (url != null) {
                log.trace("  --> Returning '" + url.toString() + "'");

                return url;
            }
        }

        // (4) Resource was not found
        log.trace("  --> Resource not found, returning null");

        return null;
    }

    /**
     * Find the resource with the given name, and return an input stream that
     * can be used for reading it. The search order is as described for
     * <code>getResource()</code>, after checking to see if the resource data
     * has been previously cached. If the resource cannot be found, return
     * <code>null</code>.
     * 
     * @param name
     *            Name of the resource to return an input stream for
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        log.trace("getResourceAsStream(" + name + ")");
        InputStream stream = null;

        // (0) Check for a cached copy of this resource
        stream = findLoadedResource(name);
        if (stream != null) {
            log.trace("  --> Returning stream from cache");
            return (stream);
        }

        // (1) Delegate to parent if requested
        if (delegate) {
            log.trace("  Delegating to parent classloader " + parent);
            ClassLoader loader = parent;
            if (loader == null) {
                loader = system;
            }
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                log.trace("  --> Returning stream from parent");
                return (stream);
            }
        }

        // (2) Search local repositories
        log.trace("  Searching local repositories");
        URL url = findResource(name);
        if (url != null) {
            log.trace("  --> Returning stream from local");
            stream = findLoadedResource(name);
            if (stream != null) {
                return stream;
            }
        }

        // (3) Delegate to parent unconditionally
        if (!delegate) {
            log.trace("  Delegating to parent classloader unconditionally " + parent);
            ClassLoader loader = parent;
            if (loader == null) {
                loader = system;
            }
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                log.trace("  --> Returning stream from parent");
                return stream;
            }
        }

        // (4) Resource was not found
        log.trace("  --> Resource not found, returning null");
        return null;
    }

    /**
     * Finds the resource with the given name if it has previously been loaded
     * and cached by this class loader, and return an input stream to the
     * resource data. If this resource has not been cached, return
     * <code>null</code>.
     * 
     * @param name
     *            Name of the resource to return
     */
    protected InputStream findLoadedResource(String name) {
        ResourceEntry entry = resourceEntries.get(name);
        if (entry != null) {
            if (entry.binaryContent != null) {
                return new ByteArrayInputStream(entry.binaryContent);
            }
        }
        return null;
    }

    /**
     * Find the specified resource in our local repository, and return a
     * <code>URL</code> refering to it, or <code>null</code> if this resource
     * cannot be found.
     * 
     * @param name
     *            Name of the resource to be found
     */
    @Override
    public URL findResource(final String name) {
        log.trace("findResource(" + name + ")");

        URL url = null;

        ResourceEntry entry = resourceEntries.get(name);
        if (entry == null) {
            entry = findResourceInternal(name, name);
        }
        if (entry != null) {
            url = entry.source;
        }

        // if ((url == null) && hasExternalRepositories) {
        // url = super.findResource(name);
        // }
        //
        if (url != null) {
            log.trace("    --> Returning '" + url.toString() + "'");
        } else {
            log.trace("    --> Resource not found, returning null");
        }
        return url;

    }

    /**
     * Return an enumeration of <code>URLs<code> representing all of
     * the resources with the given name. If no resources with this 
     * name are found, return an empty set.
     * 
     * @param name
     *            Name of the resources to be found
     * 
     * @exception IOException
     *                if an input/output error occurs
     */
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {

        if (log.isDebugEnabled()) {
            log.debug("    findResources(" + name + ")");
        }
        Vector<URL> result = new Vector<URL>();

        int jarFilesLength = jarFiles.size();

        int i;

        // Looking at the JAR files
        synchronized (jarFiles) {
            // if (openJARs()) {
            for (i = 0; i < jarFilesLength; i++) {
                JarFileFile file = jarFiles.get(i);
                JarEntry jarEntry = file.jarFile.getJarEntry(name);// jarFiles.get(i).getJarEntry(name);
                if (jarEntry != null) {
                    try {
                        String jarFakeUrl = getURI(file.file).toString();
                        jarFakeUrl = "jar:" + jarFakeUrl + "!/" + name;
                        result.addElement(new URL(jarFakeUrl));
                    } catch (MalformedURLException e) {
                        // Ignore
                    }
                }
            }
        }

        return result.elements();

    }

    /**
     * Find specified resource in local repositories.
     * 
     * @return the loaded resource, or null if the resource isn't found
     */
    protected ResourceEntry findResourceInternal(String name, String path) {
        if ((name == null) || (path == null)) {
            return null;
        }

        ResourceEntry entry = resourceEntries.get(name);
        if (entry != null) {
            return entry;
        }

        int contentLength = -1;
        InputStream binaryStream = null;

        int jarFilesLength = jarFiles.size();

        int i;

        JarEntry jarEntry = null;
        JarFileFile jff = null;

        synchronized (jarFiles) {
            for (i = 0; (entry == null) && (i < jarFilesLength); i++) {
                jff = jarFiles.get(i);
                if (jff.jarFile == null) {
                    try {
                        jff.jarFile = new JarFile(jff.file);
                    } catch (IOException e) {
                        log.info("Could not open jarfile [" + jff.file.getName() + "].", e);
                        continue;
                    }
                }
                jarEntry = jff.jarFile.getJarEntry(path);

                if (jarEntry != null) {
                    entry = new ResourceEntry();
                    try {
                        entry.codeBase = getURL(jff.file, false);
                        String jarFakeUrl = getURI(jff.file).toString();
                        jarFakeUrl = "jar:" + jarFakeUrl + "!/" + path;
                        entry.source = new URL(jarFakeUrl);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                    contentLength = (int) jarEntry.getSize();
                    try {
                        entry.manifest = jff.jarFile.getManifest();
                        binaryStream = jff.jarFile.getInputStream(jarEntry);
                    } catch (IOException e) {
                        return null;
                    }

                    // Extract resources contained in JAR to the workdir
                    if (antiJARLocking && !(path.endsWith(".class"))) {
                        byte[] buf = new byte[1024];
                        File resourceFile = new File(loaderDir, jarEntry.getName());
                        if (!resourceFile.exists()) {
                            Enumeration<JarEntry> entries = jff.jarFile.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry jarEntry2 = entries.nextElement();
                                if (!(jarEntry2.isDirectory()) && (!jarEntry2.getName().endsWith(".class"))) {
                                    resourceFile = new File(loaderDir, jarEntry2.getName());
                                    resourceFile.getParentFile().mkdirs();
                                    FileOutputStream os = null;
                                    InputStream is = null;
                                    try {
                                        is = jff.jarFile.getInputStream(jarEntry2);
                                        os = new FileOutputStream(resourceFile);
                                        while (true) {
                                            int n = is.read(buf);
                                            if (n <= 0) {
                                                break;
                                            }
                                            os.write(buf, 0, n);
                                        }
                                    } catch (IOException e) {
                                        // Ignore
                                    } finally {
                                        try {
                                            if (is != null) {
                                                is.close();
                                            }
                                        } catch (IOException e) {}
                                        try {
                                            if (os != null) {
                                                os.close();
                                            }
                                        } catch (IOException e) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (entry == null) {
                synchronized (notFoundResources) {
                    notFoundResources.put(name, name);
                }
                return null;
            }

            if (binaryStream != null) {
                byte[] binaryContent = new byte[contentLength];

                int pos = 0;
                try {
                    while (true) {
                        int n = binaryStream.read(binaryContent, pos, binaryContent.length - pos);
                        if (n <= 0) {
                            break;
                        }
                        pos += n;
                    }
                } catch (IOException e) {
                    log.error("webappClassLoader.readError", e);
                    return null;
                } finally {
                    try {
                        binaryStream.close();
                    } catch (IOException e) {}
                }
                entry.binaryContent = binaryContent;

                // The certificates are only available after the JarEntry
                // associated input stream has been fully read
                if (jarEntry != null) {
                    entry.certificates = jarEntry.getCertificates();
                }
            }
        }

        // Add the entry in the local resource repository
        synchronized (resourceEntries) {
            // Ensures that all the threads which may be in a race to load
            // a particular class all end up with the same ResourceEntry
            // instance
            ResourceEntry entry2 = resourceEntries.get(name);
            if (entry2 == null) {
                resourceEntries.put(name, entry);
            } else {
                entry = entry2;
            }
        }

        return entry;
    }

    /**
     * Get URL.
     */
    protected URL getURL(File file, boolean encoded) throws MalformedURLException {
        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }
        if (encoded) {
            return getURI(realFile);
        } else {
            return realFile.toURI().toURL();
        }
    }

    /**
     * Get URL.
     */
    protected URL getURI(File file) throws MalformedURLException {
        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }
        return realFile.toURI().toURL();
    }

    // ----------------------------------------------------------------------
    // class loading
    // ----------------------------------------------------------------------

    /**
     * Load the class with the specified name. This method searches for classes
     * in the same manner as <code>loadClass(String, boolean)</code> with
     * <code>false</code> as the second argument.
     * 
     * @param name
     *            Name of the class to be loaded
     * 
     * @exception ClassNotFoundException
     *                if the class was not found
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /**
     * Load the class with the specified name, searching using the following
     * algorithm until it finds and returns the class. If the class cannot be
     * found, returns <code>ClassNotFoundException</code>.
     * <ul>
     * <li>Call <code>findLoadedClass(String)</code> to check if the class has
     * already been loaded. If it has, the same <code>Class</code> object is
     * returned.</li>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     * call the <code>loadClass()</code> method of the parent class loader, if
     * any.</li>
     * <li>Call <code>findClass()</code> to find this class in our locally
     * defined repositories.</li>
     * <li>Call the <code>loadClass()</code> method of our parent class loader,
     * if any.</li>
     * </ul>
     * If the class was found using the above steps, and the
     * <code>resolve</code> flag is <code>true</code>, this method will then
     * call <code>resolveClass(Class)</code> on the resulting Class object.
     * 
     * @param name
     *            Name of the class to be loaded
     * @param resolve
     *            If <code>true</code> then resolve the class
     * 
     * @exception ClassNotFoundException
     *                if the class was not found
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        log.trace("loadClass(" + name + ", " + resolve + ")");
        Class<?> clazz = null;

        // (0) Check our previously loaded local class cache
        clazz = findLoadedClass0(name);
        if (clazz != null) {
            log.trace("  Returning class from cache");

            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        // (0.1) Check our previously loaded class cache
        clazz = findLoadedClass(name);
        if (clazz != null) {
            log.trace("  Returning class from cache");

            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        // (0.2) Try loading the class with the system class loader, to prevent
        // the webapp from overriding J2SE classes
        try {
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }

        boolean delegateLoad = delegate || filter(name);

        // (1) Delegate to our parent if requested
        if (delegateLoad) {
            log.trace("  Delegating to parent classloader1 " + getParent());

            ClassLoader loader = parent;
            if (loader == null) {
                loader = system;
            }
            try {
                clazz = Class.forName(name, false, loader);
                if (clazz != null) {
                    log.trace("  Loading class from parent");

                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        // (2) Search local repositories
        log.trace("  Searching local repositories");

        try {
            clazz = findClass(name);
            if (clazz != null) {
                log.trace("  Loading class from local repository");

                if (resolve) {
                    resolveClass(clazz);
                }
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }

        // (3) Delegate to parent unconditionally
        if (!delegateLoad) {
            log.trace("  Delegating to parent classloader at end: " + parent);

            ClassLoader loader = parent;
            if (loader == null) {
                loader = system;
            }
            try {
                clazz = Class.forName(name, false, loader);
                if (clazz != null) {
                    log.trace("  Loading class from parent");

                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        throw new ClassNotFoundException(name);
    }

    /**
     * Finds the class with the given name if it has previously been loaded and
     * cached by this class loader, and return the Class object. If this class
     * has not been cached, return <code>null</code>.
     * 
     * @param name
     *            Name of the resource to return
     */
    protected Class<?> findLoadedClass0(String name) {
        ResourceEntry entry = resourceEntries.get(name);
        if (entry != null) {
            return entry.loadedClass;
        }
        return null;
    }

    /**
     * Find the specified class in our local repositories, if possible. If not
     * found, throw <code>ClassNotFoundException</code>.
     * 
     * @param name
     *            Name of the class to be loaded
     * 
     * @exception ClassNotFoundException
     *                if the class was not found
     */
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        log.trace("    findClass(" + name + ")");

        // Ask our superclass to locate this class, if possible
        // (throws ClassNotFoundException if it is not found)
        Class<?> clazz = null;
        try {
            log.trace("      findClassInternal(" + name + ")");
            try {
                clazz = findClassInternal(name);
            } catch (ClassNotFoundException cnfe) {
                throw cnfe;
            } catch (AccessControlException ace) {
                throw new ClassNotFoundException(name, ace);
            } catch (RuntimeException e) {
                log.trace("      -->RuntimeException Rethrown", e);
                throw e;
            }
            if (clazz == null) {
                log.trace("    --> Returning ClassNotFoundException");
                throw new ClassNotFoundException(name);
            }
        } catch (ClassNotFoundException e) {
            log.trace("    --> Passing on ClassNotFoundException");
            throw e;
        }

        // Return the class we have located
        log.trace("      Returning class " + clazz);
        log.trace("      Loaded by " + clazz.getClassLoader().toString());
        return (clazz);

    }

    /**
     * Find specified class in local repositories.
     * 
     * @return the loaded class, or null if the class isn't found
     */
    protected Class<?> findClassInternal(String name) throws ClassNotFoundException {

        if (!validate(name)) {
            throw new ClassNotFoundException(name);
        }

        String tempPath = name.replace('.', '/');
        String classPath = tempPath + ".class";

        ResourceEntry entry = null;

        entry = findResourceInternal(name, classPath);

        if (entry == null) {
            throw new ClassNotFoundException(name);
        }

        Class<?> clazz = entry.loadedClass;
        if (clazz != null) {
            return clazz;
        }

        synchronized (this) {
            clazz = entry.loadedClass;
            if (clazz != null) {
                return clazz;
            }

            if (entry.binaryContent == null) {
                throw new ClassNotFoundException(name);
            }

            // Looking up the package
            String packageName = null;
            int pos = name.lastIndexOf('.');
            if (pos != -1) {
                packageName = name.substring(0, pos);
            }

            Package pkg = null;

            if (packageName != null) {
                pkg = getPackage(packageName);
                // Define the package (if null)
                if (pkg == null) {
                    try {
                        if (entry.manifest == null) {
                            definePackage(packageName, null, null, null, null, null, null, null);
                        } else {
                            definePackage(packageName, entry.manifest, entry.codeBase);
                        }
                    } catch (IllegalArgumentException e) {
                        // Ignore: normal error due to dual definition of
// package
                    }
                    pkg = getPackage(packageName);
                }
            }

            try {
                clazz = defineClass(name, entry.binaryContent, 0, entry.binaryContent.length, new CodeSource(entry.codeBase, entry.certificates));
            } catch (UnsupportedClassVersionError ucve) {
                throw new UnsupportedClassVersionError(ucve.getLocalizedMessage() + " " + "webappClassLoader.wrongVersion");
            }
            entry.loadedClass = clazz;
            entry.binaryContent = null;
            entry.source = null;
            entry.codeBase = null;
            entry.manifest = null;
            entry.certificates = null;
        }

        return clazz;
    }

    /**
     * Validate a classname. As per SRV.9.7.2, we must restict loading of
     * classes from J2SE (java.*).
     * 
     * @param name
     *            class name
     * @return true if the name is valid
     */
    protected boolean validate(String name) {
        if (name == null) {
            return false;
        }
        if (name.startsWith("java.")) {
            return false;
        }
        return true;
    }

    /**
     * Filter classes.
     * 
     * @param name
     *            class name
     * @return true if the class should be filtered
     */
    protected boolean filter(String name) {
        if (name == null) {
            return false;
        }

        // Looking up the package
        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1) {
            packageName = name.substring(0, pos);
        } else {
            return false;
        }

        for (String pkg : packageTriggers) {
            if (packageName.startsWith(pkg)) {
                return true;
            }
        }

        return false;
    }

    class JarFileFile {
        public File    file    = null;
        public JarFile jarFile = null;
    }
}