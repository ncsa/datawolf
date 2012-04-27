package edu.illinois.ncsa.cyberintegrator.springdata;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.springdata.SpringData;

public class WorkflowToolParameterDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowToolParameterDAO dao = SpringData.getBean(WorkflowToolParameterDAO.class);

        WorkflowToolParameter toolParameter = new WorkflowToolParameter();
        dao.save(toolParameter);

        WorkflowToolParameter toolParameter1 = dao.findOne(toolParameter.getId());
        assert (toolParameter.getId().equals(toolParameter1.getId()));
    }

}
