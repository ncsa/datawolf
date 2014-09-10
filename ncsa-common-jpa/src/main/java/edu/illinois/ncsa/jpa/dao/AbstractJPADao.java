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

import com.google.inject.persist.Transactional;

import edu.illinois.ncsa.domain.dao.IDao;

public abstract class AbstractJPADao<T, ID extends Serializable> implements IDao<T, ID> {

    private EntityManager entityManager;
    private Class<T>      entityType;

    @Inject
    protected AbstractJPADao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public T save(T entity) {
        return getEntityManager().merge(entity);
    }

    @Transactional
    public void delete(T entity) {
        getEntityManager().remove(entity);
    }

    public T findOne(ID id) {
        return getEntityManager().find(getEntityType(), id);
    }

    public List<T> findAll() {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(getEntityType());
        Root<T> root = query.from(getEntityType());
        query.select(root);

        return this.entityManager.createQuery(query).getResultList();
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
        return entityManager;
    }

}