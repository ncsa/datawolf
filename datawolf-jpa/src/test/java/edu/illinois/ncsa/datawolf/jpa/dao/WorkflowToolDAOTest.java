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
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

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

    @Test
    public void createToolWithBlob() throws Exception {
        // This tests a bug associated with WOLF-123
        WorkflowToolDao tooldao = injector.getInstance(WorkflowToolDao.class);
        FileDescriptorDao dao = injector.getInstance(FileDescriptorDao.class);
        FileDescriptor fd = new FileDescriptor();
        dao.save(fd);

        WorkflowTool wt0 = new WorkflowTool();
        wt0.setTitle("tool-0");
        wt0.addBlob(fd);
        tooldao.save(wt0);

        FileDescriptor fd1 = dao.findOne(fd.getId());

        WorkflowTool wt1 = new WorkflowTool();
        wt0.setTitle("tool-1");
        wt1.addBlob(fd1);
        tooldao.save(wt1);
    }
}
