package edu.illinois.ncsa.datawolf.jpa.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDao;

public class WorkflowToolDAOTest {
    private String          id;

    private static Injector injector;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new TestModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();
    }

    @Before
    public void createTool() {
        WorkflowToolDao dao = injector.getInstance(WorkflowToolDao.class);

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
        WorkflowToolDao dao = injector.getInstance(WorkflowToolDao.class);
        WorkflowTool tool1 = dao.findOne(id);
        assert (id.equals(tool1.getId()));
    }

    @Test
    public void testCheckInputs() throws Exception {
        WorkflowToolDao dao = injector.getInstance(WorkflowToolDao.class);
        WorkflowTool tool1 = dao.findOne(id);
        for (WorkflowToolData data : tool1.getInputs()) {
            System.out.println(data.getId() + " " + data.getTitle());
        }
        assertEquals(4, tool1.getInputs().size());
    }
}
