package edu.illinois.ncsa.cyberintegrator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;

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

    @Autowired
    protected ExecutionDAO        executionDAO;

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
            execute(new ExecutionInfo(execution, step.getUri()));
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
            execute(new ExecutionInfo(execution, step));
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
                if (ei.getStep().equals(step)) {
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
                if (ei.getStep().equals(step)) {
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
                    if (ei.getStep().equals(step)) {
                        stopExecution(ei);
                    }
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
            while (!queue.isEmpty()) {
                stopExecution(queue.get(0));
            }
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
        stepAborted(executionInfo);
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
        return executionDAO.findOne(executionInfo.getExecution()).getStepState(executionInfo.getStep());
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
        synchronized (queue) {
            queue.remove(executionInfo);
            saveQueue();
        }
    }

    /**
     * Marks the specific step in the execution as aborted.
     * 
     * @param executionInfo
     *            the execution and step whose state to set.
     */
    protected void stepAborted(ExecutionInfo executionInfo) {
        setStepState(executionInfo, State.ABORTED);
        synchronized (queue) {
            queue.remove(executionInfo);
            saveQueue();
        }
    }

    /**
     * Marks the specific step in the execution as failed.
     * 
     * @param executionInfo
     *            the execution and step whose state to set.
     */
    protected void stepFailed(ExecutionInfo executionInfo) {
        setStepState(executionInfo, State.FAILED);
        synchronized (queue) {
            queue.remove(executionInfo);
            saveQueue();
        }
    }

    /**
     * Sets the state of the step in the execution and save the execution
     * information.
     * 
     * @param executionInfo
     *            the execution and step whose state to set.
     * @param state
     *            the sate of the step in this execution.
     */
    protected void setStepState(ExecutionInfo executionInfo, State state) {
        Execution exec = executionDAO.findOne(executionInfo.getExecution());
        exec.setStepState(executionInfo.getStep(), state);
        executionDAO.save(exec);
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
        initialize();
        started = true;
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
        // TODO implement
    }

    /**
     * Load the queue from the underlying persistence layer.
     */
    private void loadQueue() {
        // TODO implement
    }

    /**
     * Class to capture information about steps known to the engine.
     * TODO this should become a bean so it can be saved/loaded
     */
    static public class ExecutionInfo {
        private final String execution;
        private final String step;

        public ExecutionInfo(Execution execution, String step) {
            this.execution = execution.getId();
            this.step = step;
        }

        public String getStep() {
            return step;
        }

        public String getExecution() {
            return execution;
        }
    }
}