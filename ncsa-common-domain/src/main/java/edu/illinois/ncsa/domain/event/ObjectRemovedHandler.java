/**
 * 
 */
package edu.illinois.ncsa.domain.event;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public abstract class ObjectRemovedHandler implements EventHandler {
    public Class<? extends AbstractBean> getBeanType() {
        return null;
    }

    public abstract void objectRemoved(ObjectRemovedEvent event);
}
