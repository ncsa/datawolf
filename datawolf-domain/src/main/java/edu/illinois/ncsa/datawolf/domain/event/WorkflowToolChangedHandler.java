/**
 * 
 */
package edu.illinois.ncsa.datawolf.domain.event;

import edu.illinois.ncsa.domain.event.EventHandler;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface WorkflowToolChangedHandler extends EventHandler {
    public void toolChanged(WorkflowToolChangedEvent event);

}
