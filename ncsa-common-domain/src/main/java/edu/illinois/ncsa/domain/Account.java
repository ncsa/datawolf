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

public class Account extends AbstractBean {
    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** userid of the person, often email address. */
    private String            userid           = "";   //$NON-NLS-1$

    /** password associated with the userid. */
    private String            password         = "";   //$NON-NLS-1$

    private String            token            = "";

    /** email of the person. */
    private Person            person           = null;

    private boolean           active           = false;

    private boolean           admin            = false;

    public Account() {}

    @Override
    public boolean equals(Object arg0) {
        return (arg0 instanceof Account) && this.toString().equals(arg0.toString());
    }

    /**
     * @return the userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid
     *            the userid to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     * @return token for user account
     */
    public String getToken() {
        return token;
    }

    /**
     * Associate a token with a user account
     * 
     * @param token
     *            token to associate with user account
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * @param person
     *            the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * Check if user account is active
     * 
     * @return true if account is enabled, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set the user account as active
     * 
     * @param active
     *            - if true, user account is enabled, if false, user account is
     *            disabled
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Check if user account is an admin
     * 
     * @return true if account is an administrative account, false otherwose
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Set the administrative privileges of the account
     * 
     * @param admin
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

}
