package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class WorkflowJPADao extends AbstractJPADao<Workflow, String> implements WorkflowDao {

    @Inject
    public WorkflowJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    @Transactional
    public List<Workflow> findByDeleted(boolean deleted) {
        EntityManager em = getEntityManager();
        List<Workflow> results = null;

        String queryString = "SELECT w FROM Workflow w " + "WHERE w.deleted = :deleted";
        TypedQuery<Workflow> typedQuery = em.createQuery(queryString, Workflow.class);
        typedQuery.setParameter("deleted", deleted);
        results = typedQuery.getResultList();

        return results;
    }

    @Override
    @Transactional
    public List<Workflow> findByCreatorEmail(String email) {
        List<Workflow> results = null;
        String queryString = "SELECT w FROM Workflow w " + "WHERE w.email = :email";

        TypedQuery<Workflow> typedQuery = getEntityManager().createQuery(queryString, Workflow.class);
        typedQuery.setParameter("email", email);

        results = typedQuery.getResultList();
        return results;

    }

    @Override
    @Transactional
    public List<Workflow> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        List<Workflow> results = null;
        String queryString = "SELECT w FROM Workflow w " + "WHERE w.creator.email = :email and w.deleted = :deleted";

        TypedQuery<Workflow> typedQuery = getEntityManager().createQuery(queryString, Workflow.class);
        typedQuery.setParameter("email", email);
        typedQuery.setParameter("deleted", deleted);
        results = typedQuery.getResultList();

        return results;
    }
}
