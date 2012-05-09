/**
 * 
 */
package edu.illinois.ncsa.domain.event;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ObjectSelectedEvent implements Event<ObjectSelectedHandler> {
    public static final Type<ObjectSelectedHandler> TYPE         = new Type<ObjectSelectedHandler>();

    private AbstractBean                          selectedBean = null;

    public ObjectSelectedEvent(AbstractBean selectedBean) {
        this.selectedBean = selectedBean;
    }

    /**
     * @return the selectedBean
     */
    public AbstractBean getSelectedBean() {
        return selectedBean;
    }

    @Override
    public Type<ObjectSelectedHandler> getEventType() {
        return TYPE;
    }

    @Override
    public void dispatch(ObjectSelectedHandler handler) {
        if ((handler.getBeanType() == null) || handler.getBeanType().isAssignableFrom(selectedBean.getClass())) {
            handler.objectSelected(this);
        }
    }
}
