package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import edu.illinois.ncsa.datawolf.domain.HPCJobInfo;
import edu.illinois.ncsa.datawolf.domain.dao.HPCJobInfoDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class HPCJobInfoJPADao extends AbstractJPADao<HPCJobInfo, String> implements HPCJobInfoDao {
    @Inject
    public HPCJobInfoJPADao(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<HPCJobInfo> findByExecutionId(String executionId) {
        // TODO Auto-generated method stub
        return null;
    }
}
