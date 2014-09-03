package edu.illinois.ncsa.datawolf.jpa.dao;

import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDataDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolParameterDao;

public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("WolfPersistence"));

        bind(ExecutionDao.class).to(ExecutionJPADao.class);
        bind(WorkflowDao.class).to(WorkflowJPADao.class);
        bind(WorkflowStepDao.class).to(WorkflowStepJPADao.class);
        bind(WorkflowToolDao.class).to(WorkflowToolJPADao.class);
        bind(WorkflowToolDataDao.class).to(WorkflowToolDataJPADao.class);
        bind(WorkflowToolParameterDao.class).to(WorkflowToolParameterJPADao.class);

    }
}
