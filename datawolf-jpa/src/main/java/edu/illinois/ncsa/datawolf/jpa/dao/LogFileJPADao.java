package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Provider;

import edu.illinois.ncsa.datawolf.domain.LogFile;
import edu.illinois.ncsa.datawolf.domain.dao.LogFileDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class LogFileJPADao extends AbstractJPADao<LogFile, String> implements LogFileDao {
    @Inject
    public LogFileJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    public List<LogFile> findByExecutionId(String executionId) {
        List<LogFile> results = null;
        try {
            getEntityManager().getTransaction().begin();
            String queryString = "SELECT l FROM LogFile l " + "WHERE l.executionId = :executionId";
            TypedQuery<LogFile> typedQuery = getEntityManager().createQuery(queryString, LogFile.class);
            typedQuery.setParameter("executionId", executionId);
            results = typedQuery.getResultList();
            return results;
        } finally {
            getEntityManager().getTransaction().commit();
        }
    }

    @Override
    public LogFile findByExecutionIdAndStepId(String executionId, String stepId) {
        List<LogFile> results = null;
        try {
            getEntityManager().getTransaction().begin();
            String queryString = "SELECT l FROM LogFile l " + "WHERE l.executionId = :executionId and l.stepId = :stepId";
            TypedQuery<LogFile> typedQuery = getEntityManager().createQuery(queryString, LogFile.class);
            typedQuery.setParameter("executionId", executionId);
            typedQuery.setParameter("stepId", stepId);
            results = typedQuery.getResultList();
            return results.get(0);
        } finally {
            getEntityManager().getTransaction().commit();
        }
    }
}
