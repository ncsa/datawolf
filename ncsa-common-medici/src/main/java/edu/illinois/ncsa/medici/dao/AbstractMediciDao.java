package edu.illinois.ncsa.medici.dao;

import java.io.Serializable;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.dao.IDao;

/**
 * Clowder Parent DAO
 */
public abstract class AbstractMediciDao<T, ID extends Serializable> implements IDao<T, ID> {

    @Inject
    @Named("medici.server")
    private String server;

    @Inject
    @Named("medici.key")
    private String key = "";

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

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    /**
     * @return clowder service endpoint
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server
     *            - configured clowder service
     */
    public void setServer(String server) {
        if (server.endsWith("/")) {
            this.server = server;
        } else {
            this.server = server + "/";
        }
    }

}
