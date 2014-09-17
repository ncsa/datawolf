package edu.illinois.ncsa.medici.dao;

import java.io.InputStream;
import java.util.List;

import org.junit.BeforeClass;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.medici.MediciDevModule;

public class DatasetMediciDAOTest {

    private static Injector injector;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new MediciDevModule());

        // Initialize persistence service
        // PersistService service = injector.getInstance(PersistService.class);
        // service.start();

        // Persistence.setInjector(injector);
    }

    // @Test
    public void testFindOne() throws Exception {
        String id = "5418504accf2d3df16a840f5";
        DatasetDao datasetDao = injector.getInstance(DatasetDao.class);
        Dataset d = datasetDao.findOne(id);
        System.out.println(d.getTitle());
    }

    // @Test
    public void testFindAll() throws Exception {
        DatasetDao datasetDao = injector.getInstance(DatasetDao.class);
        List<Dataset> results = datasetDao.findAll();

        FileStorage fs = injector.getInstance(FileStorage.class);
        System.out.println(results.size());

        for (Dataset d : results) {
            InputStream is = fs.readFile(d.getFileDescriptors().get(0));
            byte[] buf = new byte[is.available()];
            is.read(buf);
            is.close();

            System.out.println(new String(buf, "UTF-8").trim());
        }
        // fs.readFile(fd);
    }

    // @Test
    public void createAndStore() throws Exception {
        DatasetDao datasetDao = injector.getInstance(DatasetDao.class);

        Dataset dataset = new Dataset();
        dataset.setTitle("test-dataset-2");
        dataset.setDescription("my test dataset");

        FileDescriptor fd = new FileDescriptor();
        // fd.setId("5417496eccf2d3df16a83dae");
        fd.setDataURL("http://localhost:9000/api/files/541846afccf2d3df16a8402c");
        dataset.addFileDescriptor(fd);

        dataset = datasetDao.save(dataset);

        System.out.println("dataset id after = " + dataset.getId());
    }
}
