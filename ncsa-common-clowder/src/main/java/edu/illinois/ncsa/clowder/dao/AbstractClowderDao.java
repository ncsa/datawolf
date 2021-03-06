package edu.illinois.ncsa.clowder.dao;

import java.io.Serializable;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.IDao;

/**
 * Clowder Parent DAO
 */
public abstract class AbstractClowderDao<T, ID extends Serializable> implements IDao<T, ID> {

    @Inject
    @Named("clowder.server")
    private String     server;

    @Inject
    @Named("clowder.key")
    private String     key = "";

    @Inject(optional = true)
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

    public List<T> findAll(int page, int size) {
        return null;
    }

    public long count(boolean deleted) {
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
        String token = null;
        if (accountDao != null) {
            Account acct = accountDao.findByUserid(userId);
            if (acct != null) {
                token = acct.getToken();
            }
        }
        return token;
    }

}
