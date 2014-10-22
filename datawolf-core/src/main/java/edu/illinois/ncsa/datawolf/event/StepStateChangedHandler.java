/**
 * 
 */
package edu.illinois.ncsa.datawolf.event;

import edu.illinois.ncsa.domain.event.EventHandler;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface StepStateChangedHandler extends EventHandler {
    public void stepStateChange(StepStateChangedEvent event);

}
