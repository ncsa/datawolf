package edu.illinois.ncsa.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.dao.AccountDao;

public class AccountJPADao extends AbstractJPADao<Account, String> implements AccountDao {

    @Inject
    public AccountJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    @Transactional
    public Account findByUserid(String userId) {
        EntityManager em = getEntityManager();
        List<Account> list = null;
        String queryString = "SELECT a FROM Account a " + "WHERE a.userid = :userid";

        TypedQuery<Account> typedQuery = em.createQuery(queryString, Account.class);
        typedQuery.setParameter("userid", userId);
        list = typedQuery.getResultList();
        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }
}
