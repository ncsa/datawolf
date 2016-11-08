package edu.illinois.ncsa.datawolf.service;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.resteasy.util.Base64;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.dao.AccountDao;

@Provider
@ServerInterceptor
// TODO replace this deprecated interface
public class AuthenticationInterceptor implements PreProcessInterceptor {
    private Logger     log     = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private boolean    enabled = Boolean.getBoolean("datawolf.authentication");

    @Inject
    private AccountDao accountDao;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethodInvoker method) throws Failure, WebApplicationException {
        if (!enabled) {
            return null;
        }
        // TODO fix web editor login so it doesn't need access to /persons
        // Don't intercept /login or /persons endpoint
        if (!request.getUri().getPath().contains("/login") && !request.getUri().getPath().contains("/persons")) {

            if (request.getHttpHeaders().getCookies().containsKey("token")) {
                Cookie cookie = request.getHttpHeaders().getCookies().get("token");
                String token = cookie.getValue();
                if (token != null && checkToken(token)) {
                    log.debug("Valid cookie - successfully authenticated");
                    return null;
                } else {
                    return unauthorizedResponse("Not authenticated");
                }
            } else if (request.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION) != null) {
                String token = null;
                if (!request.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION).isEmpty()) {
                    token = request.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION).get(0);
                }

                if (token != null && checkLoggedIn(token)) {
                    log.debug("Authorization header found - Sucessfully authenticated");
                    return null;
                } else {
                    // TODO preprocessedPath not part of httprequest
                    // return
                    // unauthorizedResponse(request.getPreprocessedPath());
                    return unauthorizedResponse("Not authenticated");
                }
            } else {
                // TODO preprocessedPath not part of httprequest
                // return unauthorizedResponse(request.getPreprocessedPath());
                return unauthorizedResponse("Not authenticated");
            }
        } else {
            return null;
        }
    }

    /**
     * Response in case of failed authentication.
     * 
     * @param preprocessedPath
     * @return
     */
    private ServerResponse unauthorizedResponse(String preprocessedPath) {
        ServerResponse response = new ServerResponse();
        response.setStatus(HttpResponseCodes.SC_UNAUTHORIZED);
        MultivaluedMap<String, Object> headers = new Headers<Object>();
        headers.add("Content-Type", "text/plain");
        response.setMetadata(headers);
        response.setEntity("Error 401 Unauthorized: " + preprocessedPath);
        return response;
    }

    /**
     * Decode token and check against local user database.
     * 
     * @param token
     * @return
     */
    private boolean checkLoggedIn(String token) {
        // TODO
        try {
            String decoded = new String(Base64.decode(token.substring(6)));
            StringTokenizer tokenizer = new StringTokenizer(decoded, ":");
            if (tokenizer.countTokens() == 2) {
                String user = tokenizer.nextToken();
                // TODO RK : password should really be encrypted
                String password = tokenizer.nextToken();
                Account account = accountDao.findByUserid(user);
                if ((account != null) && !account.isDeleted() && account.getPassword().equals(password)) {
                    log.debug("REST Authentication successful");
                    return true;
                } else {
                    log.debug("REST Authentication failed");
                    return false;
                }
            } else {
                log.error("Authentication token not complete");
                return false;
            }
        } catch (IOException e) {
            log.error("Error decoding token");
        }
        return false;
    }

    /**
     * Check token against local user database.
     * 
     * @param cookie
     *            token in request
     * @return true if token matches, false otherwise
     */
    private boolean checkToken(String cookie) {
        StringTokenizer tokenizer = new StringTokenizer(cookie, ":");
        if (tokenizer.countTokens() == 2) {
            String user = tokenizer.nextToken();

            String token = tokenizer.nextToken();
            Account account = accountDao.findByUserid(user);
            if ((account != null) && !account.isDeleted() && account.getToken().equals(token)) {
                log.debug("REST Authentication successful");
                return true;
            } else {
                log.debug("REST Authentication failed");
                return false;
            }
        } else {
            log.error("Authentication token not complete");
            return false;
        }
    }
}
