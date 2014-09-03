package edu.illinois.ncsa.datawolf.jpa.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDataDao;

public class WorkflowToolDataDAOTest {

    private static Injector injector;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new TestModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowToolDataDao dao = injector.getInstance(WorkflowToolDataDao.class);

        WorkflowToolData toolData = new WorkflowToolData();
        dao.save(toolData);

        WorkflowToolData toolData1 = dao.findOne(toolData.getId());
        assert (toolData.getId().equals(toolData1.getId()));
    }
}
