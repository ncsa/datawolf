/**
 * 
 */
package edu.illinois.ncsa.domain.event;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ObjectUpdatedEvent implements Event<ObjectUpdatedHandler> {
    public static final Type<ObjectUpdatedHandler> TYPE         = new Type<ObjectUpdatedHandler>();

    private AbstractBean                           selectedBean = null;

    public ObjectUpdatedEvent(AbstractBean selectedBean) {
        this.selectedBean = selectedBean;
    }

    /**
     * @return the selectedBean
     */
    public AbstractBean getSelectedBean() {
        return selectedBean;
    }

    @Override
    public Type<ObjectUpdatedHandler> getEventType() {
        return TYPE;
    }

    @Override
    public void dispatch(ObjectUpdatedHandler handler) {
        if ((handler.getBeanType() == null) || handler.getBeanType().isAssignableFrom(selectedBean.getClass())) {
            handler.objectUpdated(this);
        }
    }
}
