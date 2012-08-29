/**
 * 
 */
package edu.illinois.ncsa.springdata;

import org.hibernate.LazyInitializationException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class SpringDataTest {

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Test
    public void testCloneObject() throws Exception {
        // create a bean and save with an array
        Dataset dataset = new Dataset();
        dataset.addContributor(Person.createPerson("Rob", "Kooper", "kooper@illinois.edu"));
        dataset.addContributor(Person.createPerson("Luigi", "Marini", "lmarini@illinois.edu"));
        dataset.addContributor(Person.createPerson("Jong", "Lee", "jonglee1@illinois.edu"));
        dataset.addContributor(Person.createPerson("Chris", "Navarro", "cmnavarro@illinois.edu"));
        SpringData.getBean(DatasetDAO.class).save(dataset);

        // make sure we get an exception when accessing list outside of
        // transaction.
        try {
            Transaction t = SpringData.getTransaction();
            t.start();
            Dataset fetched = SpringData.getBean(DatasetDAO.class).findOne(dataset.getId());
            t.commit();

            System.out.println(fetched.getContributors().getClass());
            fetched.getContributors().size();
            throw (new Exception("There should have been a LazyInitializationException"));
        } catch (LazyInitializationException e) {}

        // clone object and there should be no more exception.
        Transaction t = SpringData.getTransaction();
        t.start();
        Dataset fetched = SpringData.cloneObject(SpringData.getBean(DatasetDAO.class).findOne(dataset.getId()));
        t.commit();
        System.out.println(fetched.getContributors().getClass());
        Assert.assertEquals(4, fetched.getContributors().size());
    }
}
