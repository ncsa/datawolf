package edu.illinois.ncsa.datawolf.jpa.dao;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class WorkflowStepJPADao extends AbstractJPADao<WorkflowStep, String> implements WorkflowStepDao {

    @Inject
    public WorkflowStepJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }
}
