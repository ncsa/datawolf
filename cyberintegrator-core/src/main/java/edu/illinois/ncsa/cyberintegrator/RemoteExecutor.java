/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * This executor will run the job on a remote server. There is no limits on the
 * number of remote executors that can run in parallel. Each remote executor
 * will run in its own thread. The thread will submit the job and check on the
 * job status continuously.
 * 
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public abstract class RemoteExecutor extends Executor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RemoteExecutor.class);

    private Thread              remoteThread;

    /*
     * Create a thread and launch the thread that will do the real work.
     */
    @Override
    public void startJob() {
        setState(State.QUEUED);
        remoteThread = new Thread(this);
        remoteThread.start();
    }

    /*
     * Mark the thread as stopped and call the cancelRemoteJob() function.
     */
    @Override
    public void stopJob() {
        super.stopJob();
        cancelRemoteJob();
        Thread tmp = remoteThread;
        remoteThread = null;
        if (tmp != null) {
            tmp.interrupt();
        }
    }

    /**
     * Submit the remote job and keep checking to see what the status of the job
     * is and fetch any remote information.
     */
    public void run() {
        // setup anything local required for the job
        File cwd = null;

        try {
            cwd = File.createTempFile("cib", ".dir"); //$NON-NLS-1$ //$NON-NLS-2$
            cwd.delete();
            if (!cwd.mkdirs()) {
                throw (new Exception("Error creating working directory."));
            }

            // setup the executor
            setup(cwd);

            // submit job first
            State state = submitRemoteJob(cwd);
            setState(state);

            while ((state != State.ABORTED) && (state != State.FAILED) && (state != State.FINISHED)) {
                State newstate = checkRemoteJob();
                if ((newstate != null) && (newstate != state)) {
                    setState(newstate);
                    state = newstate;
                }

                // get the log only if running
                if (state == State.RUNNING) {
                    String log = getRemoteLog();
                    if ((log != null) && !log.equals("")) {
                        setLog(log);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("Got interrupted.", e);
                }
            }

            // get the log at the end always
            String log = getRemoteLog();
            if ((log != null) && !log.equals("")) {
                setLog(log);
            }

        } catch (AbortException e) {
            setState(State.ABORTED);
            println("job was aborted.", e);
        } catch (FailedException e) {
            setState(State.FAILED);
            println("job failed", e);
        } catch (Throwable thr) {
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
     * Submit the job to the remote server. This will return the new state of
     * step.
     * 
     * @throws AbortException
     *             an abort exception is thrown if the job submission is
     *             aborted.
     * @throws FailedException
     *             a failed exception is thrown if the job submission fails to
     *             submit properly.
     * @return the new state of the step
     */
    public abstract State submitRemoteJob(File cwd) throws AbortException, FailedException;

    /**
     * Tries to cancel the job on the remote server.
     * 
     */
    public abstract void cancelRemoteJob();

    /**
     * Checks the status of the job on the remote server. This will return the
     * current state of the step. This function can return null if the state of
     * the step has not changed since the previous call.
     * 
     * @return the state of the step (this could be the same state as before, or
     *         null if the state did not change).
     * @throws FailedException
     *             a failed exception is thrown if the status check fails.
     */
    public abstract State checkRemoteJob() throws FailedException;

    /**
     * Checks the remote server for any log messages. This will assume that the
     * log returned is the full log from the server. This can return null if the
     * log did not change.
     * 
     * @return the log from the server.
     */
    public abstract String getRemoteLog();

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
