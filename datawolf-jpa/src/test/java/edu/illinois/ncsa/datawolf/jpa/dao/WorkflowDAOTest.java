package edu.illinois.ncsa.datawolf.jpa.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.domain.Person;

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
        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");
        WorkflowDao dao = injector.getInstance(WorkflowDao.class);

        Workflow workflow = new Workflow();
        workflow.setCreator(person);
        WorkflowStep step = new WorkflowStep();
        workflow.addStep(step);
        dao.save(workflow);

        UnitOfWork work = injector.getInstance(UnitOfWork.class);
        workflow = dao.findOne(workflow.getId());
        workflow.setTitle("test");

        dao.save(workflow);

        work.begin();
        Workflow workflow1 = dao.findOne(workflow.getId());
        assert (workflow.getId().equals(workflow1.getId()));
        work.end();
    }
}
