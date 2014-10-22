package edu.illinois.ncsa.datawolf.jpa.dao;

import java.io.ByteArrayInputStream;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.domain.LogFile;
import edu.illinois.ncsa.datawolf.domain.dao.LogFileDao;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

public class LogFileDAOTest {
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
        LogFileDao logFileDao = injector.getInstance(LogFileDao.class);
        LogFile logfile = new LogFile();
        logfile.setExecutionId("execution-id");
        logfile.setStepId("step-id");
        logfile.setDate(new Date());
        logfile.setLog(new FileDescriptor());
        logFileDao.save(logfile);

        FileStorage fileStorage = injector.getInstance(FileStorage.class);
        FileDescriptorDao fileDescriptorDao = injector.getInstance(FileDescriptorDao.class);

        String log = "text";
        ByteArrayInputStream bais = new ByteArrayInputStream(log.toString().getBytes("UTF-8"));
        FileDescriptor fd = logfile.getLog();
        fd = fileStorage.storeFile(fd.getId(), "log.txt", bais);
        System.out.println("fd id +" + fd.getId());
        fileDescriptorDao.save(fd);

        assert (logfile.getLog() != null);
    }

    // @Test
    public void findByExecutionIdAndStepId() throws Exception {
        LogFileDao logFileDao = injector.getInstance(LogFileDao.class);
        LogFile logfile = new LogFile();
        logfile.setExecutionId("execution-id");
        logfile.setStepId("step-id");
        logfile.setDate(new Date());
        logfile.setLog(new FileDescriptor());
        logFileDao.save(logfile);

        LogFile logfile1 = logFileDao.findByExecutionIdAndStepId("execution-id", "step-id");
        assert (logfile1.getId().equals(logfile.getId()));

    }
}
