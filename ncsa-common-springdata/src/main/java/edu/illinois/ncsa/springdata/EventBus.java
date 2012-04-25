/**
 * 
 */
package edu.illinois.ncsa.springdata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class EventBus {
    private static Logger                   logger    = LoggerFactory.getLogger(EventBus.class);

    private Map<Object, Set<EventListener>> listeners = new HashMap<Object, Set<EventListener>>();

    public EventBus() {}

    public void addListener(EventListener l) {
        addListener(AbstractBean.class, l);
    }

    public synchronized void addListener(Class<? extends AbstractBean> type, EventListener l) {
        Set<EventListener> listenerset = listeners.get(type);
        if (listenerset == null) {
            listenerset = new HashSet<EventListener>();
            listeners.put(type, listenerset);
        }
        if (!listenerset.contains(l)) {
            listenerset.add(l);
        }
    }

    public void removeListener(EventListener l) {
        removeListener(AbstractBean.class, l);
    }

    public synchronized void removeListener(Class<? extends AbstractBean> type, EventListener l) {
        Set<EventListener> listenerset = listeners.get(type);
        if ((listenerset != null) && listenerset.contains(l)) {
            listenerset.remove(l);
        }
    }

    public synchronized void fireCreateObject(AbstractBean bean) {
        if (listeners.containsKey(bean.getClass())) {
            for (EventListener l : listeners.get(bean.getClass())) {
                try {
                    l.createObject(bean);
                } catch (Throwable thr) {
                    logger.error("Exception escaped.", thr);
                }
            }
        }
        if (listeners.containsKey(AbstractBean.class)) {
            for (EventListener l : listeners.get(AbstractBean.class)) {
                try {
                    l.createObject(bean);
                } catch (Throwable thr) {
                    logger.error("Exception escaped.", thr);
                }
            }
        }
    }

    public synchronized void fireDeleteObject(AbstractBean bean) {
        if (listeners.containsKey(bean.getClass())) {
            for (EventListener l : listeners.get(bean.getClass())) {
                try {
                    l.deleteObject(bean);
                } catch (Throwable thr) {
                    logger.error("Exception escaped.", thr);
                }
            }
        }
        if (listeners.containsKey(AbstractBean.class)) {
            for (EventListener l : listeners.get(AbstractBean.class)) {
                try {
                    l.deleteObject(bean);
                } catch (Throwable thr) {
                    logger.error("Exception escaped.", thr);
                }
            }
        }
    }

    public synchronized void fireUpdateObject(AbstractBean bean) {
        if (listeners.containsKey(bean.getClass())) {
            for (EventListener l : listeners.get(bean.getClass())) {
                try {
                    l.updateObject(bean);
                } catch (Throwable thr) {
                    logger.error("Exception escaped.", thr);
                }
            }
        }
        if (listeners.containsKey(AbstractBean.class)) {
            for (EventListener l : listeners.get(AbstractBean.class)) {
                try {
                    l.updateObject(bean);
                } catch (Throwable thr) {
                    logger.error("Exception escaped.", thr);
                }
            }
        }
    }
}
