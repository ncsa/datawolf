package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class WorkflowJPADao extends AbstractJPADao<Workflow, String> implements WorkflowDao {

    @Inject
    public WorkflowJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    public List<Workflow> findByDeleted(boolean deleted) {
        EntityManager em = getEntityManager();
        List<Workflow> results = null;
        try {
            em.getTransaction().begin();
            String queryString = "SELECT w FROM Workflow w " + "WHERE w.deleted = :deleted";
            TypedQuery<Workflow> typedQuery = em.createQuery(queryString, Workflow.class);
            typedQuery.setParameter("deleted", deleted);
            results = typedQuery.getResultList();

            return refreshList(results);
        } finally {
            em.getTransaction().commit();
        }
    }

    @Override
    public List<Workflow> findByCreatorEmail(String email) {

        List<Workflow> results = null;
        try {
            getEntityManager().getTransaction().begin();

            String queryString = "SELECT w FROM Workflow w " + "WHERE w.email = :email";

            TypedQuery<Workflow> typedQuery = getEntityManager().createQuery(queryString, Workflow.class);
            typedQuery.setParameter("email", email);

            results = typedQuery.getResultList();
            return refreshList(results);
        } finally {
            getEntityManager().getTransaction().commit();
        }

    }

    @Override
    public List<Workflow> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        List<Workflow> results = null;
        try {
            getEntityManager().getTransaction().begin();
            String queryString = "SELECT w FROM Workflow w " + "WHERE w.creator.email = :email and w.deleted = :deleted";

            TypedQuery<Workflow> typedQuery = getEntityManager().createQuery(queryString, Workflow.class);
            typedQuery.setParameter("email", email);
            typedQuery.setParameter("deleted", deleted);
            results = typedQuery.getResultList();
            return refreshList(results);
        } finally {
            getEntityManager().getTransaction().commit();
        }
    }
}
