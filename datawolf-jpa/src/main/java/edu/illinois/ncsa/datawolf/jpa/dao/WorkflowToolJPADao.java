package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

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

    @Override
    public List<WorkflowTool> findByDeleted(boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WorkflowTool> findByCreatorEmail(String email) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WorkflowTool> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }
}
