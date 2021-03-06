package edu.illinois.ncsa.domain.impl;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.ssl.JVMDefaultTrustManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.TokenProvider;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class LDAPTokenProvider implements TokenProvider {

    private Logger       log          = LoggerFactory.getLogger(LDAPTokenProvider.class);
    @Inject
    private AccountDao   accountDao;

    @Inject
    private PersonDao    personDao;

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

    @Inject
    @Named("ldap.trustAllCertificates")
    private boolean      trustAllCertificates;

    @Inject
    @Named("initialAdmins")
    private String       initialAdmins;

    private List<String> admins       = null;

    // Generates temporary user token
    private SecureRandom secureRandom = new SecureRandom();

    @Override
    public String getToken(String username, String password) {

        LDAPConnection ldapConnection = null;
        try {
            log.debug("base user namespace is: " + baseUserNamespace);
            SSLUtil sslUtil = null;
            if (trustAllCertificates) {
                sslUtil = new SSLUtil(new TrustAllTrustManager());
            } else {
                sslUtil = new SSLUtil(JVMDefaultTrustManager.getInstance());
            }
            ldapConnection = new LDAPConnection(sslUtil.createSSLSocketFactory(), hostname, port);
            log.debug("successfully created ldap connection");

            // Try to bind, this will fail if not authorized
            ldapConnection.bind("uid=" + username + "," + baseUserNamespace, password);
            log.debug("authentication success");

            // Filter to search the user's membership in the specified group
            Filter searchFilter = Filter.create("(&(objectClass=" + objectClass + ")(memberOf=cn=" + group + "," + baseGroupNamespace + "," + baseDN + ")(uid=" + username + "))");

            // Perform group membership search
            SearchResult searchResult = ldapConnection.search(baseDN, SearchScope.SUB, searchFilter);
            log.debug(searchResult.getEntryCount() + " LDAP entries returned.");

            if (searchResult.getEntryCount() == 1) {
                log.debug("User successfully validated. Forwarding request after checking against LDAP.");
                SearchResultEntry entry = searchResult.getSearchEntries().get(0);
                String email = "";
                if (entry.hasAttribute("mail")) {
                    email = entry.getAttributeValue("mail");
                } else {
                    log.error("Error finding email attribute in LDAP response.");
                    return null;
                }

                // Make sure user doesn't already exist
                Person person = personDao.findByEmail(email);
                if (person == null) {
                    String givenName = "";
                    if (entry.hasAttribute("givenName")) {
                        givenName = entry.getAttributeValue("givenName");
                    }
                    String surname = "";
                    if (entry.hasAttribute("sn")) {
                        surname = entry.getAttributeValue("sn");
                    }

                    person = new Person();
                    person.setFirstName(givenName);
                    person.setLastName(surname);
                    person.setEmail(email);
                    person = personDao.save(person);
                }
                Account account = accountDao.findByUserid(username);
                // If a token is associated with the account, return it, otherwise, generate a
                // new one
                if (account != null) {
                    // Eventually this should check expiration too
                    if (account.getToken() != null && !account.getToken().isEmpty()) {
                        return account.getToken();
                    }
                } else {
                    // Need to create User and account
                    // Initialize the list of admins the first time this is called
                    if (admins == null) {
                        // Remove double quotes around string
                        initialAdmins = initialAdmins.replaceAll("^\"|\"$", "").trim();
                        if (initialAdmins.isEmpty()) {
                            admins = new ArrayList<String>();
                        } else {
                            admins = Arrays.asList(initialAdmins.split(","));
                        }
                    }

                    account = new Account();
                    account.setPerson(person);
                    account.setUserid(username);
                    if (admins.contains(username)) {
                        account.setActive(true);
                        account.setAdmin(true);
                    }
                    accountDao.save(account);
                }
                // Generate a new token
                return new BigInteger(130, secureRandom).toString(32);
            } else {
                log.error("User not a member of the LDAP group: " + group);
            }
        } catch (LDAPException e) {
            log.error("Error authenticating with LDAP.", e);
        } catch (GeneralSecurityException e) {
            log.error("Error authenticating with LDAP.", e);
        } finally {
            if (ldapConnection != null) {
                ldapConnection.close();
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
