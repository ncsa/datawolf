package edu.illinois.ncsa.cyberintegrator.service;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.resteasy.util.Base64;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.springdata.AccountDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Provider
@ServerInterceptor
public class AuthenticationInterceptor implements PreProcessInterceptor {
    private Logger  log     = LoggerFactory.getLogger(DatasetsResource.class);

    private boolean enabled = Boolean.getBoolean("cyberintegrator.authentication");

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
        if (!enabled) {
            return null;
        }

        if (request.getHttpHeaders().getRequestHeader("Authorization") != null) {
            String token = request.getHttpHeaders().getRequestHeader("Authorization").get(0);

            if (token != null && checkLoggedIn(token)) {
                log.debug("Authorization header found - Sucessfully authenticated");
                return null;
            } else {
                return unauthorizedResponse(request.getPreprocessedPath());
            }
        } else {
            log.debug("Not authenticated");
            return unauthorizedResponse(request.getPreprocessedPath());
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
        try {
            String decoded = new String(Base64.decode(token.substring(6)));
            StringTokenizer tokenizer = new StringTokenizer(decoded, ":");
            if (tokenizer.countTokens() == 2) {
                String user = tokenizer.nextToken();
                // TODO RK : password should really be encrypted
                String password = tokenizer.nextToken();
                Account account = SpringData.getBean(AccountDAO.class).findByUserid(user);
                if ((account != null) && account.getPassword().equals(password)) {
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
}
