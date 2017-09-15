package edu.illinois.ncsa.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.dao.DatasetDao;

public class DatasetJPADao extends AbstractJPADao<Dataset, String> implements DatasetDao {
    private static final Logger log = LoggerFactory.getLogger(DatasetJPADao.class);

    @Inject
    protected DatasetJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    @Transactional
    public List<Dataset> findByDeleted(boolean deleted) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.deleted = :deleted";
        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("deleted", deleted);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Transactional
    public List<Dataset> findByDeleted(boolean deleted, int page, int size) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.deleted = :deleted";
        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("deleted", deleted);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Override
    public List<Dataset> findByTitleLike(String titlePattern) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.title LIKE :title";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("title", "%" + titlePattern + "%");

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Override
    public List<Dataset> findByTitleLike(String titlePattern, int page, int size) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.title LIKE :title";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("title", "%" + titlePattern + "%");
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Override
    public List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.title LIKE :title and d.deleted = :deleted";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("title", "%" + titlePattern + "%");
        q.setParameter("deleted", deleted);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Override
    public List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted, int page, int size) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.title LIKE :title and d.deleted = :deleted";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("title", "%" + titlePattern + "%");
        q.setParameter("deleted", deleted);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Transactional
    public List<Dataset> findByCreatorEmail(String email) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("email", email);

        List<Dataset> results = q.getResultList();
        return results;

    }

    @Override
    @Transactional
    public List<Dataset> findByCreatorEmail(String email, int page, int size) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("email", email);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<Dataset> results = q.getResultList();
        return results;

    }

    @Transactional
    public List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email and d.deleted = :deleted";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("email", email);
        q.setParameter("deleted", deleted);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Transactional
    public List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted, int page, int size) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email and d.deleted = :deleted";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("email", email);
        q.setParameter("deleted", deleted);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email and d.title LIKE :title";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("title", "%" + titlePattern + "%");
        q.setParameter("email", email);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern, int page, int size) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email and d.title LIKE :title";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("title", "%" + titlePattern + "%");
        q.setParameter("email", email);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email and d.deleted = :deleted and d.title LIKE :title";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("title", "%" + titlePattern + "%");
        q.setParameter("email", email);
        q.setParameter("deleted", deleted);

        List<Dataset> results = q.getResultList();
        return results;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted, int page, int size) {
        String queryString = "SELECT d FROM Dataset d " + "WHERE d.creator.email = :email and d.deleted = :deleted and d.title LIKE :title";

        TypedQuery<Dataset> q = getEntityManager().createQuery(queryString, Dataset.class);
        q.setParameter("title", "%" + titlePattern + "%");
        q.setParameter("email", email);
        q.setParameter("deleted", deleted);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<Dataset> results = q.getResultList();
        return results;
    }
}
