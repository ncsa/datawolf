package edu.illinois.ncsa.cyberintegrator.springdata;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.springdata.SpringData;

public class WorkflowToolDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        SpringData.loadXMLContext("applicationContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowToolDAO dao = SpringData.getDAO(WorkflowToolDAO.class);

        WorkflowTool tool = new WorkflowTool();
        dao.save(tool);

        WorkflowTool tool1 = dao.findOne(tool.getId());
        assert (tool.getId().equals(tool1.getId()));
    }

}
