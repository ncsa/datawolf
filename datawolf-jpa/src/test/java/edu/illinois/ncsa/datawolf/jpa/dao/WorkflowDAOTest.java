package edu.illinois.ncsa.datawolf.jpa.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;

/**
 * 
 * @author "Chris Navarro <cmnavarr@illinois.edu>"
 * 
 */
public class WorkflowDAOTest {
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
        WorkflowDao dao = injector.getInstance(WorkflowDao.class);

        Workflow workflow = new Workflow();
        dao.save(workflow);

        Workflow workflow1 = dao.findOne(workflow.getId());
        assert (workflow.getId().equals(workflow1.getId()));
    }
}
