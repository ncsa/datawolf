/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.event;

import edu.illinois.ncsa.domain.event.EventHandler;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface StepStateChangedHandler extends EventHandler {
    public void stepStateChange(StepStateChangedEvent event);

}
