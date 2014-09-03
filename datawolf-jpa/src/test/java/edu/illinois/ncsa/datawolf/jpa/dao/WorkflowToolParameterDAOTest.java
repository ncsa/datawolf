package edu.illinois.ncsa.datawolf.jpa.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolParameterDao;

public class WorkflowToolParameterDAOTest {

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
        WorkflowToolParameterDao dao = injector.getInstance(WorkflowToolParameterDao.class);

        WorkflowToolParameter toolParameter = new WorkflowToolParameter();

        dao.save(toolParameter);

        WorkflowToolParameter toolParameter1 = dao.findOne(toolParameter.getId());
        assert (toolParameter.getId().equals(toolParameter1.getId()));
    }

}
