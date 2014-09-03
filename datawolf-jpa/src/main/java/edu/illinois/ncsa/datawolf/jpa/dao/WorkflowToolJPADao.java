package edu.illinois.ncsa.datawolf.jpa.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class WorkflowToolJPADao extends AbstractJPADao<WorkflowTool, String> implements WorkflowToolDao {
    @Inject
    public WorkflowToolJPADao(EntityManager entityManager) {
        super(entityManager);
    }
}
