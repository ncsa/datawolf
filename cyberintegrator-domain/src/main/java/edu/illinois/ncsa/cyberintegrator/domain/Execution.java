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
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.jackson.JsonDateSerializer;

@Entity(name = "Execution")
@Document(collection = "Execution")
public class Execution extends AbstractBean {

    public enum State {
        WAITING, QUEUED, RUNNING, FINISHED, ABORTED, FAILED, UNKNOWN
    }

    /** Used for serialization of object */
    private static final long    serialVersionUID = 1L;

    /** Workflow that is executed */
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @DBRef
    private Workflow             workflow         = null;

    /** Date the execution is created */
    @Temporal(TemporalType.TIMESTAMP)
    private Date                 date             = new Date();

    /** creator of the execution */
    @DBRef
    private Person               creator          = null;

    /** maping a parameter to a specific parameter in the workflow */
    @ElementCollection
    @MapKeyColumn(name = "uri")
    @Column(name = "parameter")
    @CollectionTable(name = "ExecutionParameters")
    private Map<String, String>  parameters       = new HashMap<String, String>();

    /** maping a dataset to a specific dataset in the workflow */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ExecutionDatasets")
    @DBRef
    private Map<String, Dataset> datasets         = new HashMap<String, Dataset>();

    /** the state of each step executed */
    @ElementCollection
    @MapKeyColumn(name = "id")
    @Column(name = "state")
    @CollectionTable(name = "ExecutionStepState")
    private Map<String, State>   stepState        = new HashMap<String, State>();

    /** the start date of each step queued */
    @ElementCollection
    @MapKeyColumn(name = "id")
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    @CollectionTable(name = "ExecutionStepQueued")
    private Map<String, Date>    stepQueued       = new HashMap<String, Date>();

    /** the start date of each step executed */
    @ElementCollection
    @MapKeyColumn(name = "id")
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    @CollectionTable(name = "ExecutionStepStart")
    private Map<String, Date>    stepStart        = new HashMap<String, Date>();

    /** the end date of each step executed */
    @ElementCollection
    @MapKeyColumn(name = "id")
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    @CollectionTable(name = "ExecutionStepEnd")
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
    @JsonSerialize(using = JsonDateSerializer.class)
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
     * @param id
     * @return
     */
    public boolean hasParameter(String id) {
        return this.parameters.containsKey(id);
    }

    /**
     * Returns the value for a specific parameter.
     * TODO RK : this should search the workflow for the default if not found.
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
     * @param id
     * @return
     */
    public boolean hasDataset(String id) {
        return this.datasets.containsKey(id);
    }

    /**
     * Returns the value for a specific dataset.
     * 
     * @param uuid
     *            the uuid for dataset
     * @return the dataset.
     */
    public Dataset getDataset(String id) {
        return this.datasets.get(id);
    }

    /**
     * Sets the dataset associated with that specific uuid.
     * 
     * @param uuid
     *            the uuid of the output to set.
     * @param value
     *            the dataset generated.
     */
    public void setDataset(String id, Dataset dataset) {
        this.datasets.put(id, dataset);
    }

    /**
     * Returns the state for a specific step.
     * 
     * @param uri
     *            the uri for the step in the workflow
     * @return the state of the step.
     */
    public State getStepState(String id) {
        return this.stepState.get(id);
    }

    /**
     * Sets the state of the specific step in the workflow.
     * 
     * @param uri
     *            the uri of the workflow step.
     * @param state
     *            the state of the workflow step.
     */
    public void setStepState(String id, State state) {
        this.stepState.put(id, state);
    }

    /**
     * Returns the queued time of the step with the given id, or null if the
     * step has not been queued.
     * 
     * @param id
     *            the id of the step whose time is to be returned.
     * @return the time the step was queued, or null if the step is not
     *         queued.
     */
    public Date getStepQueued(String id) {
        return this.stepQueued.get(id);
    }

    /**
     * Sets the the the queued time of the given step with the id to now().
     * 
     * @param id
     *            the id of the step that has been queued.
     */
    public void setStepQueued(String id) {
        this.stepQueued.put(id, new Date());
    }

    /**
     * Returns the start time of the step with the given id, or null if the step
     * has not been started.
     * 
     * @param id
     *            the id of the step whose time is to be returned.
     * @return the time the step was started, or null if the step is not
     *         started.
     */
    public Date getStepStart(String id) {
        return this.stepStart.get(id);
    }

    /**
     * Sets the the the start time of the given step with the id to now().
     * 
     * @param id
     *            the id of the step that has started execution
     */
    public void setStepStart(String id) {
        this.stepStart.put(id, new Date());
    }

    /**
     * Returns the end time of the step with the given id, or null if the step
     * has not ended.
     * 
     * @param id
     *            the id of the step whose time is to be returned.
     * @return the time the step ended, or null if the step has not ended.
     */
    public Date getStepEnd(String id) {
        return this.stepEnd.get(id);
    }

    /**
     * Sets the the the end time of the given step with the id to now().
     * 
     * @param id
     *            the id of the step that has ended execution
     */
    public void setStepEnd(String id) {
        this.stepEnd.put(id, new Date());
    }
}
