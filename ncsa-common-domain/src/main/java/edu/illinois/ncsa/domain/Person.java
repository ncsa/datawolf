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
package edu.illinois.ncsa.domain;

import javax.persistence.Entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Entity(name = "Person")
@Document(collection = "Person")
public class Person extends AbstractBean {
    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** First Name of the person. */
    private String            firstName        = ""; //$NON-NLS-1$

    /** First Name of the person. */
    private String            lastName         = ""; //$NON-NLS-1$

    /** email of the person. */
    private String            email            = ""; //$NON-NLS-1$

    /**
     * Creates a person given first name, last name and email. This will result
     * in
     * a new bean generated, if a person exists with the given email it will
     * generate two instances of this person. Best is to make sure the DAO is
     * called first to see if a person with that email exists, if so use that
     * person object.
     * 
     * @param firstName
     *            first name of the person.
     * @param lastName
     *            last name of the person.
     * @param email
     *            email address of the person.
     * @return a newly generated person object.
     */
    public static Person createPerson(String firstName, String lastName, String email) {
        Person p = new Person();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);

        return p;
    }

    /**
     * Create a definition of a anonymous user
     */
    public Person() {}

    /**
     * Single function to set both first and last name of the person.
     * 
     * @param firstName
     *            the first name of the person.
     * @param lastName
     *            the last name of the person.
     */
    public void setName(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    /**
     * This will return the formatted name. The name will be lastname, firstname.
     * 
     * @return Returns a formatted lastname, firstname.
     */
    public String getName() {
        if ((lastName != null) && !lastName.equals("")) {
            if ((firstName != null) && !firstName.equals("")) {
                return lastName + ", " + firstName;
            } else {
                return lastName;
            }
        }
        if (firstName != null) {
            return firstName;
        }
        return "N/A";
    }

    /**
     * Sets the first name of the person.
     * 
     * @param firstName
     *            the first name of the person
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the first name of the person.
     * 
     * @return the first name of the person.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the last name of the person.
     * 
     * @param lastName
     *            the last name of the person
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the last name of the person.
     * 
     * @return the last name of the person.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the email of the person.
     * 
     * @param email
     *            the email of the person
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the email of the person.
     * 
     * @return the email of the person.
     */
    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + "<" + email + ">";
    }

    @Override
    public boolean equals(Object arg0) {
        return (arg0 instanceof Person) && this.toString().equals(arg0.toString());
    }
}
