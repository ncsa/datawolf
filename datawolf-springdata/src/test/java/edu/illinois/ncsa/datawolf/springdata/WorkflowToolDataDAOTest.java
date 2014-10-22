package edu.illinois.ncsa.datawolf.springdata;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.springdata.SpringData;

public class WorkflowToolDataDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowToolDataDAO dao = SpringData.getBean(WorkflowToolDataDAO.class);

        WorkflowToolData toolData = new WorkflowToolData();
        dao.save(toolData);

        WorkflowToolData toolData1 = dao.findOne(toolData.getId());
        assert (toolData.getId().equals(toolData1.getId()));
    }
}
