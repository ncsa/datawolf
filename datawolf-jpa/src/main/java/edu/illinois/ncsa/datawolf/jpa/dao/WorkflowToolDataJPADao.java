package edu.illinois.ncsa.datawolf.jpa.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDataDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class WorkflowToolDataJPADao extends AbstractJPADao<WorkflowToolData, String> implements WorkflowToolDataDao {
    @Inject
    public WorkflowToolDataJPADao(EntityManager entityManager) {
        super(entityManager);
    }
}
