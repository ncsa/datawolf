package edu.illinois.ncsa.datawolf.jpa.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.google.inject.Provider;

import edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolParameterDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class WorkflowToolParameterJPADao extends AbstractJPADao<WorkflowToolParameter, String> implements WorkflowToolParameterDao {
    @Inject
    public WorkflowToolParameterJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }
}
