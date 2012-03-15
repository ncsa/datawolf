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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Entity(name = "Dataset")
@Document(collection = "Dataset")
public class Dataset extends AbstractBean implements Serializable {
    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** Title of the artifact */
    private String            title            = "";        //$NON-NLS-1$

    /** Description of the artifact */
    private String            description      = "";        //$NON-NLS-1$

    /** Date the artifact is created */
    private Date              date             = new Date();

    /** creator of the artifact */
    @DBRef
    private Person            creator          = null;

    /** size of the blob associated */
    private long              size             = -1;

    /** List of contributors to the artifact. */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "DatasetContributors")
    @DBRef
    private Set<Person>       contributors     = null;

    /** Mime type of the workflow tool data */
    private String            mimetype         = "";        //$NON-NLS-1$

    /** Original filename */
    private String            filename         = null;

    /**
     * Create a new instance of the artifact.
     */
    public Dataset() {}

    /**
     * Return the title of the artifact.
     * 
     * @return title of the artifact
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the artifact
     * 
     * @param title
     *            sets the title of the artifact.
     * 
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the description of the artifact.
     * 
     * @return description of the artifact
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the artifact
     * 
     * @param description
     *            sets the description of the artifact
     * 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the date when the artifact was created.
     * 
     * @return date the artifact was created.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date when the artifact was created.
     * 
     * @param date
     *            sets the date when the artifact was created.
     * 
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Return the PersonBean that is the creator of the artifact
     * 
     * @return PersonBean that represents the creator
     */
    public Person getCreator() {
        return creator;
    }

    /**
     * Sets the PersonBean that represents the creator of the artifact.
     * 
     * @param creator
     *            sets the PersonBeans that represents the creator of the
     *            artifact.
     * 
     */
    public void setCreator(Person creator) {
        this.creator = creator;
    }

    /**
     * Return the set of PersonBeans that represents those that are contributors
     * to the artifact.
     * 
     * @return set of PersonBeans that represents all the users that have
     *         contributes to the artifact.
     */
    public Set<Person> getContributors() {
        if (contributors == null) {
            contributors = new HashSet<Person>();
        }
        return contributors;
    }

    /**
     * Set the set of PersonBeans that represents those that contributed to the
     * artifact.
     * 
     * @param contributors
     *            the set of contributors to the artifact.
     */
    public void setContributors(Collection<Person> contributors) {
        getContributors().clear();
        if (contributors != null) {
            getContributors().addAll(contributors);
        }
    }

    /**
     * Add the contributor to the set of contributors to the artifact.
     * 
     * @param contributor
     *            the PersonBean of the contributor to be added.
     */
    public void addContributor(Person contributor) {
        if (contributor != null) {
            getContributors().add(contributor);
        }
    }

    /**
     * Remove the contributor from the set of contributors to the worflow tool.
     * 
     * @param contributor
     *            the PersonBean of the contributor to be removed.
     */
    public void removeContributor(Person contributor) {
        getContributors().remove(contributor);
    }

    /**
     * Return the mime type of the artifact.
     * 
     * @return mime type of the artifact
     */
    public String getMimeType() {
        return mimetype;
    }

    /**
     * Sets the mime type of the artifact
     * 
     * @param mimetype
     *            sets the mime type of the artifact
     * 
     */
    public void setMimeType(String mimetype) {
        this.mimetype = mimetype;
    }

    /**
     * Returns the size of the blob, this is -2 if there is no blob associated
     * or the size has not been computed.
     * 
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the size of the blob, this can be set to -2 if there is no blob
     * associated with this dataset.
     * 
     * @param size
     *            the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * If this dataset originated in a filesystem, or is intended to be stored
     * in a filesystem, what filename is/should be used? If not, set to null.
     * 
     * @param filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * If this dataset originated in a filesystem, or is intended to be stored
     * in a filesystem, what filename is/should be used? If not, will return
     * null.
     * 
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
}