/**
 * 
 */
package edu.illinois.ncsa.datawolf.domain.event;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.domain.event.Event;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class WorkflowToolDeletedEvent implements Event<WorkflowToolDeletedHandler> {
    public static Type<WorkflowToolDeletedHandler> TYPE = new Type<WorkflowToolDeletedHandler>();

    private final WorkflowTool                     tool;

    public WorkflowToolDeletedEvent(WorkflowTool tool) {
        this.tool = tool;
    }

    /**
     * @return the tool
     */
    public WorkflowTool getTool() {
        return tool;
    }

    @Override
    public Type<WorkflowToolDeletedHandler> getEventType() {
        return TYPE;
    }

    @Override
    public void dispatch(WorkflowToolDeletedHandler handler) {
        handler.toolDeleted(this);
    }
}
