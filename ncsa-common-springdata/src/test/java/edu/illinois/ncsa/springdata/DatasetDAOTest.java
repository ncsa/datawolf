package edu.illinois.ncsa.springdata;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.domain.Dataset;

public class DatasetDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        DatasetDAO dao = SpringData.getBean(DatasetDAO.class);

        Dataset dataset = new Dataset();
        dao.save(dataset);

        Dataset dataset1 = dao.findOne(dataset.getId());
        assert (dataset.getId().equals(dataset1.getId()));
    }
}
