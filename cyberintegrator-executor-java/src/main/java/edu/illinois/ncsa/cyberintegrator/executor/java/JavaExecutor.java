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
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.JavaTool;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
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
    private static Logger logger        = LoggerFactory.getLogger(JavaExecutor.class);

    public static String  EXECUTOR_NAME = "java";

    private Thread        worker        = null;
    private boolean       interrupted   = false;

    @Override
    public String getExecutorName() {
        return EXECUTOR_NAME;
    }

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
                for (Entry<String, String> entry : step.getParameters().entrySet()) {
                    String value = execution.getParameter(entry.getValue());
                    tool.setParameter(entry.getKey(), value);
                }
            }

            // set inputs
            if (tool.getInputs() != null) {
                for (Entry<String, String> entry : step.getInputs().entrySet()) {
                    Dataset ds = execution.getDataset(entry.getValue());
                    if (ds == null) {
                        throw (new AbortException("Dataset is missing."));
                    }
                    if (ds.getFileDescriptors().size() == 0) {
                        throw (new FailedException("Dataset does not have any files associated."));
                    }
                    InputStream is = SpringData.getFileStorage().readFile(ds.getFileDescriptors().get(0));
                    tool.setInput(entry.getKey(), is);
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
                for (Entry<String, String> entry : step.getOutputs().entrySet()) {
                    WorkflowToolData data = step.getOutput(entry.getValue());

                    FileDescriptor fd = SpringData.getFileStorage().storeFile(tool.getOutput(entry.getKey()));
                    fd.setMimeType(data.getMimeType());

                    Dataset ds = new Dataset();
                    ds.setTitle(data.getTitle());
                    ds.setDescription(data.getDescription());
                    ds.setCreator(execution.getCreator());
                    ds.addFileDescriptor(fd);
                    SpringData.getBean(DatasetDAO.class).save(ds);

                    execution.setDataset(entry.getValue(), ds);
                }
                SpringData.getBean(ExecutionDAO.class).save(execution);
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
