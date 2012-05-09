/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.event;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.domain.event.Event;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class StepStateChangedEvent implements Event<StepStateChangedHandler> {
    public static Type<StepStateChangedHandler> TYPE = new Type<StepStateChangedHandler>();

    private final WorkflowStep                  step;
    private final Execution                     execution;
    private final Execution.State               newState;

    public StepStateChangedEvent(WorkflowStep step, Execution execution, Execution.State newState) {
        this.step = step;
        this.execution = execution;
        this.newState = newState;
    }

    /**
     * @return the step
     */
    public WorkflowStep getStep() {
        return step;
    }

    /**
     * @return the execution
     */
    public Execution getExecution() {
        return execution;
    }

    /**
     * @return the newState
     */
    public Execution.State getNewState() {
        return newState;
    }

    @Override
    public Type<StepStateChangedHandler> getEventType() {
        return TYPE;
    }

    @Override
    public void dispatch(StepStateChangedHandler handler) {
        handler.stepStateChange(this);
    }
}
