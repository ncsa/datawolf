package edu.illinois.ncsa.medici.dao;

import java.io.Serializable;
import java.util.Base64;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.IDao;

/**
 * Clowder Parent DAO
 */
public abstract class AbstractMediciDao<T, ID extends Serializable> implements IDao<T, ID> {

    @Inject
    @Named("medici.server")
    private String     server;

    @Inject
    @Named("medici.key")
    private String     key = "";

    @Inject
    private AccountDao accountDao;

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

    public String getKey() {
        return this.key;
    }

    /**
     * @return clowder service endpoint
     */
    public String getServer() {
        // Make sure the service endpoint ends with a slash
        if (!server.endsWith("/")) {
            this.server += "/";
        }
        return server;
    }

    protected String getToken(String userId) {
        // TODO replace this with token from Clowder
        Account acct = accountDao.findByUserid(userId);
        String token = null;
        if (acct != null) {
            token = new String(Base64.getEncoder().encode(new String(acct.getUserid() + ":" + acct.getPassword()).getBytes()));
        }
        return token;
    }

}
