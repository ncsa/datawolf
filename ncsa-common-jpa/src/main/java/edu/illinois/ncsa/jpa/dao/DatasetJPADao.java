package edu.illinois.ncsa.jpa.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.dao.DatasetDao;

public class DatasetJPADao extends AbstractJPADao<Dataset, String> implements DatasetDao {

    @Inject
    protected DatasetJPADao(EntityManager entityManager) {
        super(entityManager);
    }
}
