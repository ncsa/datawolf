package edu.illinois.ncsa.medici.dao;

import java.io.Serializable;
import java.util.List;

import edu.illinois.ncsa.domain.dao.IDao;

/**
 * 
 * TODO - implement Medici CRUD operations
 */
public abstract class AbstractMediciDao<T, ID extends Serializable> implements IDao<T, ID> {

    public T save(T entity) {
        return entity;
    }

    public void delete(T entity) {}

    public T findOne(ID id) {
        return null;
    }

    public boolean exists(ID id) {
        return findOne(id) != null;
    }

    public List<T> findAll() {
        return null;
    }

    public long count() {
        // TODO implement count
        return 0;
    }
}
