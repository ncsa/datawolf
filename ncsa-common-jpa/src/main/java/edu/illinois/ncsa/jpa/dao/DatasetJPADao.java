package edu.illinois.ncsa.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.dao.DatasetDao;

public class DatasetJPADao extends AbstractJPADao<Dataset, String> implements DatasetDao {

    @Inject
    protected DatasetJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    @Transactional
    public List<Dataset> findByDeleted(boolean deleted) {
        List<Dataset> results = null;
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.deleted = :deleted";
        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("deleted", deleted);
        results = q.getResultList();
        return results;
    }

    @Transactional
    public List<Dataset> findByDeleted(boolean deleted, int page, int size) {
        List<Dataset> results = null;
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.deleted = :deleted";
        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("deleted", deleted);
        q.setFirstResult(page * size);
        q.setMaxResults(size);
        results = q.getResultList();
        return results;
    }

    @Override
    public List<Dataset> findByTitleLike(String titlePattern) {
        // TODO implement query
        return null;
    }

    @Override
    public List<Dataset> findByTitleLike(String titlePattern, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted) {
        // TODO implement query
        return null;
    }

    @Override
    public List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted, int page, int size) {
        // TODO implement query
        return null;
    }

    @Transactional
    public List<Dataset> findByCreatorEmail(String email) {
        List<Dataset> results = null;
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("email", email);
        results = q.getResultList();

        return results;

    }

    @Override
    @Transactional
    public List<Dataset> findByCreatorEmail(String email, int page, int size) {
        List<Dataset> results = null;
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("email", email);
        q.setFirstResult(page * size);
        q.setMaxResults(size);
        results = q.getResultList();

        return results;

    }

    @Transactional
    public List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        List<Dataset> results = null;
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email and d.deleted = :deleted";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("email", email);
        q.setParameter("deleted", deleted);
        results = q.getResultList();

        return results;
    }

    @Transactional
    public List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted, int page, int size) {
        List<Dataset> results = null;
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email and d.deleted = :deleted";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("email", email);
        q.setParameter("deleted", deleted);
        q.setFirstResult(page * size);
        q.setMaxResults(size);
        results = q.getResultList();

        return results;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern) {
        // TODO implement query
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted) {
        // TODO implement query
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

}
