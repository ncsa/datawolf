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
package edu.illinois.ncsa.cyberintegrator.service;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.AccountDAO;
import edu.illinois.ncsa.springdata.PersonDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/login")
public class LoginResource {
    private Logger log = LoggerFactory.getLogger(LoginResource.class);

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
    public Person login(@QueryParam("email") String email, @HeaderParam("Authorization") String auth) {
        PersonDAO personDao = SpringData.getBean(PersonDAO.class);

        if ((auth != null) && !auth.equals("")) {
            try {
                String decoded = new String(Base64.decode(auth.substring(6)));
                StringTokenizer tokenizer = new StringTokenizer(decoded, ":");
                if (tokenizer.countTokens() == 2) {
                    String user = tokenizer.nextToken();
                    return personDao.findByEmail(user);
                } else {
                    return null;
                }
            } catch (IOException e) {
                log.error("Error decoding token", e);
            }
        }

        if ((email != null) && !email.equals("")) {
            return personDao.findByEmail(email);
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
    public void createAccount(@QueryParam("email") String email, @QueryParam("password") String password) throws Exception {
        PersonDAO personDao = SpringData.getBean(PersonDAO.class);

        if ((email == null) || email.equals("") || (password == null) || password.equals("")) {
            throw (new Exception("No email or password specified"));
        }

        Person person = personDao.findByEmail(email);
        if (person == null) {
            throw (new Exception("User does not exist"));
        }

        AccountDAO accountDAO = SpringData.getBean(AccountDAO.class);
        Account account = accountDAO.findByUserid(email);
        if (account == null) {
            account = new Account();
            account.setPerson(person);
            account.setUserid(person.getEmail());
        }
        account.setPassword(password);
        account.setDeleted(false);
        SpringData.getBean(AccountDAO.class).save(account);
    }

    /**
     * Delete the account given the specific id.
     * 
     * @param email
     *            the email for whom the account to be deleted
     */
    @DELETE
    public void disable(@QueryParam("email") @DefaultValue("") String email) throws Exception {
        if ((email == null) || email.equals("")) {
            throw (new Exception("No email specified"));
        }

        AccountDAO accountDAO = SpringData.getBean(AccountDAO.class);
        Account account = accountDAO.findByUserid(email);
        if (account != null) {
            account.setDeleted(true);
            accountDAO.save(account);
        }
    }
}
