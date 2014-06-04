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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.PersonDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/persons")
public class PersonsResource {

    /**
     * Get all users
     * 
     * @param size
     *            number of users per page
     * @param page
     *            page number starting 0
     * @param showdeleted
     *            should we return deleted person.
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Person> getPersons(@QueryParam("size") @DefaultValue("100") int size, @QueryParam("page") @DefaultValue("0") int page, @QueryParam("email") @DefaultValue("") String email,
            @QueryParam("showdeleted") @DefaultValue("false") boolean showdeleted) {
        PersonDAO personDao = SpringData.getBean(PersonDAO.class);
        if ("".equals(email)) {
            Page<Person> results = null;
            if (showdeleted) {
                results = personDao.findAll(new PageRequest(page, size));
            } else {
                results = personDao.findByDeleted(false, new PageRequest(page, size));
            }
            return results.getContent();
        } else {
            Person result = personDao.findByEmail(email);
            List<Person> results = new ArrayList<Person>();
            results.add(result);
            return results;
        }

    }

    /**
     * 
     * @param firstName
     *            given name of user to create
     * @param lastName
     *            surname of user to create
     * @param email
     *            email address of user to create
     * @return id of generated user
     */
    @POST
    @Produces({ MediaType.TEXT_PLAIN })
    public String createPerson(@QueryParam("firstname") @DefaultValue("") String firstName, @QueryParam("lastname") @DefaultValue("") String lastName,
            @QueryParam("email") @DefaultValue("") String email) {
        if ("".equals(email))
            return null;
        PersonDAO personDao = SpringData.getBean(PersonDAO.class);
        Person p = personDao.findByEmail(email);
        if (p == null) {
            p = new Person();
        }
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);
        p = personDao.save(p);
        return p.getId();
    }

    /**
     * 
     * Get a dataset by Id
     * 
     * @param personId
     *            dataset Id
     * @return
     *         a dataset in JSON
     */
    @GET
    @Path("{person-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Person getPerson(@PathParam("person-id") String personId) {
        PersonDAO personDao = SpringData.getBean(PersonDAO.class);
        return personDao.findOne(personId);
    }

    /**
     * Delete the person given the specific id.
     * 
     * @param personId
     *            the person to be deleted
     */
    @DELETE
    @Path("{person-id}")
    public void deletePerson(@PathParam("person-id") String personId) throws Exception {
        if ("".equals(personId)) {
            throw (new Exception("Invalid id passed in."));
        }
        PersonDAO personDao = SpringData.getBean(PersonDAO.class);
        Person person = personDao.findOne(personId);
        if (person == null) {
            throw (new Exception("Invalid id passed in."));
        }
        person.setDeleted(true);
        personDao.save(person);
    }
}