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
import edu.illinois.ncsa.domain.TokenProvider;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.domain.impl.DataWolfTokenProvider;
import edu.illinois.ncsa.domain.impl.FileStorageDisk;
import edu.illinois.ncsa.jpa.dao.AccountJPADao;
import edu.illinois.ncsa.jpa.dao.DatasetJPADao;
import edu.illinois.ncsa.jpa.dao.FileDescriptorJPADao;
import edu.illinois.ncsa.jpa.dao.PersonJPADao;

public class DevPersistenceModule extends AbstractModule {
    private Logger logger = LoggerFactory.getLogger(DevPersistenceModule.class);

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
                properties.setProperty("engine.timeout", "3600");
                properties.setProperty("engine.extraLocalExecutor", "4");
                properties.setProperty("engine.localExecutorThreads", "4");
                properties.setProperty("engine.pageSize", "50");
                properties.setProperty("executor.debug", "false");
                Names.bindProperties(binder(), properties);
                jpa.properties(properties);
            } catch (IOException e) {
                logger.error("Error reading properties file", e);
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
        bind(DatasetDao.class).to(DatasetJPADao.class);
        bind(ExecutionDao.class).to(ExecutionJPADao.class);
        bind(LogFileDao.class).to(LogFileJPADao.class);
        bind(HPCJobInfoDao.class).to(HPCJobInfoJPADao.class);
        bind(AccountDao.class).to(AccountJPADao.class);
        bind(TokenProvider.class).to(DataWolfTokenProvider.class);

        bind(FileDescriptorDao.class).to(FileDescriptorJPADao.class);
        bind(FileStorage.class).to(FileStorageDisk.class);
        bindConstant().annotatedWith(Names.named("engine.storeLogs")).to(true);

        MapBinder<String, Executor> binder = MapBinder.newMapBinder(binder(), String.class, Executor.class);
        binder.addBinding("java").to(JavaExecutor.class);
        binder.addBinding("commandline").to(CommandLineExecutor.class);
        bind(JavaExecutor.class);
        bind(CommandLineExecutor.class);
        bind(Engine.class);
    }
}
