/**
 * 
 */
package edu.illinois.ncsa.domain.event;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface Event<H extends EventHandler> {
    public static class Type<H> {
        private static int nextType = 0;
        private int        type;

        public Type() {
            this.type = ++nextType;
        }

        @Override
        public int hashCode() {
            return type;
        }
    }

    public abstract Type<H> getEventType();

    public abstract void dispatch(H handler);
}
