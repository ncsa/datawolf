/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ThreadedEngineTest extends AbstractEngineTest {
    @Override
    public Engine getEngineImplementation() {
        ThreadedEngine engine = new ThreadedEngine();
        engine.setWorkers(4);
        return engine;
    }

    @Test
    public void testWorkers() {
        ThreadedEngine engine = (ThreadedEngine) this.engine;
        engine.setWorkers(1);

        assertFalse(engine.isStarted());
        assertEquals(1, engine.getWorkers());

        engine.setWorkers(2);
        assertEquals(2, engine.getWorkers());

        engine.startEngine();
        assertTrue(engine.isStarted());

        assertEquals(2, engine.getWorkers());

        engine.setWorkers(1);
        assertEquals(1, engine.getWorkers());

        engine.setWorkers(3);
        assertEquals(3, engine.getWorkers());
    }
}
