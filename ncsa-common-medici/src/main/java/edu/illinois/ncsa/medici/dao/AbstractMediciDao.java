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
        System.out.println("save " + entity.getClass().getName() + " to medici");
        return entity;
    }

    public void delete(T entity) {
        System.out.println("delete " + entity.getClass().getName() + " from medici");
    }

    public T findOne(ID id) {
        System.out.println("find one entity in medici");
        return null;
    }

    public boolean exists(ID id) {
        return findOne(id) != null;
    }

    public List<T> findAll() {
        System.out.println("find all objects of this type from medici");
        return null;
    }

    public long count() {
        // TODO implement count
        return 0;
    }
}
