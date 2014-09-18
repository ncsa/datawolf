package edu.illinois.ncsa.datawolf.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;

import edu.illinois.ncsa.datawolf.Engine;
import edu.illinois.ncsa.datawolf.Executor;
import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.datawolf.domain.dao.HPCJobInfoDao;
import edu.illinois.ncsa.datawolf.domain.dao.LogFileDao;
import edu.illinois.ncsa.datawolf.domain.dao.SubmissionDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDataDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolParameterDao;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineExecutor;
import edu.illinois.ncsa.datawolf.executor.java.JavaExecutor;
import edu.illinois.ncsa.datawolf.jpa.dao.ExecutionJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.HPCJobInfoJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.LogFileJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.SubmissionJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowStepJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowToolDataJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowToolJPADao;
import edu.illinois.ncsa.datawolf.jpa.dao.WorkflowToolParameterJPADao;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.jpa.dao.AccountJPADao;
import edu.illinois.ncsa.jpa.dao.PersonJPADao;
import edu.illinois.ncsa.medici.dao.DatasetMediciDao;
import edu.illinois.ncsa.medici.dao.FileDescriptorMediciDao;
import edu.illinois.ncsa.medici.impl.FileStorageMedici;

/**
 * Binds Persistence DAOs
 * 
 * @author Chris Navarro <cmnavarr@illinois.edu>
 * 
 */
public class PersistenceModule extends AbstractModule {
    private Logger logger = LoggerFactory.getLogger(PersistenceModule.class);

    @Override
    protected void configure() {
        JpaPersistModule jpa = new JpaPersistModule("WolfPersistence");

        // Configure Persistence
        // String datawolfProperties = "src/test/resources/datawolf.properties";

        // This contains properties to set on injected files (e.g. medici key)
        Properties properties = new Properties();
        String datawolfProperties = System.getProperty("datawolf.properties");
        if (datawolfProperties.trim() != "") {
            File file = new File(datawolfProperties);
            try {
                properties.load(new FileInputStream(file));
                Names.bindProperties(binder(), properties);
                jpa.properties(properties);
            } catch (IOException e) {
                logger.error("Error reading properties file: " + System.getProperty("datawolf.properties"), e);
            }
        }

        install(jpa);

        bind(PersonDao.class).to(PersonJPADao.class);
        bind(SubmissionDao.class).to(SubmissionJPADao.class);
        bind(WorkflowDao.class).to(WorkflowJPADao.class);
        bind(WorkflowStepDao.class).to(WorkflowStepJPADao.class);
        bind(WorkflowToolDao.class).to(WorkflowToolJPADao.class);
        bind(WorkflowToolParameterDao.class).to(WorkflowToolParameterJPADao.class);
        bind(WorkflowToolDataDao.class).to(WorkflowToolDataJPADao.class);
        bind(DatasetDao.class).to(DatasetMediciDao.class);
        bind(ExecutionDao.class).to(ExecutionJPADao.class);
        bind(LogFileDao.class).to(LogFileJPADao.class);
        bind(HPCJobInfoDao.class).to(HPCJobInfoJPADao.class);
        bind(AccountDao.class).to(AccountJPADao.class);

        bind(FileDescriptorDao.class).to(FileDescriptorMediciDao.class);

        // String datawolfProperties = "src/test/resources/datawolf.properties";

        // This contains properties to set on injected files (e.g. medici key)

        bind(FileStorage.class).to(FileStorageMedici.class);

        MapBinder<String, Executor> binder = MapBinder.newMapBinder(binder(), String.class, Executor.class);
        binder.addBinding("java").to(JavaExecutor.class);
        binder.addBinding("commandline").to(CommandLineExecutor.class);
        bind(JavaExecutor.class);
        bind(CommandLineExecutor.class);
        bind(Engine.class);
    }
}
