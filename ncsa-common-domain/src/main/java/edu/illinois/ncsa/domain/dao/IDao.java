package edu.illinois.ncsa.domain.dao;

import java.io.Serializable;
import java.util.List;

/**
 * DAO interface for generic operations supported by all DAOs
 */
public interface IDao<T, ID extends Serializable> {
    T save(T entity);

    void delete(T entity);

    T findOne(ID id);

    List<T> findAll();

    List<T> findAll(int page, int size);

    boolean exists(ID id);

    long count(boolean deleted);
}
