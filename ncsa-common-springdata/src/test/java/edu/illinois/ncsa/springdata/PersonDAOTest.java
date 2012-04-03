package edu.illinois.ncsa.springdata;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.domain.Person;

public class PersonDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        SpringData.loadXMLContext("applicationContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        PersonDAO dao = SpringData.getDAO(PersonDAO.class);
        Person person = new Person();
        dao.save(person);

        Person person1 = dao.findOne(person.getId());
        assert (person.getId().equals(person1.getId()));
    }
}
