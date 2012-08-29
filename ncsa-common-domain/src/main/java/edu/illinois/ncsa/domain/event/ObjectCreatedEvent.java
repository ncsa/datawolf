/**
 * 
 */
package edu.illinois.ncsa.domain.event;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ObjectCreatedEvent implements Event<ObjectCreatedHandler> {
    public static final Type<ObjectCreatedHandler> TYPE          = new Type<ObjectCreatedHandler>();

    private List<AbstractBean>                     selectedBeans = null;

    public ObjectCreatedEvent(AbstractBean selectedBean) {
        this.selectedBeans = new ArrayList<AbstractBean>();
        this.selectedBeans.add(selectedBean);
    }

    public ObjectCreatedEvent(List<AbstractBean> selectedBeans) {
        this.selectedBeans = selectedBeans;
    }

    /**
     * @return the selectedBean
     */
    public List<AbstractBean> getSelectedBeans() {
        return selectedBeans;
    }

    @Override
    public Type<ObjectCreatedHandler> getEventType() {
        return TYPE;
    }

    @Override
    public void dispatch(ObjectCreatedHandler handler) {
        if ((handler.getBeanType() == null) || handler.getBeanType().isAssignableFrom(selectedBeans.get(0).getClass())) {
            handler.objectCreated(this);
        }
    }
}
