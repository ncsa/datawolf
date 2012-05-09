/**
 * 
 */
package edu.illinois.ncsa.domain.event;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ObjectRemovedEvent implements Event<ObjectRemovedHandler> {
    public static final Type<ObjectRemovedHandler> TYPE         = new Type<ObjectRemovedHandler>();

    private AbstractBean                           selectedBean = null;

    public ObjectRemovedEvent(AbstractBean selectedBean) {
        this.selectedBean = selectedBean;
    }

    /**
     * @return the selectedBean
     */
    public AbstractBean getSelectedBean() {
        return selectedBean;
    }

    @Override
    public Type<ObjectRemovedHandler> getEventType() {
        return TYPE;
    }

    @Override
    public void dispatch(ObjectRemovedHandler handler) {
        if ((handler.getBeanType() == null) || handler.getBeanType().isAssignableFrom(selectedBean.getClass())) {
            handler.objectRemoved(this);
        }
    }
}
