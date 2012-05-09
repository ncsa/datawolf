/**
 * 
 */
package edu.illinois.ncsa.springdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.event.Event;
import edu.illinois.ncsa.domain.event.EventHandler;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class EventBusTest {
    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    public void checkSingleton() {
        EventBus eb = SpringData.getEventBus();
        assertEquals(eb, SpringData.getEventBus());
    }

    @Test
    public void testListern() {
        AbstractBean obj = new AbstractBean();
        final List<AbstractBean> lst = new ArrayList<AbstractBean>();
        EventBus eb = SpringData.getEventBus();

        eb.addHandler(AddTestEvent.type, new AddTestEventHandler() {
            @Override
            public void beanAdded(AddTestEvent event) {
                lst.add(event.getBean());
            }
        });
        eb.addHandler(DeleteTestEvent.type, new DeleteTestEventHandler() {
            @Override
            public void beanRemoved(DeleteTestEvent event) {
                lst.remove(event.getBean());
            }
        });

        eb.fireEvent(new AddTestEvent(obj));
        assertTrue(lst.contains(obj));

        eb.fireEvent(new DeleteTestEvent(obj));
        assertFalse(lst.contains(obj));
    }

    static public interface AddTestEventHandler extends EventHandler {
        public void beanAdded(AddTestEvent event);
    }

    static public class AddTestEvent implements Event<AddTestEventHandler> {
        public static Type<AddTestEventHandler> type = new Type<AddTestEventHandler>();
        private final AbstractBean              bean;

        public AddTestEvent(AbstractBean bean) {
            this.bean = bean;
        }

        public AbstractBean getBean() {
            return bean;
        }

        @Override
        public Type<AddTestEventHandler> getEventType() {
            return type;
        }

        @Override
        public void dispatch(AddTestEventHandler handler) {
            handler.beanAdded(this);
        }
    }

    static public interface DeleteTestEventHandler extends EventHandler {
        public void beanRemoved(DeleteTestEvent event);
    }

    static public class DeleteTestEvent implements Event<DeleteTestEventHandler> {
        public static Type<DeleteTestEventHandler> type = new Type<DeleteTestEventHandler>();
        private final AbstractBean                 bean;

        public DeleteTestEvent(AbstractBean bean) {
            this.bean = bean;
        }

        public AbstractBean getBean() {
            return bean;
        }

        @Override
        public Type<DeleteTestEventHandler> getEventType() {
            return type;
        }

        @Override
        public void dispatch(DeleteTestEventHandler handler) {
            handler.beanRemoved(this);
        }
    }

}
