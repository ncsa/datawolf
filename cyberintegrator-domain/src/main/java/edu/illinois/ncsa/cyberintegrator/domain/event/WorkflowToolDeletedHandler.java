/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.domain.event;

import edu.illinois.ncsa.domain.event.EventHandler;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface WorkflowToolDeletedHandler extends EventHandler {
    public void toolDeleted(WorkflowToolDeletedEvent event);

}
