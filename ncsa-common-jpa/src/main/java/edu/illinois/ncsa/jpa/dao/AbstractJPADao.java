package edu.illinois.ncsa.jpa.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import edu.illinois.ncsa.domain.dao.IDao;

public abstract class AbstractJPADao<T, ID extends Serializable> implements IDao<T, ID> {
    private static final Logger     log = LoggerFactory.getLogger(AbstractJPADao.class);

    private Provider<EntityManager> entityManager;
    private Class<T>                entityType;

    @Inject
    protected AbstractJPADao(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public T save(T entity) {
        EntityManager em = getEntityManager();
        return em.merge(entity);
    }

    @Transactional
    public void delete(T entity) {
        getEntityManager().remove(entity);
    }

    @Transactional
    public T findOne(ID id) {
        EntityManager em = getEntityManager();

        T entity = null;
        entity = em.find(getEntityType(), id);

        return entity;
    }

    @Transactional
    public List<T> findAll(int page, int size) {
        List<T> results = null;
        EntityManager entityManager = this.getEntityManager();
        TypedQuery<T> query = entityManager.createQuery("SELECT e FROM " + getEntityType().getSimpleName() + " e", getEntityType());
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        results = query.getResultList();

        return results;

    }

    @Transactional
    public List<T> findAll() {
        List<T> results = null;
        EntityManager entityManager = this.getEntityManager();
        // entityManager.getTransaction().begin();
        // CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        // CriteriaQuery<T> query = builder.createQuery(getEntityType());

        // Root<T> root = query.from(getEntityType());
        // query.select(root);
        TypedQuery<T> query = entityManager.createQuery("SELECT e FROM " + getEntityType().getSimpleName() + " e", getEntityType());
        results = query.getResultList();// entityManager.createQuery(query).getResultList();

        return results;

    }

    public boolean exists(ID id) {
        return findOne(id) != null;
    }

    @Transactional
    public long count(boolean deleted) {
        EntityManager entityManager = this.getEntityManager();
        TypedQuery<Long> query = entityManager.createQuery("SELECT COUNT(e) FROM " + getEntityType().getSimpleName() + " e WHERE e.deleted = :deleted", Long.class);
        query.setParameter("deleted", deleted);
        return query.getSingleResult();
    }

    protected Class<T> getEntityType() {
        if (entityType == null) {
            setParameterizedType();
        }
        return entityType;
    }

    @SuppressWarnings("unchecked")
    private void setParameterizedType() {
        Type t = getClass().getGenericSuperclass();
        Class<?> current = getClass();
        while (!(t instanceof ParameterizedType)) {
            current = current.getSuperclass();
            t = current.getGenericSuperclass();
        }
        ParameterizedType parameterizedType = (ParameterizedType) t;
        entityType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    public EntityManager getEntityManager() {
        return entityManager.get();
    }
}
