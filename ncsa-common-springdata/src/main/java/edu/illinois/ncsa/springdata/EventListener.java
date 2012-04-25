/**
 * 
 */
package edu.illinois.ncsa.springdata;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface EventListener {
    void createObject(AbstractBean object);

    void deleteObject(AbstractBean object);

    void updateObject(AbstractBean object);
}
