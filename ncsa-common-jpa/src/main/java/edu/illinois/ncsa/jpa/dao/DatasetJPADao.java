package edu.illinois.ncsa.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.dao.DatasetDao;

public class DatasetJPADao extends AbstractJPADao<Dataset, String> implements DatasetDao {

    @Inject
    protected DatasetJPADao(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<Dataset> findByDeleted(boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByTitleLike(String titlePattern) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmail(String email) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }
}
