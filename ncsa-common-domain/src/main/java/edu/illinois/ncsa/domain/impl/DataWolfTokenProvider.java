package edu.illinois.ncsa.domain.impl;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.inject.Inject;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.TokenProvider;
import edu.illinois.ncsa.domain.dao.AccountDao;

public class DataWolfTokenProvider implements TokenProvider {
    private Logger       log          = LoggerFactory.getLogger(DataWolfTokenProvider.class);

    @Inject
    private AccountDao   accountDao;

    // Generates temporary user token
    private SecureRandom secureRandom = new SecureRandom();

    @Override
    public String getToken(String username, String password) {
        Account account = accountDao.findByUserid(username);
        if (account != null && BCrypt.checkpw(password, account.getPassword())) {
            if (account.getToken() != null && !account.getToken().isEmpty()) {
                return account.getToken();
            } else {
                return new BigInteger(130, secureRandom).toString(32);
            }
        }

        return null;
    }

    @Override
    public boolean isTokenValid(String token) {
        Account account = accountDao.findByToken(token);
        if (account == null || account.isDeleted() || !account.isActive()) {
            log.error("Authentication failed, user account does not exist, is deleted, or is not active.");
            return false;
        }

        return account.getToken().equals(token);
    }

}
