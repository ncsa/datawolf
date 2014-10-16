package edu.illinois.ncsa.jpa.dao;

import java.util.List;

import javax.inject.Inject;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class GuiceTest {

    private static Injector injector;

    @Inject
    private PersonDao       dao;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new TestModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();

        // Persistence.setInjector(injector);
    }

    @Test
    public void testCreateAndStore() throws Exception {
        // @Inject is not working
        PersonDao dao = injector.getInstance(PersonDao.class);

        Person p1 = new Person();
        p1.setFirstName("Chris");
        p1.setLastName("Navarro");
        p1.setEmail("cmnavarr@illinois.edu");

        Person p2 = new Person();
        p2.setFirstName("Rob");
        p2.setLastName("Kooper");
        p2.setEmail("kooper@illinois.edu");

        dao.save(p1);
        dao.save(p2);

        List<Person> person = dao.findAll();
        System.out.println("number of people = " + person.size());

        Person p = dao.findOne(p1.getId());
        System.out.println("name: " + p.getName());
    }
}
