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
package edu.illinois.ncsa.cyberintegrator.executor.java;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.AbortException;
import edu.illinois.ncsa.cyberintegrator.Executor;
import edu.illinois.ncsa.cyberintegrator.FailedException;
import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.JavaTool;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Parameter;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * Executor for java tools. This is just a little wrapper for calling
 * the functions defined by the JavaTool interface.
 * 
 * @author Rob Kooper
 * 
 */
public class JavaExecutor extends Executor {
    static private Logger logger      = LoggerFactory.getLogger(JavaExecutor.class);

    private Thread        worker      = null;
    private boolean       interrupted = false;

    @Override
    public void kill() throws Exception {
        interrupted = true;
        if (worker != null) {
            worker.interrupt();
        }
    }

    @Override
    public void execute(File cwd, Execution execution, WorkflowStep step) throws AbortException, FailedException {
        logger.info("Executing " + step.getTitle() + " with tool " + step.getTool().getTitle());

        // store worker so we can get interrupted.
        worker = Thread.currentThread();
        if (interrupted) {
            throw (new FailedException("Thread got interrupted."));
        }

        // parse the implementation
        // String[] impl = step.getTool().getImplementation().split("\n");
        // String classname = impl[impl.length - 1];
        JavaToolImplementation impl = (JavaToolImplementation) step.getTool().getImplementation();
        String classname = impl.getToolClassName();// impl[impl.length - 1];
        logger.debug("Implementation class : " + classname);

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

        // the real code
        try {
            // create instance of the tool
            JavaTool tool;
            try {
                tool = (JavaTool) dcl.loadClass(classname).newInstance();
            } catch (Exception exc) {
                throw (new FailedException("Could load class.", exc));
            }

            // set temp folder
            tool.setTempFolder(cwd);

            // set parameters
            if (tool.getParameters() != null) {
                for (Parameter parameter : tool.getParameters()) {
                    // check for parameters that are set
                    String value = execution.getParameter(parameter.getID());

                    // if no parameter check tool
                    if (value == null) {
                        for (WorkflowToolParameter wtp : step.getTool().getParameters()) {
                            if (wtp.getId().equals(parameter.getID())) {
                                value = wtp.getValue();
                                break;
                            }
                        }
                    }

                    // empty string is a null parameter
                    if (value.equals("")) { //$NON-NLS-1$
                        tool.setParameter(parameter.getID(), null);
                    } else {
                        tool.setParameter(parameter.getID(), value);
                    }
                }
            }

            // set inputs
            if (tool.getInputs() != null) {
                for (Entry<WorkflowToolData, String> entry : step.getInputs().entrySet()) {
                    Dataset ds = execution.getDataset(entry.getValue());
                    if (ds == null) {
                        throw (new AbortException("Dataset is missing."));
                    }
                    InputStream is = SpringData.getFileStorage().readFile(ds.getFileDescriptors().get(0));
                    tool.setInput(entry.getKey().getDataId(), is);
                }
            }

            // execute
            if (interrupted) {
                throw (new FailedException("Thread got interrupted."));
            }
            tool.execute();
            if (interrupted) {
                throw (new FailedException("Thread got interrupted."));
            }

            // get outputs in the case of CyberintegratorTool
            if (tool.getOutputs() != null) {
                Transaction t = SpringData.getTransaction();
                t.start();
                for (Entry<String, WorkflowToolData> entry : step.getOutputs().entrySet()) {
                    FileDescriptor fd = SpringData.getFileStorage().storeFile(tool.getOutput(entry.getValue().getDataId()));
                    fd.setMimeType(entry.getValue().getMimeType());

                    Dataset ds = new Dataset();
                    ds.setTitle(entry.getValue().getTitle());
                    ds.setDescription(entry.getValue().getDescription());
                    // TODO RK : add creator
                    // ds.setCreator(creator)
                    ds.addFileDescriptor(fd);
                    SpringData.getBean(DatasetDAO.class).save(ds);
                }
                t.commit();
            }

        } catch (AbortException e) {
            logger.info("Step aborted.", e);
            throw (e);
        } catch (FailedException e) {
            logger.info("Step failed.", e);
            throw (e);
        } catch (Throwable e) {
            logger.info("Step failed.", e);
            throw (new FailedException("Uncaught exception was thrown, step failed.", e));
        } finally {
            // all done
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
