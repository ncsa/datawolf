package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

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

    @Override
    public List<Workflow> findByDeleted(boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Workflow> findByCreatorEmail(String email) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Workflow> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }
}
