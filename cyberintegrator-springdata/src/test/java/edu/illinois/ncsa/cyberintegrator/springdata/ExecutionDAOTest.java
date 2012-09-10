package edu.illinois.ncsa.cyberintegrator.springdata;

import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * 
 * @author "Chris Navarro <cmnavarr@illinois.edu>"
 * 
 */
public class ExecutionDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        ExecutionDAO dao = SpringData.getBean(ExecutionDAO.class);
        Execution execution = new Execution();
        dao.save(execution);

        Execution execution1 = dao.findOne(execution.getId());
        assert (execution.getId().equals(execution1.getId()));
    }

    @Test
    public void testNullDataset() throws Exception {
        ExecutionDAO dao = SpringData.getBean(ExecutionDAO.class);

        String uuid = UUID.randomUUID().toString();

        Execution execution = new Execution();
        execution.setDataset("1", null);
        execution.setDataset("2", Execution.EMPTY_DATASET);
        execution.setDataset("3", uuid);
        dao.save(execution);

        Transaction t = SpringData.getTransaction();
        t.start();
        Execution execution2 = dao.findOne(execution.getId());

        // This is the issue we work around by using EMPTY_DATASET
        Assert.assertFalse("check key is not in map", execution2.hasDataset("1"));
        Assert.assertEquals("check value is correct", null, execution2.getDataset("1"));

        // This is the value for the dataset if no dataset will ever exist
        Assert.assertTrue("check key is in map", execution2.hasDataset("2"));
        Assert.assertEquals("check value is correct", Execution.EMPTY_DATASET, execution2.getDataset("2"));

        // This is the value for the dataset if everything goes well.
        Assert.assertTrue("check key is in map", execution2.hasDataset("3"));
        Assert.assertEquals("check value is correct", uuid, execution2.getDataset("3"));

        t.commit();
    }

}
