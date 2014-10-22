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
public class WorkflowToolChangedEvent implements Event<WorkflowToolChangedHandler> {
    public static Type<WorkflowToolChangedHandler> TYPE = new Type<WorkflowToolChangedHandler>();

    private final WorkflowTool                     tool;

    public WorkflowToolChangedEvent(WorkflowTool tool) {
        this.tool = tool;
    }

    /**
     * @return the tool
     */
    public WorkflowTool getTool() {
        return tool;
    }

    @Override
    public Type<WorkflowToolChangedHandler> getEventType() {
        return TYPE;
    }

    @Override
    public void dispatch(WorkflowToolChangedHandler handler) {
        handler.toolChanged(this);
    }
}
