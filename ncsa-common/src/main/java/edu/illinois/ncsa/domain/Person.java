/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010 , NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
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
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
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
 *******************************************************************************/
package edu.illinois.ncsa.domain;

import java.io.Serializable;

import javax.persistence.Entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Entity(name = "Person")
@Document(collection = "Person")
public class Person extends AbstractBean implements Serializable {
    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** Name of the person. */
    private String            name             = ""; //$NON-NLS-1$

    /** email of the person. */
    private String            email            = ""; //$NON-NLS-1$

    /**
     * Create a definition of a anonymous user
     */
    public Person() {}

    /**
     * Sets the name of the person.
     * 
     * @param name
     *            the name of the person
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the person.
     * 
     * @return the name of the person.
     */
    public String getName() {
        return name;
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
    public boolean equals(Object arg0) {
        if (arg0 instanceof Person) {
            Person other = (Person) arg0;
            return (name + email).equals(other.name + other.email);
        }
        return false;
    }
}