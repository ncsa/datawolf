package edu.illinois.ncsa.domain.impl;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.TokenProvider;
import edu.illinois.ncsa.domain.dao.AccountDao;

public class LDAPTokenProvider implements TokenProvider {

    private Logger       log          = LoggerFactory.getLogger(LDAPTokenProvider.class);
    @Inject
    private AccountDao   accountDao;

    @Inject
    @Named("ldap.hostname")
    private String       hostname;

    @Inject
    @Named("ldap.port")
    private int          port;

    @Inject
    @Named("ldap.group")
    private String       group;

    @Inject
    @Named("ldap.baseDN")
    private String       baseDN;

    @Inject
    @Named("ldap.userDN")
    private String       baseUserNamespace;

    @Inject
    @Named("ldap.groupDN")
    private String       baseGroupNamespace;

    @Inject
    @Named("ldap.objectClass")
    private String       objectClass;

    // Generates temporary user token
    private SecureRandom secureRandom = new SecureRandom();

    @Override
    public String getToken(String username, String password) {

        try {
            log.debug("base user namespace is: " + baseUserNamespace);
            // TODO need to check for ldaps vs ldap
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            LDAPConnection conn = new LDAPConnection(sslUtil.createSSLSocketFactory(), hostname, port);
            log.debug("successfully created ldap connection");
            String userNamespace = baseUserNamespace + "," + baseDN;
            // Try to bind, this will fail if not authorized
            conn.bind("uid=" + username + "," + baseUserNamespace, password);
            log.debug("authentication success");
            Account account = accountDao.findByUserid(username);
            if (account != null && account.getToken() != null && !account.getToken().isEmpty()) {
                return account.getToken();
            }
            return new BigInteger(130, secureRandom).toString(32);
        } catch (LDAPException e) {
            log.error("Error authenticating with LDAP.", e);
        } catch (GeneralSecurityException e) {
            log.error("Error authenticating with LDAP.", e);
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
