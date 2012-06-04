/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator;

/**
 * Abstract class representing a service executor. Service executors will run on
 * a remote machine and will use little resources on the local machine. Service
 * executors will often have a thread running that keeps polling the remote
 * service for the state of the job that was submitted.
 * 
 * @author Rob Kooper
 */
public abstract class ServiceExecutor extends Executor implements Runnable {
    private Thread executionThread = null;

    /**
     * Run the actual step. This function will be called from its own thread
     * and should not return until the step is finished. If there was an error
     * during execution of the step it can throw a specific exception.
     * 
     * If the job is to be stopped during execution the isJobStopped will return
     * true and the thread should be exited as quickly as possible, removing if
     * possible any remotely submitted jobs.
     */
    public abstract void run();

    @Override
    public void startJob() {
        executionThread = new Thread(this);
        executionThread.start();
    }

    @Override
    public void stopJob() {
        super.stopJob();

        Thread t = executionThread;
        executionThread = null;
        t.interrupt();
    }
}
