/*******************************************************************************
 * Copyright (c) 2012 University of Illinois/NCSA.  All rights reserved.
 * 
 *   National Center for Supercomputing Applications (NCSA)
 *   http://www.ncsa.illinois.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of University of Illinois, NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 ******************************************************************************/
package edu.illinois.ncsa.datawolf.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.util.Base64;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.illinois.ncsa.datawolf.service.utils.LoginUtil;
import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.PersonDao;

@Path("/login")
public class LoginResource {
    private Logger       log          = LoggerFactory.getLogger(LoginResource.class);

    @Inject
    private PersonDao    personDao;

    @Inject
    private AccountDao   accountDao;

    @Inject
    @Named("initialAdmins")
    private String       initialAdmins;

    private List<String> admins       = null;

    // Generates temporary user token
    private SecureRandom secureRandom = new SecureRandom();

    /**
     * Login a user by email and authorization string
     * 
     * @param email
     *            email of the user attempting to authenticate
     * @param auth
     *            authentication string/token
     * @return the user if authentication is successful
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response login(@QueryParam("email") String email, @HeaderParam("Authorization") String auth) {
        if ((auth != null) && !auth.equals("")) {
            try {
                String decoded = new String(Base64.decode(auth.substring(6)));
                StringTokenizer tokenizer = new StringTokenizer(decoded, ":");
                if (tokenizer.countTokens() == 2) {
                    String user = tokenizer.nextToken();
                    String password = tokenizer.nextToken();
                    Account userAccount = accountDao.findByUserid(user);
                    if (userAccount == null) {
                        log.error("User account does not exist");
                        return null;
                    }

                    if (!userAccount.isActive()) {
                        return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Not Active").build();
                    }

                    if (BCrypt.checkpw(password, userAccount.getPassword()) && userAccount.getPerson().getEmail().equals(email)) {
                        String token = new BigInteger(130, secureRandom).toString(32);
                        userAccount.setToken(token);
                        accountDao.save(userAccount);
                        return Response.ok(userAccount.getPerson()).cookie(new NewCookie("token", user + ":" + token, null, null, null, 86400, false)).build();
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (IOException e) {
                log.error("Error decoding token", e);
                return null;
            }
        }
        return null;

    }

    /**
     * Create a user account
     * 
     * @param email
     *            email of the account to create
     * @param password
     *            password for the user account
     * @throws Exception
     *             If email/password not specified or if user does not exist
     */
    @POST
    @Produces({ MediaType.TEXT_PLAIN })
    public Response createAccount(@QueryParam("email") @DefaultValue("") String email, @QueryParam("personId") @DefaultValue("") String personId, @QueryParam("password") String password)
            throws Exception {

        Person person = null;
        Account account = null;
        if (email != null && !email.equals("") && (password != null) && !password.equals("")) {
            person = personDao.findByEmail(email);
            account = accountDao.findByUserid(email);
        } else if (personId != null && !personId.equals("") && (password != null) && !password.equals("")) {
            person = personDao.findOne(personId);
            account = accountDao.findByUserid(personId);
        } else {
            throw (new Exception("No email, personId or password specified"));
        }

        if (person == null) {
            throw (new Exception("User does not exist"));
        }

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
        if (account == null) {
            account = new Account();
            account.setPerson(person);
            account.setUserid(person.getEmail());
            account.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));

            String token = null;
            if (admins.contains(person.getEmail())) {
                token = new BigInteger(130, secureRandom).toString(32);

                account.setActive(true);
                account.setAdmin(true);
                account.setToken(token);
            }

            accountDao.save(account);
            if (token != null) {
                return Response.ok().cookie(new NewCookie("token", email + ":" + token, null, null, null, 86400, false)).build();
            } else {
                return Response.ok("Not Active").build();
            }

        } else {
            log.warn("Account for specified user already exists.");
        }

        return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Account for specified user already exists").build();

    }

    /**
     * Enable/Disable user accounts
     * 
     * @param request
     *            - Request body with key-value pair for active attribute
     * @param personId
     *            - User account to update
     * @return Response from request, Success (200) or Error (500) with message
     */
    @PUT
    @Path("{person-id}/active")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response updateAccount(@Context HttpRequest request, @PathParam("person-id") String personId) {
        String credential = null;

        if (request.getHttpHeaders().getCookies().containsKey("token")) {
            Cookie cookie = request.getHttpHeaders().getCookies().get("token");
            credential = cookie.getValue();
        } else if (request.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION) != null) {
            if (!request.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION).isEmpty()) {
                String authorization = request.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION).get(0);
                try {
                    credential = new String(Base64.decode(authorization.substring(6)));
                } catch (IOException e) {
                    log.error("Error decoding authorization", e);
                }
            }
        }

        if (credential != null) {
            List<String> credentials = LoginUtil.parseCredentials(credential);
            Person person = personDao.findOne(personId);
            Account account = accountDao.findByUserid(credentials.get(0));

            if (person != null && account.isAdmin()) {

                Account updateAccount = accountDao.findByUserid(person.getEmail());
                if (updateAccount != null) {
                    boolean active = updateAccount.isActive();
                    try {
                        JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new InputStreamReader(request.getInputStream())));
                        JsonObject body = jsonElement.getAsJsonObject();
                        if (body.has("active")) {
                            active = body.get("active").getAsBoolean();
                        }
                        updateAccount.setActive(active);
                        accountDao.save(updateAccount);

                        return Response.ok("Updated account").build();
                    } catch (Exception e) {
                        log.error("Could not parse request body.", e);
                        return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Could not parse request body").build();
                    }

                } else {
                    return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Could not find user account for specified user").build();
                }
            }

        }

        return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Failed to updated account.").build();

    }

    /**
     * Delete the account given the specific id.
     * 
     * @param email
     *            the email for whom the account to be deleted
     */
    @DELETE
    public Response disable(@Context HttpRequest request, @QueryParam("email") @DefaultValue("") String email) throws Exception {
        if ((email == null) || email.equals("")) {
            log.error("No email specified.");
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("No email specified.").build();
        }

        String credential = null;

        if (request.getHttpHeaders().getCookies().containsKey("token")) {
            Cookie cookie = request.getHttpHeaders().getCookies().get("token");
            credential = cookie.getValue();
        } else if (request.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION) != null) {
            if (!request.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION).isEmpty()) {
                String authorization = request.getHttpHeaders().getRequestHeader(HttpHeaders.AUTHORIZATION).get(0);
                credential = new String(Base64.decode(authorization.substring(6)));
            }
        }

        if (credential != null) {
            List<String> credentials = LoginUtil.parseCredentials(credential);
            Account adminAccount = accountDao.findByUserid(credentials.get(0));

            if (adminAccount.isAdmin()) {
                Account deleteAccount = accountDao.findByUserid(email);
                if (deleteAccount != null) {
                    deleteAccount.setDeleted(true);
                    deleteAccount.setActive(false);
                    accountDao.save(deleteAccount);
                    return Response.status(HttpStatus.SC_OK).entity("Account deleted.").build();
                }
            } else {
                log.error("Only administrators can delete accounts.");
                return Response.status(HttpStatus.SC_FORBIDDEN).entity("Only administrators can delete accounts.").build();
            }
        }

        return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("Failed to delete account.").build();
    }

}
