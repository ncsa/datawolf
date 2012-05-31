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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.event.StepStateChangedEvent;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * Engine that is responsible for executing the steps. This is an abstract class
 * that defines the methods that the engine will need to implement.
 * 
 * @author Rob Kooper
 */
public abstract class Engine {
    final static public String    EXT_PT     = "edu.uiuc.ncsa.cyberintegrator.engine"; //$NON-NLS-1$

    final static protected Logger logger     = LoggerFactory.getLogger(Engine.class);

    private Properties            properties = new Properties();

    /** Is the engine up and running */
    private boolean               started    = false;

    /** List of submitted steps */
    private List<ExecutionInfo>   queue      = new ArrayList<Engine.ExecutionInfo>();

    /** All known executors. */
    private Map<String, Executor> executors  = new HashMap<String, Executor>();

    /**
     * Create the engine with no properties set.
     */
    public Engine() {
        this(null);
    }

    /**
     * Create the engine with a default set of properties.
     * 
     * @param properties
     *            the default properties to use.
     */
    public Engine(Properties properties) {
        setProperties(properties);
        loadQueue();
    }

    /**
     * Saves the properties passed in. If the engine is already running it will
     * call initialize.
     * 
     * @param properties
     *            engine properties
     */
    public void setProperties(Properties properties) {
        if (properties != null) {
            this.properties.putAll(properties);
        }
        if (started) {
            initialize();
        }
    }

    /**
     * Returns the list of the properties set on the engine.
     * 
     * @param properties
     *            engine properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Returns a list of all keys known to this engine.
     * 
     * @return list of all known keys for the properties for this engine.
     */
    public Set<String> getPropertyKeys() {
        Set<String> result = new HashSet<String>();
        for (Object o : properties.keySet()) {
            result.add(o.toString());
        }
        return result;
    }

    /**
     * Return the property with the given name.
     * 
     * @param name
     *            the property to be returned.
     * @return the value of the property.
     */
    public String getProperty(String name) {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(name);
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
        for (WorkflowStep step : execution.getWorkflow().getSteps()) {
            execute(new ExecutionInfo(execution, step));
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
        for (String step : steps) {
            execute(new ExecutionInfo(execution, execution.getWorkflow().getStep(step)));
        }
    }

    /**
     * Submit the step to the engine to be executed. This will submit the step
     * to the engine and the engine will execute this step with the given
     * execution environment. Based on the engine, this could happen
     * immediately, on a remote server or at some time in the future.
     * 
     * @param executionInfo
     *            information about the execution that needs to be performed.
     *            This contains both the steps as well as the execution
     *            environment.
     */
    public void execute(ExecutionInfo executionInfo) {
        synchronized (queue) {
            queue.add(executionInfo);
            saveQueue();
        }
    }

    /**
     * Postpone the step, this could be because the executor is not ready.
     * 
     * @param executionInfo
     *            the execution to be placed at the end of the list.
     */
    protected void postponeExecution(ExecutionInfo executionInfo) {
        synchronized (queue) {
            queue.remove(executionInfo);
            queue.add(executionInfo);
            saveQueue();
        }
    }

    /**
     * Checks to see if a step is in the queue. The queue is a list of all steps
     * both executing and waiting to be exectued.
     * 
     * @param step
     *            the step to be checked to see if it is in the queue.
     * @return true if the step is in the queue, false otherwise.
     */
    public boolean isInQueue(String step) {
        synchronized (queue) {
            for (ExecutionInfo ei : queue) {
                if (ei.getStepId().equals(step)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return a list of all steps the engine knows about. This includes both
     * steps that are currently executing and waiting to be executed. This list
     * is ordered, meaning the top items is the list are either executing, or
     * will be first to be executed.
     * 
     * @return ordered list of all steps executing or waiting to be executed.
     */
    public List<ExecutionInfo> getQueue() {
        synchronized (queue) {
            return new ArrayList<ExecutionInfo>(queue);
        }
    }

    /**
     * Checks to see if a step is in running. This will allow to check to see if
     * the step is currently executing.
     * 
     * @param step
     *            the step to be checked to see if it is executing..
     * @return true if the step is executing, false otherwise.
     */
    public boolean isRunning(String step) {
        synchronized (queue) {
            for (ExecutionInfo ei : queue) {
                if (ei.getStepId().equals(step)) {
                    return getStepState(ei) == Execution.State.RUNNING;
                }
            }
        }
        return false;
    }

    /**
     * Returns a list of all steps that are in the queue, and are currently
     * being executed.
     * 
     * @return list of all steps executing.
     */
    public List<ExecutionInfo> getRunning() {
        List<ExecutionInfo> running = new ArrayList<Engine.ExecutionInfo>();

        synchronized (queue) {
            for (ExecutionInfo ei : queue) {
                if (getStepState(ei) == Execution.State.RUNNING) {
                    running.add(ei);
                }
            }
        }
        return running;
    }

    /**
     * Returns a list of all steps that are in the queue, and are currently
     * waiting.
     * 
     * @return list of all steps executing.
     */
    public List<ExecutionInfo> getWaiting() {
        List<ExecutionInfo> waiting = new ArrayList<Engine.ExecutionInfo>();

        synchronized (queue) {
            for (ExecutionInfo ei : queue) {
                if (getStepState(ei) == Execution.State.WAITING) {
                    waiting.add(ei);
                }
            }
        }
        return waiting;
    }

    /**
     * Stops a particular step from being executed, and removes it from the
     * list. If the step is already executing attempts will be made to stop the
     * step from executing.
     * 
     * @param steps
     *            the list of steps to be stopped.
     */
    public void stop(String... steps) {
        for (String step : steps) {
            synchronized (queue) {
                for (ExecutionInfo ei : queue) {
                    if (ei.getStepId().equals(step)) {
                        stopExecution(ei);
                    }
                }
                saveQueue();
            }
        }
    }

    /**
     * Stop all steps from executing. This will remove all non executed steps
     * from the queue, and attempt to stop all steps currently executing.
     */
    public void stopAll() {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                stopExecution(queue.get(0));
            }
            saveQueue();
        }
    }

    private void stopExecution(ExecutionInfo executionInfo) {
        if (getStepState(executionInfo) == State.RUNNING) {
            try {
                kill(executionInfo);
            } catch (Exception e) {
                logger.error("Could not kill step.", e);
            }
        }
        try {
            Transaction transaction = SpringData.getTransaction();
            transaction.start();
            stepAborted(executionInfo);
            transaction.commit();
        } catch (Exception e) {
            logger.error("Could not mark step as aborted.", e);
        }
    }

    /**
     * Return the state of a specific step for this execution. This will load
     * the bean and return the state of a specific step in this execution.
     * 
     * @param executionInfo
     *            the execution and step whose state to return.
     * @return the sate of the step in this execution.
     */
    protected State getStepState(ExecutionInfo executionInfo) {
        State state = State.UNKNOWN;

        try {
            Transaction transaction = SpringData.getTransaction();
            transaction.start(true);
            state = executionInfo.getExecutionBean().getStepState(executionInfo.getStepId());
            transaction.commit();

            if (state == null) {
                state = State.WAITING;
            }
        } catch (Exception e) {
            logger.error("Could not get step state.", e);
        }

        return state;
    }

    /**
     * Marks the specific step in the execution as running.
     * 
     * @param executionInfo
     *            the execution and step whose state to set.
     */
    protected void stepRunning(ExecutionInfo executionInfo) {
        setStepState(executionInfo, State.RUNNING);
    }

    /**
     * Marks the specific step in the execution as done.
     * 
     * @param executionInfo
     *            the execution and step whose state to set.
     */
    protected void stepFinished(ExecutionInfo executionInfo) {
        setStepState(executionInfo, State.FINISHED);
    }

    /**
     * Marks the specific step in the execution as aborted.
     * 
     * @param executionInfo
     *            the execution and step whose state to set.
     */
    protected void stepAborted(ExecutionInfo executionInfo) {
        setStepState(executionInfo, State.ABORTED);
    }

    /**
     * Marks the specific step in the execution as failed.
     * 
     * @param executionInfo
     *            the execution and step whose state to set.
     */
    protected void stepFailed(ExecutionInfo executionInfo) {
        setStepState(executionInfo, State.FAILED);
    }

    /**
     * Sets the state of the step in the execution and save the execution
     * information.
     * 
     * @param executionInfo
     *            the execution and step whose state to set.
     * @param state
     *            the sate of the step in this execution.
     * @param remove
     *            remove the step from the queue and mark all missing outputs.
     */
    protected void setStepState(ExecutionInfo executionInfo, State state) {
        Execution execution = executionInfo.getExecutionBean();
        WorkflowStep step = executionInfo.getStepBean();

        // are we done
        // done == FINISHED, ABORTED, CANCELLED, FAILED
        // !done == WAITING, QUEUED, RUNNING
        boolean done = (state == State.FINISHED) || (state == State.ABORTED) || (state == State.FAILED);

        // mark all missing outputs
        if (done) {
            for (String id : step.getOutputs().keySet()) {
                if (!execution.hasDataset(id)) {
                    execution.setDataset(id, null);
                }
            }
        }

        // add time stamps
        if (state == State.QUEUED) {
            execution.setStepQueued(step.getId());
        } else if (state == State.RUNNING) {
            execution.setStepStart(step.getId());
        } else if (done) {
            execution.setStepEnd(step.getId());
        }

        // set step state
        executionInfo.getExecutionBean().setStepState(step.getId(), state);
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // remove step
        if (done) {
            synchronized (queue) {
                queue.remove(executionInfo);
                saveQueue();
            }
        }

        // fire an event to inform everybody of the new state
        StepStateChangedEvent event = new StepStateChangedEvent(step, execution, state);
        SpringData.getEventBus().fireEvent(event);
    }

    /**
     * Tells the actual engine to stop the process from running. Some executors
     * might not be able to stop a step once it has started and can throw an
     * exception saying so.
     * 
     * @param executionInfo
     *            information about the specific step that needs to be stopped.
     * @throws Exception
     *             throws an exception if the step could not be stopped.
     */
    protected abstract void kill(ExecutionInfo executionInfo) throws Exception;

    /**
     * This will mark the engine as started.
     */
    public void startEngine() {
        started = true;
        initialize();
    }

    /**
     * This will stop all steps first, then will shutdown the engine.
     */
    public void stopEngine() {
        started = false;
        stopAll();
    }

    /**
     * Return true if the engine is started.
     * 
     * @return true if the engine has been started.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Called when then engine is started, or when the properties is set. The
     * default implementation does nothing.
     */
    protected abstract void initialize();

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

    /**
     * Class to capture information about steps known to the engine.
     * TODO RK : this should become a bean so it can be saved/loaded
     */
    static public class ExecutionInfo {
        private final String execution;
        private final String step;

        public ExecutionInfo(Execution execution, WorkflowStep step) {
            this.execution = execution.getId();
            this.step = step.getId();
        }

        public String getStepId() {
            return step;
        }

        public WorkflowStep getStepBean() {
            return SpringData.getBean(WorkflowStepDAO.class).findOne(step);
        }

        public String getExecutionId() {
            return execution;
        }

        public Execution getExecutionBean() {
            return SpringData.getBean(ExecutionDAO.class).findOne(execution);
        }
    }
}
