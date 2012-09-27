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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * Abstract class representing a local executor. Local executors will run on the
 * local machine and use resources on the local machine. Examples of local
 * executors are the java executor and the command line executor..
 * 
 * @author Rob Kooper
 */
public abstract class LocalExecutor extends Executor implements Runnable {
    private static final Logger       logger = LoggerFactory.getLogger(LocalExecutor.class);

    private static ThreadPoolExecutor threadpool;

    static {
        threadpool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    // ----------------------------------------------------------------------
    // EXECUTOR THREADPOOL
    // ----------------------------------------------------------------------
    public static void setWorkers(int workers) {
        logger.info("Changing workers to " + workers);
        threadpool.setCorePoolSize(workers);
        threadpool.setMaximumPoolSize(workers);
    }

    public static int getWorkers() {
        return threadpool.getMaximumPoolSize();
    }

    public static boolean isBusy() {
        return threadpool.getActiveCount() >= threadpool.getMaximumPoolSize();
    }

    // ----------------------------------------------------------------------
    // EXECUTOR IMPLEMENTATION
    // ----------------------------------------------------------------------
    /**
     * This will run the tool specified. At this point the working directory
     * is filled with all files needed for the execution of the tool.
     * 
     * @param cwd
     *            the folder in which the executor can store all data.
     * @throws AbortException
     *             an abort exception is thrown if the execution is aborted.
     * @throws FailedException
     *             a failed exception is thrown if the execution is failed to
     *             execute properly.
     */
    public abstract void execute(File cwd) throws AbortException, FailedException;

    @Override
    public void startJob() {
        setState(State.QUEUED);
        threadpool.execute(this);
    }

    public void stopJob() {
        super.stopJob();
        threadpool.remove(this);
    }

    // ----------------------------------------------------------------------
    // EXECUTE CODE
    // ----------------------------------------------------------------------

    @Override
    public void run() {
        setState(State.RUNNING);
        File cwd = null;

        try {
            // create a temp directory
            cwd = File.createTempFile("cib", ".dir"); //$NON-NLS-1$ //$NON-NLS-2$
            cwd.delete();
            if (!cwd.mkdirs()) {
                throw (new Exception("Error creating working directory."));
            }

            // setup the executor
            setup(cwd);

            // launch the job
            execute(cwd);

            // mark step as finished
            setState(State.FINISHED);
        } catch (AbortException exc) {
            logger.info("Job was aborted", exc);
            setState(State.ABORTED);
            println("job was aborted.", exc);
        } catch (FailedException exc) {
            logger.info("Job has failed", exc);
            setState(State.FAILED);
            println("job failed.", exc);
        } catch (Throwable thr) {
            logger.info("Error during execution.", thr);
            setState(State.FAILED);
            println("Error during execution.", thr);
        } finally {
            // do some cleanup
            if ((cwd != null) && !deleteDirectory(cwd)) {
                String msg = String.format("Could not remove directory [%s].", cwd.getAbsolutePath());
                logger.info(msg);
                println(msg);
            }
        }
    }

    /**
     * This will stage any files needed for execution in a temporary directory.
     * This function is called before the run function. When overriding this
     * function make sure you call the super.
     */
    private void setup(File cwd) throws FailedException {
        Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();

        Transaction t = SpringData.getTransaction();
        try {
            t.start(true);
            WorkflowStep step = SpringData.getBean(WorkflowStepDAO.class).findOne(getStepId());
            blobs.addAll(step.getTool().getBlobs());
        } catch (Exception e) {
            throw (new FailedException("Could not get all blobs.", e));
        } finally {
            try {
                t.commit();
            } catch (Exception e) {
                throw (new FailedException("Could not get all blobs.", e));
            }
        }

        try {
            for (FileDescriptor blob : blobs) {
                byte[] buf = new byte[10240];
                InputStream is = SpringData.getFileStorage().readFile(blob);
                FileOutputStream os = new FileOutputStream(new File(cwd, blob.getFilename()));
                int len;
                while ((len = is.read(buf)) > 0) {
                    os.write(buf, 0, len);
                }
                os.close();
                is.close();
            }
        } catch (IOException e) {
            throw (new FailedException("Could not save blobs.", e));
        }

        // if not on windows, chmod on temp directory so that files are
        // executable
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) { //$NON-NLS-1$//$NON-NLS-2$
            try {
                Process p = Runtime.getRuntime().exec("chmod -R u+x " + cwd.getAbsolutePath()); //$NON-NLS-1$
                p.waitFor();
            } catch (Exception e) {
                throw (new FailedException("Could not change permissions of files.", e));
            }
        }
    }

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
