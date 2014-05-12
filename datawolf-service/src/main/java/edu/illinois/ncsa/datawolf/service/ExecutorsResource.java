/*******************************************************************************
 * Copyright (c) 2012 University of Illinois/NCSA.  All rights reserved.
 * 
 *   National Center for Supercomputing Applications (NCSA)
 *   http://www.ncsa.illinois.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of University of Illinois, NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 ******************************************************************************/
package edu.illinois.ncsa.datawolf.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import edu.illinois.ncsa.datawolf.Engine;
import edu.illinois.ncsa.datawolf.Executor;
import edu.illinois.ncsa.datawolf.executor.java.tool.JavaTool;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/executors")
public class ExecutorsResource {

    private static final Logger log = LoggerFactory.getLogger(ExecutorsResource.class);

    /**
     * Get all available executors
     * 
     * @return
     *         Returns the set of available executors
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<String> getExecutors() throws Exception {
        Engine engine = SpringData.getBean(Engine.class);
        if (engine == null)
            throw (new Exception("Can't retrive CI engine and executors"));

        Iterator<Executor> iterator = engine.getExecutors().iterator();
        List<String> executorNames = new ArrayList<String>();
        while (iterator.hasNext()) {
            Executor exec = iterator.next();
            executorNames.add(exec.getExecutorName());
        }
        return executorNames;
    }

    /**
     * Discovers available JavaTools included in set of Jar files
     * 
     * @param input
     *            FormData that includes zip file of jars
     * @return List of JavaTools and their basic information
     */
    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public Map<String, JavaTool> findJavaTools(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("tool");
        for (InputPart inputPart : inputParts) {
            File tempfile = null;
            try {
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                tempfile = File.createTempFile("tool", ".zip");
                OutputStream outputStream = new FileOutputStream(tempfile);
                byte[] buf = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf);
                }
                outputStream.close();
                inputStream.close();

                Set<Class<? extends JavaTool>> tools = readJarFiles(tempfile);
                List<JavaTool> javaTools = new ArrayList<JavaTool>();
                Map<String, JavaTool> map = new HashMap<String, JavaTool>();
                if (tools != null) {
                    Iterator<Class<? extends JavaTool>> it = tools.iterator();
                    while (it.hasNext()) {
                        try {
                            Class<? extends JavaTool> obj = it.next();
                            JavaTool tool = obj.newInstance();
                            map.put(obj.getName(), tool);
                            javaTools.add(tool);
                        } catch (InstantiationException e) {
                            log.error("Error creating instance of JavaTool.", e);
                        } catch (IllegalAccessException e) {
                            log.error("Error creating instance of JavaTool.", e);
                        }
                    }
                }
                return map;
            } catch (IOException e) {
                log.error("Error creating zip file from form data.", e);
            } finally {
                if (tempfile != null) {
                    tempfile.delete();
                }
            }
        }
        return null;
    }

    /**
     * Finds all available JavaTools in set of jar files
     * 
     * @param file
     *            Zip file containing jars
     * @return Set of java tools
     */
    public static Set<Class<? extends JavaTool>> readJarFiles(File file) {
        ZipFile zipfile = null;
        List<File> fileList = new ArrayList<File>();
        try {
            zipfile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipfile.entries();
            ConfigurationBuilder cb = new ConfigurationBuilder();
            List<URL> urlList = new ArrayList<URL>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                InputStream is = zipfile.getInputStream(entry);
                String[] pieces = entry.getName().split("/");
                File output = File.createTempFile(pieces[1], ".jar");
                fileList.add(output);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(output);
                    byte[] buf = new byte[10240];
                    int len = 0;
                    while ((len = is.read(buf)) >= 0) {
                        fos.write(buf, 0, len);
                    }
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                    is.close();
                }

                urlList.add(new URL(output.toURI().toURL().toString()));
            }

            URL[] urls = urlList.toArray(new URL[urlList.size()]);
            cb.addUrls(urls);
            Reflections reflections = new Reflections(cb);
            Set<String> classnames = reflections.getStore().getSubTypesOf(JavaTool.class.getName());
            return ImmutableSet.copyOf(ReflectionUtils.<JavaTool> forNames(classnames, new URLClassLoader(urls, Thread.currentThread().getContextClassLoader())));
        } catch (Exception e) {
            log.error("Error reading jar files from zip.", e);
            try {
                if (zipfile != null) {
                    zipfile.close();
                }
            } catch (Exception e1) {
                log.error("Error closing zip file.", e1);
            }
        } finally {
            for (File f : fileList) {
                f.delete();
            }
        }
        return null;
    }
}
