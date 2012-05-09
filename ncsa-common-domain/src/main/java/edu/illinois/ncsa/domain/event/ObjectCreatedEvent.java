/**
 * 
 */
package edu.illinois.ncsa.domain.event;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ObjectCreatedEvent implements Event<ObjectCreatedHandler> {
    public static final Type<ObjectCreatedHandler> TYPE         = new Type<ObjectCreatedHandler>();

    private AbstractBean                           selectedBean = null;

    public ObjectCreatedEvent(AbstractBean selectedBean) {
        this.selectedBean = selectedBean;
    }

    /**
     * @return the selectedBean
     */
    public AbstractBean getSelectedBean() {
        return selectedBean;
    }

    @Override
    public Type<ObjectCreatedHandler> getEventType() {
        return TYPE;
    }

    @Override
    public void dispatch(ObjectCreatedHandler handler) {
        if ((handler.getBeanType() == null) || handler.getBeanType().isAssignableFrom(selectedBean.getClass())) {
            handler.objectCreated(this);
        }
    }
}
