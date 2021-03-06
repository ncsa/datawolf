package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class WorkflowToolJPADao extends AbstractJPADao<WorkflowTool, String> implements WorkflowToolDao {
    @Inject
    public WorkflowToolJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    @Transactional
    public List<WorkflowTool> findByDeleted(boolean deleted) {
        EntityManager em = getEntityManager();
        List<WorkflowTool> results = null;

        String queryString = "SELECT w FROM WorkflowTool w " + "WHERE w.deleted = :deleted";
        TypedQuery<WorkflowTool> typedQuery = em.createQuery(queryString, WorkflowTool.class);
        typedQuery.setParameter("deleted", deleted);
        results = typedQuery.getResultList();

        return results;
    }

    @Override
    @Transactional
    public List<WorkflowTool> findByCreatorEmail(String email) {
        List<WorkflowTool> results = null;
        String queryString = "SELECT w FROM WorkflowTool w " + "WHERE w.creator.email = :email";

        TypedQuery<WorkflowTool> typedQuery = getEntityManager().createQuery(queryString, WorkflowTool.class);
        typedQuery.setParameter("email", email);
        results = typedQuery.getResultList();

        return results;
    }

    @Override
    @Transactional
    public List<WorkflowTool> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        List<WorkflowTool> results = null;
        String queryString = "SELECT w FROM WorkflowTool w " + "WHERE w.creator.email = :email and w.deleted = :deleted";

        TypedQuery<WorkflowTool> typedQuery = getEntityManager().createQuery(queryString, WorkflowTool.class);
        typedQuery.setParameter("email", email);
        typedQuery.setParameter("deleted", deleted);
        results = typedQuery.getResultList();

        return results;
    }
}
