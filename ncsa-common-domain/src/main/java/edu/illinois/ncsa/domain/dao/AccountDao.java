package edu.illinois.ncsa.domain.dao;

import edu.illinois.ncsa.domain.Account;

public interface AccountDao extends IDao<Account, String> {
    Account findByUserid(String userId);

    Account findByToken(String token);
}
