package edu.illinois.ncsa.jpa.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.inject.Provider;

import edu.illinois.ncsa.domain.dao.IDao;

public abstract class AbstractJPADao<T, ID extends Serializable> implements IDao<T, ID> {

    private Provider<EntityManager> entityManager;
    private Class<T>                entityType;

    @Inject
    protected AbstractJPADao(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    // @Transactional
    public T save(T entity) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T saved = em.merge(entity);
            return saved;
        } finally {
            em.getTransaction().commit();
        }
    }

    public void delete(T entity) {
        try {
            getEntityManager().getTransaction().begin();
            getEntityManager().remove(entity);
        } finally {
            getEntityManager().getTransaction().commit();
        }
    }

    public T findOne(ID id) {
        EntityManager em = getEntityManager();
        T entity = null;
        try {
            em.getTransaction().begin();
            entity = em.find(getEntityType(), id);
            // TODO this seems like a caching problem
            if (entity != null) {
                em.refresh(entity);
            }
        } finally {
            em.getTransaction().commit();
        }
        return entity;
    }

    public List<T> findAll() {
        List<T> result = null;
        EntityManager entityManager = this.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = builder.createQuery(getEntityType());
            Root<T> root = query.from(getEntityType());
            query.select(root);

            result = entityManager.createQuery(query).getResultList();
            // TODO this seems like a caching problem
            for (T entity : result) {
                entityManager.refresh(entity);
            }
        } finally {
            entityManager.getTransaction().commit();
        }
        return result;

    }

    public boolean exists(ID id) {
        return findOne(id) != null;
    }

    public long count() {
        return findAll().size();
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
