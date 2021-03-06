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
package edu.illinois.ncsa.datawolf.executor.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.AbortException;
import edu.illinois.ncsa.datawolf.FailedException;
import edu.illinois.ncsa.datawolf.LocalExecutor;
import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.executor.java.tool.JavaTool;
import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.util.BeanUtil;

/**
 * Executor for java tools. This is just a little wrapper for calling
 * the functions defined by the JavaTool interface.
 * 
 * @author Rob Kooper
 * 
 */
public class JavaExecutor extends LocalExecutor {
    private static Logger logger        = LoggerFactory.getLogger(JavaExecutor.class);

    public static String  EXECUTOR_NAME = "java";

    private Thread        workerThread  = null;

    @Override
    public String getExecutorName() {
        return EXECUTOR_NAME;
    }

    @Override
    public void stopJob() {
        super.stopJob();
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    @Override
    public void execute(File cwd) throws AbortException, FailedException {
        workerThread = Thread.currentThread();

        // run with the classloader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DisposeClassLoader dcl = new DisposeClassLoader(cl);
        dcl.setWorkDir(cwd);

        // get all jarfiles
        Set<File> jarfiles = new HashSet<File>();
        getJarFiles(cwd, jarfiles);
        for (File file : jarfiles) {
            dcl.addJarFile(file);
        }

        // the java tool.
        JavaTool tool;

        // list of inputstreams we opened.
        Set<InputStream> inputs = new HashSet<InputStream>();

        // make sure the classes will get unloaded
        try {

            // Transaction t = SpringData.getTransaction();

            try {
                work.begin();
                // t.start();

                WorkflowStep step = workflowStepDao.findOne(getStepId());
                Execution execution = executionDao.findOne(getExecutionId());
                JavaToolImplementation impl = BeanUtil.JSONToObject(step.getTool().getImplementation(), JavaToolImplementation.class);

                logger.info("Executing " + step.getTitle() + " with tool " + step.getTool().getTitle());

                // parse the implementation
                String classname = impl.getToolClassName();
                logger.debug("Implementation class : " + classname);

                // create instance of the tool
                try {
                    tool = (JavaTool) dcl.loadClass(classname).newInstance();
                } catch (Exception exc) {
                    throw (new FailedException("Could load class.", exc));
                }

                // set temp folder
                tool.setTempFolder(cwd);

                // set parameters
                if (tool.getParameters() != null) {
                    for (Entry<String, String> entry : step.getParameters().entrySet()) {
                        String value = null;
                        if (execution.hasParameter(entry.getValue())) {
                            value = execution.getParameter(entry.getValue());
                        } else {
                            value = step.getTool().getParameter(entry.getKey()).getValue();
                        }
                        tool.setParameter(entry.getKey(), value);
                    }
                }

                // set inputs
                if (tool.getInputs() != null) {
                    for (Entry<String, String> entry : step.getInputs().entrySet()) {
                        Dataset ds = datasetDao.findOne(execution.getDataset(entry.getValue()));
                        if (ds == null) {
                            throw (new AbortException("Dataset is missing."));
                        }
                        if (ds.getFileDescriptors().size() == 0) {
                            throw (new FailedException("Dataset does not have any files associated."));
                        }
                        InputStream is = fileStorage.readFile(ds.getFileDescriptors().get(0));
                        inputs.add(is);
                        tool.setInput(entry.getKey(), is);
                    }
                }
            } catch (AbortException e) {
                throw e;
            } catch (FailedException e) {
                throw e;
            } catch (Throwable e) {
                throw (new FailedException("Could not run transaction to save information about step.", e));
            } finally {
                work.end();
                // try {
                // if (t != null) {
                // t.commit();
                // }
                // } catch (Exception e) {
                // throw (new
// FailedException("Could not commit transaction to save information about step.",
// e));
                // }
            }

            // check if job is still alive
            if (isJobStopped()) {
                throw (new AbortException("Job is stopped."));
            }

            // run the actual code.
            try {
                workerThread = Thread.currentThread();
                tool.execute();
            } catch (Throwable e) {
                if (isJobStopped()) {
                    throw (new AbortException("Job is stopped.", e));
                }
            } finally {
                workerThread = null;
            }

            // close inputs
            for (InputStream is : inputs) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("Error closing inputstream.", e);
                }
            }

            // get outputs in the case of DataWolfTool
            if (tool.getOutputs() != null) {
                // t = SpringData.getTransaction();
                // List of created datasets
                List<AbstractBean> datasets = new ArrayList<AbstractBean>();
                try {
                    work.begin();
                    // t.start();
                    WorkflowStep step = workflowStepDao.findOne(getStepId());
                    Execution execution = executionDao.findOne(getExecutionId());

                    for (Entry<String, String> entry : step.getOutputs().entrySet()) {
                        WorkflowToolData data = step.getOutput(entry.getValue());

                        Dataset ds = new Dataset();
                        ds.setTitle(data.getTitle());
                        ds.setDescription(data.getDescription());
                        ds.setCreator(execution.getCreator());

                        FileDescriptor fd = fileStorage.storeFile(data.getTitle(), tool.getOutput(entry.getKey()), execution.getCreator(), ds);
                        fd.setMimeType(data.getMimeType());

                        ds = datasetDao.save(ds);

                        execution.setDataset(entry.getValue(), ds.getId());
                        datasets.add(ds);
                    }

                    executionDao.save(execution);

                    // } catch (AbortException e) {
                    // throw e;
                    // } catch (FailedException e) {
                    // throw e;
                } catch (Throwable e) {
                    throw (new FailedException("Error saving output from step.", e));
                } finally {
                    work.end();
                    // try {
                    // if (t != null) {
                    // t.commit();
                    // TODO is this needed?
                    // SpringData.getEventBus().fireEvent(new
// ObjectCreatedEvent(datasets));
                    // }
                    // } catch (Exception e) {
                    // throw (new
// FailedException("Could not commit transaction to save information about step.",
// e));
                    // }
                }
            }

        } finally {
            // all done
            tool = null;
            dcl.dispose();
            System.gc();
        }
    }

    private void getJarFiles(File folder, Set<File> result) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                getJarFiles(file, result);
            } else if (file.getName().toLowerCase().endsWith(".jar")) { //$NON-NLS-1$
                result.add(file);
            }
        }
    }
}
