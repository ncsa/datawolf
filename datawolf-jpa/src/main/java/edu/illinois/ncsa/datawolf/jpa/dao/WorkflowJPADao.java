package edu.illinois.ncsa.datawolf.jpa.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class WorkflowJPADao extends AbstractJPADao<Workflow, String> implements WorkflowDao {
    @Inject
    public WorkflowJPADao(EntityManager entityManager) {
        super(entityManager);
    }
}
