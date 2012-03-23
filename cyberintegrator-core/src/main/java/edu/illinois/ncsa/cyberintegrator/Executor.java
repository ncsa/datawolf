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
package edu.illinois.ncsa.cyberintegrator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.domain.FileDescriptor;

/**
 * Abstract class representing an executor. In the case of the Cyberintegrator
 * the executors implement a specific piece of code that encapsulates the
 * execution of a tool. Example of these executors are command line executor
 * (External), cyberintegrator tool executors (Java) and Matlab executor
 * (Matlab).
 * 
 * @author Rob Kooper
 */
public abstract class Executor {
    private static final Logger                           logger    = LoggerFactory.getLogger(Executor.class);

    /** All known executors. */
    private static Map<String, Class<? extends Executor>> executors = new HashMap<String, Class<? extends Executor>>();

    /**
     * Find an executor that fits the given name. This will create a new
     * instance of the executor.
     * 
     * @param name
     *            the name of the executor to find.
     * @return an instance of the executor.
     */
    public static Executor findExecutor(String name) {
        try {
            return executors.get(name).newInstance();
        } catch (Exception e) {
            logger.error("Could not create an instance of the executor.", e);
            return null;
        }
    }

    /**
     * Add an executor to the list of known executors.
     * 
     * @param name
     *            the name of the exector, this is the same name as used in the
     *            tool.
     * @param executor
     *            the executor.
     */
    public static void addExecutor(String name, Class<? extends Executor> executor) {
        executors.put(name, executor);
    }

    /**
     * This will stage any files needed for execution in a temporary directory.
     * This function is called before the run function. When overriding this
     * function make sure you call the super.
     * 
     * @throws Exception
     *             an exception can be thrown when something goes wrong in the
     *             cleanup (for example files that could not be created).
     */
    public void setup(File cwd, WorkflowStep step) throws Exception {
        for (FileDescriptor blob : step.getTool().getBlobs()) {
            // TODO RK : fetch all blobs.
//            try {
//                File zipfile = TupeloHelper.getBlob(context, tool);
//                if (zipfile != null) {
//                    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipfile));
//                    if (zis != null) {
//                        byte[] buf = new byte[10240];
//                        int len;
//                        ZipEntry ze;
//                        while ((ze = zis.getNextEntry()) != null) {
//                            if (!ze.isDirectory()) {
//                                File file = new File(cwd, ze.getName());
//                                file.getParentFile().mkdirs();
//                                FileOutputStream fos = new FileOutputStream(file);
//                                while ((len = zis.read(buf)) > 0) {
//                                    fos.write(buf, 0, len);
//                                }
//                                fos.close();
//                                zis.closeEntry();
//                            }
//                        }
//                        zis.close();
//                    }
//                    zipfile.delete();
//                }
//            } catch (IOException e) {
//                log.warn("Could not extract files associated with this tool.", e);
//            } catch (OperatorException e) {
//                if ((e instanceof NotFoundException) || (e.getCause() instanceof NotFoundException)) {
//                    log.debug("No files are associated with this tool.");
//                } else {
//                    log.warn("Could not retrieve files associated with this tool.", e);
//                }
//            }
        }

        // if not on windows, chmod on temp directory so that files are
        // executable
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) { //$NON-NLS-1$//$NON-NLS-2$
            Process p = Runtime.getRuntime().exec("chmod -R u+x " + cwd.getAbsolutePath()); //$NON-NLS-1$
            p.waitFor();
        }
    }

    /**
     * Before a step is executed this will check to see if the executor is ready
     * to run. Some executors need a license, this allows the executor to check
     * and see if a license is available before the step is executed. This can
     * reserve any items needed to run. The executer will be started soon.
     * 
     * @return true if the executor can be run.
     */
    public boolean canRun() {
        return true;
    }

    /**
     * The executor can indicate if it is safe to use the same JVM as the
     * Cyberintegrator. If set to false, the Cyberintegrator will create a
     * complete new JVM in which this tool will run, the is useful for JAVA code
     * that calls System.exit() since this could kill the Cyberintegrator JVM as
     * well. The default is for each step to require their own JVM.
     * 
     * @return false if each step requires its own JVM.
     */
    public boolean useSameJVM() {
        return false;
    }

    /**
     * The executor should try to kill the execution. The step should be stopped
     * from executing.
     * 
     * @throws Exception
     *             if the step could not bet stopped.
     */
    abstract public void kill() throws Exception;

    /**
     * This will run the tool specified. This function is responsible for
     * setting up the inputs, parameters for the tool, executing the tool and
     * retrieving the output generated by the tool. This function is called
     * after the run function.
     * 
     * @throws Exception
     *             an exception can be thrown if anything goes wrong during
     *             execution, either in the user code or in the executor.
     */
    public void run(Execution execution, WorkflowStep step) throws AbortException, FailedException {
        // create a temp directory
        File cwd = null;
        try {
            cwd = File.createTempFile("cib", ".dir"); //$NON-NLS-1$ //$NON-NLS-2$
            cwd.delete();
            if (!cwd.mkdirs()) {
                throw (new FailedException("Error creating working directory"));
            }
        } catch (FailedException e) {
            throw (e);
        } catch (Exception e) {
            throw (new FailedException("Error creating working directory", e));
        }

        // setup the executor
        try {
            setup(cwd, step);
        } catch (Exception e) {
            throw (new FailedException("Error setting up executor.", e));
        }

        // launch the job
        try {
            execute(cwd, execution, step);
        } catch (Throwable thr) {
            throw (new FailedException("Error running executor.", thr));
        }

        // do some cleanup
        if (!deleteDirectory(cwd)) {
            logger.info(String.format("Could not remove directory [%s].", cwd.getAbsolutePath()));
        }
    }

    /**
     * This will run the tool specified. This function is responsible for
     * setting up the inputs, parameters for the tool, executing the tool and
     * retrieving the output generated by the tool. This function is called
     * after the run function.
     * 
     * @param cwd
     *            the folder created for this specific execution.
     * @param execution
     *            all the information collected for the execution of the step.
     * @param step
     *            the step that needs to be executed.
     * @throws Exception
     *             an exception can be thrown if anything goes wrong during
     *             execution, either in the user code or in the executor.
     */
    public abstract void execute(File cwd, Execution execution, WorkflowStep step) throws AbortException, FailedException;

    /**
     * Recursively remove a directory and all files in the directory.
     * 
     * @param path
     *            the directory to be removed
     * @return true if the directory could be deleted.
     */
    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File element : files) {
                if (element.isDirectory()) {
                    deleteDirectory(element);
                } else {
                    element.delete();
                }
            }
        }
        return (path.delete());
    }
}
