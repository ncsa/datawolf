package edu.illinois.ncsa.cyberintegrator.springdata;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.springdata.SpringData;

public class WorkflowToolDataDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        SpringData.loadXMLContext("applicationContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowToolDataDAO dao = SpringData.getDAO(WorkflowToolDataDAO.class);

        WorkflowToolData toolData = new WorkflowToolData();
        dao.save(toolData);

        WorkflowToolData toolData1 = dao.findOne(toolData.getId());
        assert (toolData.getId().equals(toolData1.getId()));
    }
}
