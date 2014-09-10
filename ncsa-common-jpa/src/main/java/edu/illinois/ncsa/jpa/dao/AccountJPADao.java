package edu.illinois.ncsa.jpa.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.dao.AccountDao;

public class AccountJPADao extends AbstractJPADao<Account, String> implements AccountDao {

    @Inject
    public AccountJPADao(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Account findByUserid(String userId) {
        // TODO Auto-generated method stub
        return null;
    }

}
