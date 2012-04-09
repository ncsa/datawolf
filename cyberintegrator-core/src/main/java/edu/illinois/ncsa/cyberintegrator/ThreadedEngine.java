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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;

public class ThreadedEngine extends Engine {
    private static Logger                    logger     = LoggerFactory.getLogger(ThreadedEngine.class);

    public static final String               WORKERS    = "workers";                                    //$NON-NLS-1$
    public static final String               ONEJVM     = "onejvm";                                     //$NON-NLS-1$

    private boolean                          onejvm     = false;
    private ArrayList<WorkerThread>          workers    = new ArrayList<WorkerThread>();
    private Map<ExecutionInfo, WorkerThread> processing = new HashMap<ExecutionInfo, WorkerThread>();

    /**
     * Return the list of default prooperties.
     * 
     * @return list of default properties for any new ThreadedEngine.
     */
    public static Properties getDefaultProperties() {
        Properties defaults = new Properties();
        int procs = Runtime.getRuntime().availableProcessors() - 1;
        if (procs < 1) {
            procs = 1;
        }
        defaults.put(WORKERS, Integer.toString(procs));
        defaults.put(ONEJVM, Boolean.toString(false));
        return defaults;
    }

    /**
     * Create a new ThreadedEngine with default properties.
     */
    public ThreadedEngine() {
        this(getDefaultProperties());
    }

    /**
     * Create a new ThreadedEngine with the list of properties specified.
     * 
     * @param properties
     */
    public ThreadedEngine(Properties properties) {
        super(properties);
    }

    @Override
    protected void kill(ExecutionInfo executionInfo) throws Exception {
        processing.get(executionInfo).kill();
    }

    /**
     * Based on properties initialize the engine.
     */
    @Override
    protected void initialize() {
        if (getProperty(ONEJVM) != null) {
            onejvm = Boolean.parseBoolean(getProperty(ONEJVM));
        } else {
            onejvm = false;
        }

        int numworkers = 2;
        if (getProperty(WORKERS) != null) {
            try {
                numworkers = Integer.parseInt(getProperty(WORKERS));
                if (numworkers <= 0) {
                    throw (new NumberFormatException("Number of workers is less than 1."));
                }
            } catch (NumberFormatException exc) {
                numworkers = 2;
                logger.warn("Could not parse number of workers, defaulting to 2.", exc);
            }
        }
        setWorkers(numworkers);
    }

    /**
     * Set the number of workers for this engine. This is a one time change and
     * will not be stored in the list of properties.
     * 
     * @param numworkers
     *            the new number of workers.
     * 
     */
    synchronized public void setWorkers(int numworkers) {
        logger.info(String.format("Using %d worker threads.", numworkers));

        if (numworkers > workers.size()) {
            for (int i = workers.size(); i < numworkers; i++) {
                WorkerThread wt = new WorkerThread();
                wt.setName("ThreadedEngine Worker " + i); //$NON-NLS-1$
                workers.add(wt);
// LogAggregator.addLogListener(wt.getWorkerLog());
                wt.start();
            }
        } else if (numworkers < workers.size()) {
            while (workers.size() > numworkers) {
                WorkerThread wt = workers.remove(0);
// LogAggregator.removeLogListener(wt.getWorkerLog());
            }
        }
    }

    /**
     * Return the number of workers to handle jobs in the queue.
     * 
     * @return number of worker threads.
     */
    public int getWorkers() {
        return workers.size();
    }

    /**
     * One thread per worker. Each worker runs independently.
     */
    class WorkerThread extends Thread {
        private Process  process = null;
        private Executor executor;

//        private WorkerLog     workerlog     = null;
        public WorkerThread() {
//            workerlog = new WorkerLog();
        }

        public void kill() throws Exception {
            if (process == null) {
                process.destroy();
            } else {
                executor.kill();
            }
        }

        @Override
        public void run() {
            ExecutionInfo curr = null;
            ExecutionInfo last = null;

            while (workers.contains(Thread.currentThread())) {
                try {
                    curr = null;

                    // grab top item of the queue
                    synchronized (processing) {
                        for (ExecutionInfo ei : getQueue()) {
                            if ((last != ei) && (processing.get(ei) == null)) {
                                processing.put(ei, this);
                                curr = ei;
                                break;
                            }
                        }
                    }

                    // if nothing is waiting, try again
                    if (curr == null) {
                        last = null;
                        Thread.sleep(100);
                        continue;
                    }
                    last = curr;

                    Execution exection = curr.getExecution();
                    WorkflowStep step = curr.getStep();

                    // check to see if all inputs of the step are ready
                    for (String id : step.getInputs()) {
                        if (exection.getDataset(id) == null) {
                            for (WorkflowStep ws : exection.getWorkflow().getSteps()) {
                                if (ws.getOutputs().contains(id)) {
                                    switch (exection.getStepState(ws.getId())) {
                                    case WAITING:
                                    case RUNNING:
                                        processing.remove(curr);
                                        curr = null;
                                        break;
                                    case FINISHED:
                                        logger.error("Step is finished but dataset is missing!");
                                    case ABORTED:
                                    case FAILED:
                                        logger.debug("One of the inputs is failed, aborted or missing. Aborting step.");
                                        synchronized (processing) {
                                            processing.remove(curr);
                                            stepAborted(curr);
                                        }
                                        curr = null;
                                        break;
                                    }
                                }
                            }
                            if (curr != null) {
                                logger.error(String.format("Could not find any step responsible for this dataset [%s].", id));
                                stepAborted(curr);
                                processing.remove(curr);
                                curr = null;
                            }
                            break;
                        }
                    }

                    // can we still run?
                    if (curr == null) {
                        continue;
                    }

                    // make sure we have an executor
                    executor = Executor.findExecutor(step.getTool().getExecutor());
                    if (executor == null) {
                        logger.error(String.format("Could not find the right executor [%s] for tool [%s].", step.getTool().getExecutor(), step.getTool().getTitle()));
                        stepAborted(curr);
                        processing.remove(curr);
                        continue;
                    }

                    // check to make sure the executor can run
                    if (!executor.canRun()) {
                        synchronized (processing) {
                            postponeExecution(curr);
                            processing.remove(curr);
                        }
                        continue;
                    }

                    // finally step can be launched.
                    logger.debug(String.format("[%s] launching step %s.", Thread.currentThread().getName(), step.getTitle()));
//                    if (onejvm || executor.useSameJVM() || onejvm) {
                    // execute in same JVM
                    try {
                        executor.run(exection, step);
                    } catch (AbortException exc) {
                        logger.debug("Step is aborted", exc);
                        stepAborted(curr);
                    } catch (FailedException exc) {
                        logger.debug("Step is failed", exc);
                        stepFailed(curr);
                    } catch (Throwable thr) {
                        logger.debug("Step throws an exception, assume it failed", thr);
                        stepFailed(curr);
                    }
//                    } else {
//                        // serialize the context
//                        File contextfile = File.createTempFile("context", ".nt"); //$NON-NLS-1$ //$NON-NLS-2$
//                        PrintStream ps = new PrintStream(contextfile);
//                        for (Triple t : myjob.execution.getContextDefinition()) {
//                            ps.println(t);
//                        }
//                        ps.println(Triple.create(myjob.execution.getContextId(), Rdf.TYPE, Tupelo.DEFAULT_CONTEXT_TYPE).toString());
//                        ps.close();
//
//                        // create commandline list
//                        ArrayList<String> list = new ArrayList<String>();
//                        list.add(System.getProperty("eclipse.launcher")); //$NON-NLS-1$
//                        list.add("-nosplash"); //$NON-NLS-1$
//                        for (String x : System.getProperty("eclipse.commands").split("[\n\r]+")) { //$NON-NLS-1$ //$NON-NLS-2$
//                            list.add(x);
//                        }
//                        list.add(contextfile.getAbsolutePath());
//                        list.add(myjob.execution.getStep().toString());
//                        String y = ""; //$NON-NLS-1$
//                        for (String x : list) {
//                            y += x + " "; //$NON-NLS-1$
//                        }
//                        log.debug(y);
//
//                        // launch eclipse again but now to run
//                        ProcessBuilder pb = new ProcessBuilder(list);
//                        pb.redirectErrorStream(true);
//                        p = pb.start();
//                        log.debug("Second process started, waiting.");
//                        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                        String line;
//                        while (!isFinished() && ((line = br.readLine()) != null)) {
//                            log.debug(line);
//                        }
//                        p.waitFor();
//                        br.close();
//                        log.debug("Second process done.");
//
//                        // cleanup
//                        contextfile.delete();
//                    }

                    // step is finished
                    logger.debug(String.format("[%s] finished step %s.", Thread.currentThread().getName(), step.getTitle()));
                    processing.remove(curr);

                    // fire event
//                    fireStepFinished(curr.getStep(), exection.getStepState(curr.getStep()));

                } catch (Throwable thr) {
                    logger.warn("Uncaught exception in worker thread.", thr);
                }
            }
        }

        private boolean isFinished() {
            try {
                process.exitValue();
                return true;
            } catch (Throwable thr) {
                return false;
            }
        }

//        public LogListener getWorkerLog() {
//            return workerlog;
//        }
//
//        class WorkerLog implements LogListener {
//            /**
//             * Record any exceptions during the execution of a step.
//             */
//            public void log(LogLevel level, String name, String where, Object message, Throwable thr) {
//                if (Thread.currentThread().equals(this)) {
//                    if (es != null) {
//                        es.log("RUNTIME", message.toString(), thr); //$NON-NLS-1$
//                    }
//                }
//            }
//        }
    }

}
