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
package edu.illinois.ncsa.cyberintegrator.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Person;

@Entity(name = "Workflow")
@Document(collection = "Workflow")
public class Workflow extends AbstractBean {
    /** Used for serialization of object */
    private static final long  serialVersionUID = 1L;

    /** Title of the workflow */
    private String             title            = "";                           //$NON-NLS-1$

    /** Description of the workflow */
    private String             description      = "";                           //$NON-NLS-1$

    /** Date the workflow is created */
    @Temporal(TemporalType.DATE)
    private Date               created          = new Date();

    /** creator of the workflow */
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @DBRef
    private Person             creator          = null;

    /** List of contributors to the workflow. */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowContributors")
    @DBRef
    private List<Person>       contributors     = new ArrayList<Person>();

    /** List of steps in the workflow, in order of addition. */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowSteps")
    @DBRef
    private List<WorkflowStep> steps            = new ArrayList<WorkflowStep>();

    /**
     * Create a new instance of the workflow.
     */
    public Workflow() {}

    /**
     * Return the title of the workflow.
     * 
     * @return title of the workflow
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the workflow
     * 
     * @param title
     *            sets the title of the workflow.
     * 
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the description of the workflow.
     * 
     * @return description of the workflow
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the workflow
     * 
     * @param description
     *            sets the description of the workflow.
     * 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the date when the workflow was created.
     * 
     * @return date the workflow was created.
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the date when the workflow was created.
     * 
     * @param date
     *            sets the date when the workflow was created.
     * 
     */
    public void setDate(Date created) {
        this.created = created;
    }

    /**
     * Return the PersonBean that is the creator of the workflow
     * 
     * @return PersonBean that represents the creator
     */
    public Person getCreator() {
        return creator;
    }

    /**
     * Sets the PersonBean that represents the creator of the workflow.
     * 
     * @param creator
     *            sets the PersonBean that represents the creator of the
     *            workflow.
     * 
     */
    public void setCreator(Person creator) {
        this.creator = creator;
    }

    /**
     * Return the set of PersonBeans that represents those that are contributors
     * to the workflow.
     * 
     * @return set of PersonBeans that represents all the users that have
     *         contributes to the workflow.
     * 
     */
    public Collection<Person> getContributors() {
        return this.contributors;
    }

    /**
     * Set the set of PersonBeans that represents those that contributed to the
     * workflow.
     * 
     * @param contributors
     *            the set of contributors to the workflow.
     */
    public void setContributors(Collection<Person> contributors) {
        this.contributors.clear();
        if (contributors != null) {
            this.contributors.addAll(contributors);
        }
    }

    /**
     * Add the contributor to the set of contributors to the workflow.
     * 
     * @param contributor
     *            the PersonBean of the contributor to be added.
     */
    public void addContributor(Person contributor) {
        if (contributor != null) {
            this.contributors.add(contributor);
        }
    }

    /**
     * Remove the contributor from the set of contributors to the worflow.
     * 
     * @param contributor
     *            the PersonBean of the contributor to be removed.
     */
    public void removeContributor(Person contributor) {
        this.contributors.remove(contributor);
    }

    /**
     * Retrieve the list of steps of a workflow. This returns the list of the
     * steps as they have been added to the workflow.
     * 
     * @return list of steps as they have been added to the workflow.
     */
    public List<WorkflowStep> getSteps() {
        return this.steps;
    }

    /**
     * Retrieve a specific step from the workflow. Better to use the DAO and get
     * the step directly since this will iterate over all steps in the workflow.
     * 
     * @return the step requested, or null if it is not found.
     */
    public WorkflowStep getStep(String step) {
        for (WorkflowStep ws : this.steps) {
            if (ws.getId().equals(step)) {
                return ws;
            }
        }
        return null;
    }

    /**
     * Set the list of steps of a workflow. This sets the list of the steps as
     * they have been added to the workflow.
     * 
     * @param steps
     *            list of steps as they have been added to the workflow.
     */
    public void setSteps(List<WorkflowStep> steps) {
        this.steps.clear();
        if (steps != null) {
            this.steps.addAll(steps);
        }
    }

    public WorkflowToolData getOutput(String id) {
        for (WorkflowStep step : steps) {
            WorkflowToolData data = step.getOutput(id);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    /**
     * Add the step to the list of steps in the workflow. This will add the step
     * to the end of the list of steps associated with the workflow.
     * 
     * @param step
     *            the WorkflowStepBean to be added.
     */
    public void addStep(WorkflowStep step) {
        if ((step != null) && !this.steps.contains(step)) {
            this.steps.add(step);
        }
    }
}
