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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * Engine that is responsible for executing the steps. This is an abstract class
 * that defines the methods that the engine will need to implement.
 * 
 * @author Rob Kooper
 */
public class Engine {
    private static final Logger   logger       = LoggerFactory.getLogger(Engine.class);

    /** All known executors. */
    private Map<String, Executor> executors    = new HashMap<String, Executor>();

    /** List of all workers */
    private WorkerThread          engineThread = new WorkerThread();

    /** List of steps */
    private List<Executor>        queue        = new ArrayList<Executor>();

    /**
     * Create the engine with a single worker.
     */
    public Engine() {
        engineThread.setName("EngineThread");
        engineThread.start();
        loadQueue();
    }

    public void setLocalExecutorThreads(int threads) {
        LocalExecutor.setWorkers(threads);
    }

    /**
     * Find an executor that fits the given name. This will create a new
     * instance of the executor.
     * 
     * @param name
     *            the name of the executor to find.
     * @return an instance of the executor.
     */
    public Executor findExecutor(String name) {
        try {
            return executors.get(name).getClass().newInstance();
        } catch (Exception e) {
            logger.error("Could not create an instance of the executor.", e);
            return null;
        }
    }

    /**
     * Add an executor to the list of known executors.
     * 
     * @param executor
     *            the executor, any executions will be done on a new instance of
     *            the executor.
     */
    public void addExecutor(Executor executor) {
        logger.info(String.format("Adding executor : %s", executor.getExecutorName()));
        executors.put(executor.getExecutorName(), executor);
    }

    @Required
    public void setExecutors(Set<Executor> executors) {
        this.executors.clear();
        for (Executor executor : executors) {
            addExecutor(executor);
        }
    }

    public Set<Executor> getExecutors() {
        return new HashSet<Executor>(executors.values());
    }

    /**
     * Submit the all the steps in the workflow to the engine to be executed.
     * This will submit the step to the engine and the engine will execute this
     * step with the given execution environment. Based on the engine, this
     * could happen immediately, on a remote server or at some time in the
     * future.
     * 
     * @param execution
     *            the execution information
     * @throws Exception
     *             if the step could not be added to the list.
     */
    public void execute(Execution execution) {
        try {
            Transaction t = SpringData.getTransaction();
            try {
                t.start();
                for (WorkflowStep step : SpringData.getBean(WorkflowDAO.class).findOne(execution.getWorkflowId()).getSteps()) {
                    execute(execution, step);
                }
            } finally {
                t.commit();
            }
        } catch (Exception e) {
            logger.error("Could not get transaction to get steps.", e);
        }
    }

    /**
     * Submit the step to the engine to be executed. This will submit the step
     * to the engine and the engine will execute this step with the given
     * execution environment. Based on the engine, this could happen
     * immediately, on a remote server or at some time in the future.
     * 
     * @param execution
     *            the execution information
     * @param steps
     *            the list of steps to be executed.
     */
    public void execute(Execution execution, String... steps) {
        try {
            Transaction transaction = SpringData.getTransaction();
            transaction.start();
            for (String step : steps) {
                execute(execution, SpringData.getBean(WorkflowStepDAO.class).findOne(step));
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Could not execute steps.", e);
        }
    }

    /**
     * Submit the step to the engine to be executed. This will submit the step
     * to the engine and the engine will execute this step with the given
     * execution environment. Based on the engine, this could happen
     * immediately, on a remote server or at some time in the future.
     * 
     * @param execution
     *            the execution information
     * @param steps
     *            the list of steps to be executed.
     */
    public void execute(Execution execution, WorkflowStep... steps) {
        for (WorkflowStep step : steps) {
            Executor exec = findExecutor(step.getTool().getExecutor());
            if (exec == null) {
                logger.error(String.format("Could not find an executor [%s] for step %s.", step.getTool().getExecutor(), step.getId()));
            } else {
                exec.setJobInformation(execution, step);
                addJob(exec);
            }
        }
    }

    public boolean hasStep(String executionId, String stepId) {
        synchronized (queue) {
            for (Executor exec : queue) {
                if (exec.getExecutionId().equals(executionId) && exec.getStepId().equals(stepId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Collection<String> getSteps(String executionId) {
        List<String> steps = new ArrayList<String>();
        synchronized (queue) {
            for (Executor exec : queue) {
                if (exec.getExecutionId().equals(executionId)) {
                    steps.add(exec.getStepId());
                }
            }
        }
        return steps;
    }

    /**
     * Stops a particular step from being executed, and removes it from the
     * list. If the step is already executing attempts will be made to stop the
     * step from executing.
     * 
     * @param executionId
     *            the execution Id
     * 
     * @param steps
     *            the list of steps to be stopped.
     */
    public void stop(String executionId, String... steps) {
        List<String> allsteps = Arrays.asList(steps);

        synchronized (queue) {
            for (Executor exec : queue) {
                if (exec.getExecutionId().equals(executionId) && allsteps.contains(exec.getStepId())) {
                    exec.stopJob();
                }
            }
        }
    }

    /**
     * Stops all steps in a execution from being executed, and removes it from
     * the
     * list. If the step is already executing attempts will be made to stop the
     * step from executing.
     * 
     * @param executionId
     *            the execution Id
     */
    public void stop(String executionId) {
        synchronized (queue) {
            for (Executor exec : queue) {
                if (exec.getExecutionId().equals(executionId)) {
                    exec.stopJob();
                }
            }
        }
    }

    /**
     * Stop all steps from executing. This will remove all non executed steps
     * from the queue, and attempt to stop all steps currently executing.
     */
    public void stopAll() {
        synchronized (queue) {
            for (Executor exec : queue) {
                exec.stopJob();
            }
        }
    }

    private void addJob(Executor exec) {
        synchronized (queue) {
            queue.add(exec);
            saveQueue();
        }
    }

    /**
     * Save the queue to the underlying persistence layer.
     */
    private void saveQueue() {
        // TODO RK : Enable saving of queue to spring-data
    }

    /**
     * Load the queue from the underlying persistence layer.
     */
    private void loadQueue() {
        // TODO RK : Enable loading of queue from spring-data
    }

    class WorkerThread extends Thread {
        public void run() {
            int idx = 0;
            for (;;) {
                try {
                    if (queue.size() > 0) {
                        if (idx >= queue.size()) {
                            idx = 0;
                        }
                        Executor exec = queue.get(idx);

                        switch (exec.getState()) {
                        case UNKNOWN:
                        case QUEUED:
                        case RUNNING:
                            idx++;
                            break;

                        case ABORTED:
                        case FAILED:
                        case FINISHED:
                            synchronized (queue) {
                                queue.remove(idx);
                            }
                            saveQueue();
                            idx = 0;
                            break;

                        case WAITING:
                            // 0 = OK, 1 = WAIT, 2 = ERROR
                            int canrun = 0;

                            // if job is stopped mark as aborted
                            if (exec.isJobStopped()) {
                                canrun = 2;
                            } else {
                                Transaction transaction = null;
                                try {
                                    transaction = SpringData.getTransaction();
                                    transaction.start();

                                    Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(exec.getExecutionId());
                                    WorkflowStep step = SpringData.getBean(WorkflowStepDAO.class).findOne(exec.getStepId());

                                    // check to see if all inputs of the
                                    // step are ready
                                    for (String id : step.getInputs().values()) {
                                        if (!execution.hasDataset(id)) {
                                            canrun = 1;
                                        } else if (execution.getDataset(id) == null) {
                                            canrun = 2;
                                        } else if (Execution.EMPTY_DATASET.equals(execution.getDataset(id))) {
                                            canrun = 2;
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.error("Error getting job information.", e);
                                } finally {
                                    try {
                                        transaction.commit();
                                    } catch (Exception e) {
                                        logger.error("Error getting job information.", e);
                                    }

                                }
                            }

                            // check to make sure the executor can run
                            switch (canrun) {
                            case 0:
                                if (exec.isExecutorReady()) {
                                    exec.startJob();
                                    idx = 0;
                                } else {
                                    idx++;
                                }
                                break;
                            case 1:
                                idx++;
                                break;
                            case 2:
                                exec.setState(edu.illinois.ncsa.cyberintegrator.domain.Execution.State.ABORTED);
                                exec.stopJob();
                                synchronized (queue) {
                                    queue.remove(idx);
                                }
                                saveQueue();
                                idx = 0;
                                break;
                            }
                            break;
                        }
                    }

                    // little sleep
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {}
                } catch (Throwable thr) {
                    logger.error("Runaway exception in engine.", thr);
                }
            }
        }
    }
}
