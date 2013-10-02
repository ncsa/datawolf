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

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties("@id")
public class AbstractBean implements Serializable {
    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** Unique identifier for this bean, used by persistence layer */
    @Id
    @Column(length = 36)
    private String            id;

    /** Should the bean be assumed to be deleted and not be returned */
    private boolean           deleted          = false;

    public AbstractBean() {
        setId(UUID.randomUUID().toString());
    }

    /**
     * Return the id of the bean.
     * 
     * @return id of the bean
     */
    public final String getId() {
        return id;
    }

    /**
     * Sets the id of the bean. This has to be a unique id since it is used as
     * the key in the database.
     * 
     * @param id
     *            the id of the object.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Should the bean be assumed to be deleted. Only a handfule rest api calls
     * right now will use this value.
     * 
     * @return true if the bean is deleted, false otherwise.
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Should the bean be assumed to be deleted. Only a handfule rest api calls
     * right now will use this value.
     * 
     * @param deleted
     *            true if the bean is deleted, false otherwise.
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Compares two objects with each other. If the object is an AbstractBean it
     * will compare id's, otherwise it will return false.
     * 
     * @param obj
     *            the object that should be compared to this AbstractBean
     * @return true if the two beans are the same (i.e. the id's are the same),
     *         false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractBean) {
            return ((AbstractBean) obj).getId().equals(getId());
        }
        return false;
    }

    /**
     * Returns the hashcode of this object, which is the hashcode of the id.
     * 
     * @return hashcode based on the id of the bean.
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
