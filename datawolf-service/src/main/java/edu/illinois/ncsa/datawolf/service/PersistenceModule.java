package edu.illinois.ncsa.datawolf.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import edu.illinois.ncsa.datawolf.executor.hpc.HPCExecutor;
import edu.illinois.ncsa.datawolf.executor.java.JavaExecutor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.dao.PersonDao;

/**
 * Binds Persistence DAOs and available executors
 * 
 * @author Chris Navarro <cmnavarr@illinois.edu>
 * 
 */
public class PersistenceModule extends AbstractModule {
    private Logger              logger = LoggerFactory.getLogger(PersistenceModule.class);
    private Properties          configuration;
    private Map<String, String> defaultDAOs;

    @Override
    protected void configure() {
        JpaPersistModule jpa = new JpaPersistModule("WolfPersistence");

        // This contains properties to set on injected files (e.g. medici key)
        configuration = new Properties();
        String datawolfProperties = System.getProperty("datawolf.properties");
        if (datawolfProperties.trim() != "") {
            File file = new File(datawolfProperties);
            try {
                configuration.load(new FileInputStream(file));
                Names.bindProperties(binder(), configuration);
                jpa.properties(configuration);
            } catch (IOException e) {
                logger.error("Error reading properties file: " + System.getProperty("datawolf.properties"), e);
            }
        }

        install(jpa);

        try {
            bind(PersonDao.class).to(getRequestedDao(PersonDao.class, "person.dao")); //$NON-NLS-1$
            bind(SubmissionDao.class).to(getRequestedDao(SubmissionDao.class, "submission.dao")); //$NON-NLS-1$
            bind(WorkflowDao.class).to(getRequestedDao(WorkflowDao.class, "workflow.dao")); //$NON-NLS-1$
            bind(WorkflowStepDao.class).to(getRequestedDao(WorkflowStepDao.class, "workflowstep.dao")); //$NON-NLS-1$
            bind(WorkflowToolDao.class).to(getRequestedDao(WorkflowToolDao.class, "workflowtool.dao")); //$NON-NLS-1$
            bind(WorkflowToolParameterDao.class).to(getRequestedDao(WorkflowToolParameterDao.class, "workflowtoolparameter.dao")); //$NON-NLS-1$
            bind(WorkflowToolDataDao.class).to(getRequestedDao(WorkflowToolDataDao.class, "workflowtooldata.dao")); //$NON-NLS-1$
            bind(DatasetDao.class).to(getRequestedDao(DatasetDao.class, "dataset.dao")); //$NON-NLS-1$
            bind(ExecutionDao.class).to(getRequestedDao(ExecutionDao.class, "execution.dao")); //$NON-NLS-1$
            bind(LogFileDao.class).to(getRequestedDao(LogFileDao.class, "logfile.dao")); //$NON-NLS-1$
            bind(HPCJobInfoDao.class).to(getRequestedDao(HPCJobInfoDao.class, "hpcjobinfo.dao")); //$NON-NLS-1$
            bind(AccountDao.class).to(getRequestedDao(AccountDao.class, "account.dao")); //$NON-NLS-1$
            bind(FileDescriptorDao.class).to(getRequestedDao(FileDescriptorDao.class, "filedescriptor.dao")); //$NON-NLS-1$
            bind(FileStorage.class).to(getRequestedDao(FileStorage.class, "filestorage")); //$NON-NLS-1$

            // Add executors
            MapBinder<String, Executor> binder = MapBinder.newMapBinder(binder(), String.class, Executor.class);

            if (configuration.containsKey("java.executor")) { //$NON-NLS-1$
                binder.addBinding(JavaExecutor.EXECUTOR_NAME).to(JavaExecutor.class);
                bind(JavaExecutor.class);
            }

            if (configuration.containsKey("commandline.executor")) { //$NON-NLS-1$
                binder.addBinding(CommandLineExecutor.EXECUTOR_NAME).to(CommandLineExecutor.class);
                bind(CommandLineExecutor.class);
            }

            if (configuration.containsKey("hpc.executor")) { //$NON-NLS-1$
                binder.addBinding(HPCExecutor.EXECUTOR_NAME).to(HPCExecutor.class);
            }

            bind(Engine.class);
        } catch (ClassNotFoundException e) {
            logger.error("Error binding DAO's", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> getRequestedDao(Class<T> type, String key) throws ClassNotFoundException {
        String daoClass;
        if (configuration.containsKey(key)) {
            daoClass = configuration.getProperty(key);
        } else {
            if (defaultDAOs == null) {
                defaultDAOs = getDefaultDAOs();
            }
            daoClass = defaultDAOs.get(key);
        }
        return (Class<T>) Class.forName(daoClass);
    }

    public Map<String, String> getDefaultDAOs() {
        Map<String, String> defaultDAOs = new HashMap<String, String>();
        defaultDAOs.put("submission.dao", "edu.illinois.ncsa.datawolf.jpa.dao.SubmissionJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("workflow.dao", "edu.illinois.ncsa.datawolf.jpa.dao.WorkflowJPADao"); //$NON-NLS-1$ //$NON-NLS-2$
        defaultDAOs.put("workflowstep.dao", "edu.illinois.ncsa.datawolf.jpa.dao.WorkflowStepJPADao"); //$NON-NLS-1$ //$NON-NLS-2$
        defaultDAOs.put("workflowtool.dao", "edu.illinois.ncsa.datawolf.jpa.dao.WorkflowToolJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("workflowtoolparameter.dao", "edu.illinois.ncsa.datawolf.jpa.dao.WorkflowToolParameterJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("workflowtooldata.dao", "edu.illinois.ncsa.datawolf.jpa.dao.WorkflowToolDataJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("execution.dao", "edu.illinois.ncsa.datawolf.jpa.dao.ExecutionJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("hpcjobinfo.dao", "edu.illinois.ncsa.datawolf.jpa.dao.HPCJobInfoJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("logfile.dao", "edu.illinois.ncsa.datawolf.jpa.dao.LogFileJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("person.dao", "edu.illinois.ncsa.jpa.dao.PersonJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("dataset.dao", "edu.illinois.ncsa.jpa.dao.DatasetJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("account.dao", "edu.illinois.ncsa.jpa.dao.AccountJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("filedescriptor.dao", "edu.illinois.ncsa.jpa.dao.FileDescriptorJPADao"); //$NON-NLS-1$//$NON-NLS-2$
        defaultDAOs.put("filestorage.dao", "edu.illinois.ncsa.domain.impl.FileStorageDisk"); //$NON-NLS-1$//$NON-NLS-2$

        return defaultDAOs;
    }

}
