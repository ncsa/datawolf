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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Entity(name = "Dataset")
@Document(collection = "Dataset")
public class Dataset extends AbstractBean implements Serializable {
    /** Used for serialization of object */
    private static final long    serialVersionUID = 1L;

    /** Title of the artifact */
    private String               title            = "";        //$NON-NLS-1$

    /** Description of the artifact */
    private String               description      = "";        //$NON-NLS-1$

    /** Date the artifact is created */
    private Date                 date             = new Date();

    /** creator of the artifact */
    @DBRef
    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    private Person               creator          = null;

    /** List of contributors to the artifact. */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "DatasetContributors")
    @DBRef
    private List<Person>         contributors     = null;

    /** all blobs associated with this dataset */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "DatasetBlobs")
    @DBRef
    private List<FileDescriptor> blobs            = null;

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
    public List<Person> getContributors() {
        if (contributors == null) {
            contributors = new ArrayList<Person>();
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
     * Remove the contributor from the set of contributors to the dataset.
     * 
     * @param contributor
     *            the PersonBean of the contributor to be removed.
     */
    public void removeContributor(Person contributor) {
        getContributors().remove(contributor);
    }

    /**
     * Return the set of blobs associated with the dataset.
     * 
     * @return set of blob associated with the dataset.
     */
    public List<FileDescriptor> getBlobs() {
        if (blobs == null) {
            blobs = new ArrayList<FileDescriptor>();
        }
        return blobs;
    }

    /**
     * Set the set of blobs associated with the dataset.
     * 
     * @param blobs
     *            the set of blobs to the dataset.
     */
    public void setBlobs(Collection<FileDescriptor> blobs) {
        getBlobs().clear();
        if (blobs != null) {
            getBlobs().addAll(blobs);
        }
    }

    /**
     * Add the blob to the set of blobs to the dataset.
     * 
     * @param blob
     *            the blob to be added.
     */
    public void addBlob(FileDescriptor blob) {
        if (blob != null) {
            getBlobs().add(blob);
        }
    }

    /**
     * Remove the blob from the set of blobs of the dataset.
     * 
     * @param blob
     *            the blob to be removed.
     */
    public void removeBlob(FileDescriptor blob) {
        getBlobs().remove(blob);
    }
}
