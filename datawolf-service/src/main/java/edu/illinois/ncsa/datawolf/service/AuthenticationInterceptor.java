package edu.illinois.ncsa.datawolf.service;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
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

import edu.illinois.ncsa.datawolf.service.utils.LoginUtil;
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
        // Don't intercept /login or /persons for creating users or loggin in
        // TODO Consider making this more specific so future POST endpoints are
        // not included in this
        String requestPath = request.getUri().getPath();
        if ((requestPath.contains("/persons") || requestPath.contains("/login")) && request.getHttpMethod().equals(HttpMethod.POST)) {
            return null;
        } else {
            if (request.getHttpHeaders().getCookies().containsKey("token")) {
                Cookie cookie = request.getHttpHeaders().getCookies().get("token");
                String token = cookie.getValue();
                if (token != null && checkLoggedIn(token, true)) {
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

                if (token != null && checkLoggedIn(token, false)) {
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
     * Check if a user is logged into the system
     * 
     * @param credential
     *            - user credential
     * @param tokenAuth
     *            - if true, using token authentication, otherwise using
     *            password
     *            authentication
     * @return true is user is logged in, false otherwise
     */
    private boolean checkLoggedIn(String credential, boolean tokenAuth) {

        String tmpCredential = credential;
        if (!tokenAuth) {
            try {
                log.warn("decoding auth string");
                tmpCredential = new String(Base64.decode(credential.substring(6)));
            } catch (IOException e) {
                log.error("Error decoding token", e);
                return false;
            }
        }

        List<String> credentials = LoginUtil.parseCredentials(tmpCredential);
        if (credentials.size() != 2) {
            log.warn("Could not parse credential to obtain user and password/token.");
            return false;
        }

        String user = credentials.get(0);
        String token = credentials.get(1);

        Account account = accountDao.findByUserid(user);
        if (account == null || account.isDeleted() || !account.isActive()) {
            log.error("Authentication failed, user account does not exist, is deleted, or is not active.");
            return false;
        }

        if (!tokenAuth) {
            // TODO RK : password should really be encrypted
            return account.getPassword().equals(token);
        } else {
            return account.getToken().equals(token);
        }
    }
}