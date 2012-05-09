/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.domain.event;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.domain.event.Event;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class WorkflowToolCreatedEvent implements Event<WorkflowToolCreatedHandler> {
    public static Type<WorkflowToolCreatedHandler> TYPE = new Type<WorkflowToolCreatedHandler>();

    private final WorkflowTool                     tool;

    public WorkflowToolCreatedEvent(WorkflowTool tool) {
        this.tool = tool;
    }

    /**
     * @return the tool
     */
    public WorkflowTool getTool() {
        return tool;
    }

    @Override
    public Type<WorkflowToolCreatedHandler> getEventType() {
        return TYPE;
    }

    @Override
    public void dispatch(WorkflowToolCreatedHandler handler) {
        handler.toolCreated(this);
    }
}
