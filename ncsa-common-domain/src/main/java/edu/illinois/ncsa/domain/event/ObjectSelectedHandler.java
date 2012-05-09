/**
 * 
 */
package edu.illinois.ncsa.domain.event;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public abstract class ObjectSelectedHandler implements EventHandler {
    public Class<? extends AbstractBean> getBeanType() {
        return null;
    }

    public abstract void objectSelected(ObjectSelectedEvent event);
}
