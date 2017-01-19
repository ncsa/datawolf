package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import edu.illinois.ncsa.datawolf.domain.HPCJobInfo;
import edu.illinois.ncsa.datawolf.domain.dao.HPCJobInfoDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class HPCJobInfoJPADao extends AbstractJPADao<HPCJobInfo, String> implements HPCJobInfoDao {
    @Inject
    public HPCJobInfoJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    @Transactional
    public List<HPCJobInfo> findByExecutionId(String executionId) {
        List<HPCJobInfo> results = null;

        String queryString = "SELECT o FROM HPCJobInfo o " + "WHERE o.executionId = :executionId";
        TypedQuery<HPCJobInfo> typedQuery = getEntityManager().createQuery(queryString, HPCJobInfo.class);
        typedQuery.setParameter("executionId", executionId);
        results = typedQuery.getResultList();

        return results;
    }
}
