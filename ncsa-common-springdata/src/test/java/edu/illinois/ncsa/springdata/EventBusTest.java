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

        eb.addListener(new EventListener() {
            @Override
            public void updateObject(AbstractBean object) {
                if (object instanceof AbstractBean) {}
            }

            @Override
            public void deleteObject(AbstractBean object) {
                if (object instanceof AbstractBean) {
                    lst.remove(object);
                }
            }

            @Override
            public void createObject(AbstractBean object) {
                if (object instanceof AbstractBean) {
                    lst.add(object);
                }
            }
        });

        eb.fireCreateObject(obj);
        assertTrue(lst.contains(obj));

        eb.fireUpdateObject(obj);
        assertTrue(lst.contains(obj));

        eb.fireDeleteObject(obj);
        assertFalse(lst.contains(obj));
    }
}
