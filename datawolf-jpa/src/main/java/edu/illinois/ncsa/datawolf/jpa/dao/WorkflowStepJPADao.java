package edu.illinois.ncsa.datawolf.jpa.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class WorkflowStepJPADao extends AbstractJPADao<WorkflowStep, String> implements WorkflowStepDao {
    @Inject
    public WorkflowStepJPADao(EntityManager entityManager) {
        super(entityManager);
    }
}
