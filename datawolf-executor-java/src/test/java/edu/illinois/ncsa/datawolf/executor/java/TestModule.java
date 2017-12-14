package edu.illinois.ncsa.datawolf.executor.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;

import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.datawolf.domain.dao.LogFileDao;
import edu.illinois.ncsa.datawolf.domain.dao.SubmissionDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDataDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolParameterDao;
import edu.illinois.ncsa.datawolf.jpa.dao.ExecutionJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.LogFileJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.SubmissionJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowStepJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowToolDataJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowToolJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowToolParameterJPADao;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.domain.impl.FileStorageDisk;
import edu.illinois.ncsa.jpa.dao.DatasetJPADao;
import edu.illinois.ncsa.jpa.dao.FileDescriptorJPADao;
import edu.illinois.ncsa.jpa.dao.PersonJPADao;

public class TestModule extends AbstractModule {
    private Logger logger = LoggerFactory.getLogger(TestModule.class);

    @Override
    protected void configure() {
        JpaPersistModule jpa = new JpaPersistModule("WolfPersistence");

        Properties properties = new Properties();
        String datawolfProperties = "src/test/resources/datawolf.properties";
        if (datawolfProperties.trim() != "") {
            File file = new File(datawolfProperties);
            try {
                properties.load(new FileInputStream(file));
                properties.setProperty("disk.folder", System.getProperty("java.io.tmpdir")); //$NON-NLS-1$//$NON-NLS-2$
                properties.setProperty("disk.levels", "2"); //$NON-NLS-1$ //$NON-NLS-2$
                properties.setProperty("executor.cleanup", "true");
                Names.bindProperties(binder(), properties);
                jpa.properties(properties);
            } catch (IOException e) {
                logger.error("Error reading properties file", e);
            }
        }
        install(jpa);

        // DAOs
        bind(PersonDao.class).to(PersonJPADao.class);
        bind(SubmissionDao.class).to(SubmissionJPADao.class);
        bind(WorkflowDao.class).to(WorkflowJPADao.class);
        bind(WorkflowStepDao.class).to(WorkflowStepJPADao.class);
        bind(WorkflowToolDao.class).to(WorkflowToolJPADao.class);
        bind(WorkflowToolParameterDao.class).to(WorkflowToolParameterJPADao.class);
        bind(WorkflowToolDataDao.class).to(WorkflowToolDataJPADao.class);
        bind(DatasetDao.class).to(DatasetJPADao.class);
        bind(ExecutionDao.class).to(ExecutionJPADao.class);
        bind(FileDescriptorDao.class).to(FileDescriptorJPADao.class);
        bind(FileStorage.class).to(FileStorageDisk.class);
        bind(LogFileDao.class).to(LogFileJPADao.class);

        bind(JavaExecutor.class);
    }
}
