package edu.illinois.ncsa.cyberintegrator.springdata;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.springdata.SpringData;

public class WorkflowStepDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        SpringData.loadXMLContext("applicationContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowStepDAO dao = SpringData.getDAO(WorkflowStepDAO.class);

        WorkflowStep step = new WorkflowStep();
        dao.save(step);

        WorkflowStep step1 = dao.findOne(step.getId());

        assert (step.getId().equals(step1.getId()));
    }
}
