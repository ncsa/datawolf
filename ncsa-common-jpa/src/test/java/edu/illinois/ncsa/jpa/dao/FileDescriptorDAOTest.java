package edu.illinois.ncsa.jpa.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.jpa.JPADevModule;

public class FileDescriptorDAOTest {
    private static Injector injector;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new JPADevModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();

        // Persistence.setInjector(injector);
    }

    @Test
    public void testCreateAndStore() throws Exception {
        FileDescriptorDao fileDescriptorDao = injector.getInstance(FileDescriptorDao.class);
        FileDescriptor fd = new FileDescriptor();

        fileDescriptorDao.save(fd);

        FileDescriptor fd1 = fileDescriptorDao.findOne(fd.getId());
        assert (fd1.getId().equals(fd.getId()));
    }
}
