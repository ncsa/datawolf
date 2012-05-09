/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.domain.event;

import edu.illinois.ncsa.domain.event.EventHandler;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface WorkflowToolCreatedHandler extends EventHandler {
    public void toolCreated(WorkflowToolCreatedEvent event);

}
