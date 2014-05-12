package edu.illinois.ncsa.datawolf.springdata;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

@Transactional
public class WorkflowToolDAOTest {
    private String id;

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Before
    public void createTool() {
        WorkflowToolDAO dao = SpringData.getBean(WorkflowToolDAO.class);

        WorkflowTool tool = new WorkflowTool();
        tool.addInput(new WorkflowToolData());
        tool.addInput(new WorkflowToolData());
        tool.addInput(new WorkflowToolData());
        tool.addInput(new WorkflowToolData());
        dao.save(tool);

        id = tool.getId();
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowToolDAO dao = SpringData.getBean(WorkflowToolDAO.class);
        WorkflowTool tool1 = dao.findOne(id);
        assert (id.equals(tool1.getId()));
    }

    @Test
    public void testCheckInputs() throws Exception {
        WorkflowToolDAO dao = SpringData.getBean(WorkflowToolDAO.class);
        Transaction t = SpringData.getTransaction();
        t.start();
        WorkflowTool tool1 = dao.findOne(id);
        for (WorkflowToolData data : tool1.getInputs()) {
            System.out.println(data.getId() + " " + data.getTitle());
        }
        assertEquals(4, tool1.getInputs().size());
        t.commit();
    }
}
