package edu.illinois.ncsa.cyberintegrator.springdata;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * 
 * @author "Chris Navarro <cmnavarr@illinois.edu>"
 * 
 */
public class WorkflowDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        SpringData.loadXMLContext("testContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowDAO dao = SpringData.getBean(WorkflowDAO.class);

        Workflow workflow = new Workflow();
        dao.save(workflow);

        Workflow workflow1 = dao.findOne(workflow.getId());
        assert (workflow.getId().equals(workflow1.getId()));
    }

}
