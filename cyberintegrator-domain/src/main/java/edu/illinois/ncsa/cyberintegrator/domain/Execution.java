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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;

@Entity(name = "Execution")
@Document(collection = "Execution")
public class Execution extends AbstractBean {
    public enum State {
        WAITING, RUNNING, FINISHED, ABORTED, FAILED
    }

    /** Used for serialization of object */
    private static final long    serialVersionUID = 1L;

    /** Workflow that is executed */
    @DBRef
    private Workflow             workflow         = null;

    /** Date the execution is created */
    private Date                 date             = new Date();

    /** creator of the execution */
    @DBRef
    private Person               creator          = null;

    /** maping a parameter to a specific parameter in the workflow */
    private Map<String, String>  parameters       = new HashMap<String, String>();

    /** maping a dataset to a specific dataset in the workflow */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ExecutionDatasets")
    @DBRef
    private Map<String, Dataset> datasets         = new HashMap<String, Dataset>();

    /** the state of each step executed */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ExecutionStepState")
    private Map<String, State>   stepState        = new HashMap<String, State>();

    /** the start date of each step executed */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ExecutionStepStart")
    private Map<String, Date>    stepStart        = new HashMap<String, Date>();

    /** the end date of each step executed */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ExecutionStepEnd")
    private Map<String, Date>    stepEnd          = new HashMap<String, Date>();

    /**
     * Create a new instance of the execution.
     */
    public Execution() {}

    /**
     * Return the workflow that was executed.
     * 
     * @return workflow that was executed.
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * Sets the workflow that is executed.
     * 
     * @param workflow
     *            sets the workflow this execution is for.
     * 
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Return the date when the execution was created.
     * 
     * @return date the execution was created.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date when the execution was created.
     * 
     * @param date
     *            sets the date when the execution was created.
     * 
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Return the PersonBean that is the creator of the execution
     * 
     * @return PersonBean that represents the creator
     */
    public Person getCreator() {
        return creator;
    }

    /**
     * Sets the PersonBean that represents the creator of the execution.
     * 
     * @param creator
     *            sets the PersonBean that represents the creator of the
     *            execution.
     */
    public void setCreator(Person creator) {
        this.creator = creator;
    }

    /**
     * Returns the value for a specific parameter.
     * TODO this should search the workflow for the default if not found.
     * 
     * @param uuid
     *            the uuid for parameter
     * @return the value of the parameter.
     */
    public String getParameter(String uuid) {
        return this.parameters.get(uuid);
    }

    /**
     * Sets the value for the specific parameter.
     * 
     * @param uuid
     *            the uuid of the parameter to set.
     * @param value
     *            the value of the parameter.
     */
    public void setParameter(String uuid, String value) {
        this.parameters.put(uuid, value);
    }

    /**
     * Returns the value for a specific dataset.
     * 
     * @param uuid
     *            the uuid for dataset
     * @return the dataset.
     */
    public Dataset getDataset(String uuid) {
        return this.datasets.get(uuid);
    }

    /**
     * Sets the dataset associated with that specific uuid.
     * 
     * @param uuid
     *            the uuid of the output to set.
     * @param value
     *            the dataset generated.
     */
    public void setDataset(String uuid, Dataset dataset) {
        this.datasets.put(uuid, dataset);
    }

    /**
     * Returns the state for a specific step.
     * 
     * @param uri
     *            the uri for the step in the workflow
     * @return the state of the step.
     */
    public State getStepState(String uri) {
        return this.stepState.get(uri);
    }

    /**
     * Sets the state of the specific step in the workflow.
     * 
     * @param uri
     *            the uri of the workflow step.
     * @param state
     *            the state of the workflow step.
     */
    public void setStepState(String uri, State state) {
        this.stepState.put(uri, state);
    }
}
