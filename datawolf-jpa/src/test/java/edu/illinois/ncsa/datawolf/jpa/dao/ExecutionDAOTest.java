package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;

import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;

/**
 * 
 * @author "Chris Navarro <cmnavarr@illinois.edu>"
 * 
 */
@Transactional
public class ExecutionDAOTest {
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
        ExecutionDao dao = injector.getInstance(ExecutionDao.class);
        Execution execution = new Execution();
        dao.save(execution);

        Execution execution1 = dao.findOne(execution.getId());
        assert (execution.getId().equals(execution1.getId()));
    }

    @Test
    public void testNullDataset() throws Exception {
        ExecutionDao dao = injector.getInstance(ExecutionDao.class);

        String uuid = UUID.randomUUID().toString();

        Execution execution = new Execution();
        execution.setDataset("1", null);
        execution.setDataset("2", Execution.EMPTY_DATASET);
        execution.setDataset("3", uuid);
        dao.save(execution);

        // TODO fix this unit test, it was testing transaction.commit()
        UnitOfWork work = injector.getInstance(UnitOfWork.class);
        work.begin();
        Execution execution2 = dao.findOne(execution.getId());

        // This is the issue we work around by using EMPTY_DATASET
        // Assert.assertFalse("check key is not in map",
// execution2.hasDataset("1"));
        Assert.assertEquals("check value is correct", null, execution2.getDataset("1"));

        // This is the value for the dataset if no dataset will ever exist
        Assert.assertTrue("check key is in map", execution2.hasDataset("2"));
        Assert.assertEquals("check value is correct", Execution.EMPTY_DATASET, execution2.getDataset("2"));

        // This is the value for the dataset if everything goes well.
        Assert.assertTrue("check key is in map", execution2.hasDataset("3"));
        Assert.assertEquals("check value is correct", uuid, execution2.getDataset("3"));
        work.end();
    }

}
