/**
 * 
 */
package edu.illinois.ncsa.springdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.event.Event;
import edu.illinois.ncsa.domain.event.EventHandler;
import edu.illinois.ncsa.domain.event.Event.Type;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class EventBus {
    private static Logger                              logger     = LoggerFactory.getLogger(EventBus.class);

    private Map<Type<?>, List<? extends EventHandler>> handlerMap = new HashMap<Type<?>, List<? extends EventHandler>>();

    public <H extends EventHandler> void addHandler(Type<H> type, H handler) {
        List<H> handlers = getHandlers(type);
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    public <H extends EventHandler> void removeHandler(Type<H> type, H handler) {
        List<H> handlers = getHandlers(type);
        if (handlers.contains(handler)) {
            handlers.remove(handler);
        }
    }

    public <H extends EventHandler> void fireEvent(Event<H> event) {
        for (H handler : getHandlers(event.getEventType())) {
            try {
                event.dispatch(handler);
            } catch (Throwable thr) {
                logger.error("Uncaught exception in handler.", thr);
            }
        }
    }

    private <H extends EventHandler> List<H> getHandlers(Type<H> type) {
        @SuppressWarnings("unchecked")
        List<H> list = (List<H>) handlerMap.get(type);
        if (list == null) {
            list = new ArrayList<H>();
            handlerMap.put(type, list);
        }
        return list;
    }
}
