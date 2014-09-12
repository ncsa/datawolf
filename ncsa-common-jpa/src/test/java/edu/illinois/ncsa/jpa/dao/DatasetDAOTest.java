package edu.illinois.ncsa.jpa.dao;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.jpa.JPADevModule;

public class DatasetDAOTest {
    private static Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new JPADevModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();

        // Persistence.setInjector(injector);
    }

    @After
    public void tearDown() {
        // injector.getInstance(PersistService.class).stop();
        // injector = null;
    }

    @Test
    public void testFindByCreatorEmail() throws Exception {
        DatasetDao datasetDao = injector.getInstance(DatasetDao.class);
        System.out.println(System.identityHashCode(datasetDao));

        // Person p1 = new Person();
        // p1.setFirstName("Chris");
        // p1.setLastName("Navarro");
        // p1.setEmail("cmnavarr@illinois.edu");

        Dataset dataset = new Dataset();
        // dataset.setCreator(p1);

        datasetDao.save(dataset);

        DatasetDao datasetDao1 = injector.getInstance(DatasetDao.class);
        dataset.setTitle("test");
        datasetDao1.save(dataset);
        System.out.println(dataset.getId());

        // List<Dataset> datasets =
// datasetDao.findByCreatorEmail("cmnavarr@illinois.edu");
        // assert (datasets.size() == 1);
    }

    // @Test
    public void testFindByDeleted() throws Exception {
        DatasetDao datasetDao = injector.getInstance(DatasetDao.class);

        Dataset dataset = new Dataset();
        dataset.setDeleted(true);

        datasetDao.save(dataset);

        List<Dataset> datasets = datasetDao.findByDeleted(false);
        System.out.println(datasets.size());
        assert (datasets.size() == 0);
    }

    // @Test
    public void testFindByCreatorEmailAndDeleted() throws Exception {
        DatasetDao datasetDao = injector.getInstance(DatasetDao.class);

        Person p1 = new Person();
        p1.setFirstName("Chris");
        p1.setLastName("Navarro");
        p1.setEmail("cmnavarr@illinois.edu");

        Dataset dataset = new Dataset();
        dataset.setCreator(p1);
        dataset.setDeleted(true);

        datasetDao.save(dataset);

        List<Dataset> datasets = datasetDao.findByCreatorEmailAndDeleted("cmnavarr@illinois.edu", true);
        System.out.println(datasets.size());
        assert (datasets.size() == 1);
    }
}
