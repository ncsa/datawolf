package edu.illinois.ncsa.cyberintegrator.springdata;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * 
 * @author "Chris Navarro <cmnavarr@illinois.edu>"
 * 
 */
public class ExecutionDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        SpringData.loadXMLContext("testContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        ExecutionDAO dao = SpringData.getBean(ExecutionDAO.class);
        Execution execution = new Execution();
        dao.save(execution);

        Execution execution1 = dao.findOne(execution.getId());
        assert (execution.getId().equals(execution1.getId()));
    }

}
