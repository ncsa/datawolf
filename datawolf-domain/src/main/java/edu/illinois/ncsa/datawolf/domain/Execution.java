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
package edu.illinois.ncsa.datawolf.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.jackson.JsonDateSerializer;

public class Execution extends AbstractBean {

    public enum State {
        WAITING, QUEUED, RUNNING, FINISHED, ABORTED, FAILED, UNKNOWN
    }

    /** Used for serialization of object */
    private static final long   serialVersionUID = 1L;

    /** Marker for non existing dataset due to errrors. */
    public static final String  EMPTY_DATASET    = "ERROR";

    /** Title of the execution */
    private String              title            = "";                           //$NON-NLS-1$

    /** Description of the execution */
    private String              description      = "";                           //$NON-NLS-1$

    /** Workflow that is executed */
    private String              workflowId       = null;

    /** Date the execution is created */
    private Date                date             = new Date();

    /** creator of the execution */
    private Person              creator          = null;

    /** maping a parameter to a specific parameter in the workflow */
    private Map<String, String> parameters       = new HashMap<String, String>();

    /** maping a dataset to a specific id of a dataset in the workflow */
    private Map<String, String> datasets         = new HashMap<String, String>();

    /** the state of each step executed */
    private Map<String, State>  stepState        = new HashMap<String, State>();

    /** the start date of each step queued */
    private Map<String, Long>   stepsQueued      = new HashMap<String, Long>();

    /** the start date of each step executed */
    private Map<String, Long>   stepsStart       = new HashMap<String, Long>();

    /** the end date of each step executed */
    private Map<String, Long>   stepsEnd         = new HashMap<String, Long>();

    /** a collection of properties to store information about the execution */
    private Map<String, String> properties       = new HashMap<String, String>();

    /**
     * Create a new instance of the execution.
     */
    public Execution() {}

    /**
     * Return the title of the execution.
     * 
     * @return title of the execution
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the execution
     * 
     * @param title
     *            sets the title of the execution.
     * 
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the description of the execution.
     * 
     * @return description of the execution
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the execution
     * 
     * @param description
     *            sets the description of the execution.
     * 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the workflow that was executed.
     * 
     * @return workflow that was executed.
     */
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * Sets the workflow that is executed. This will reset all steps to be in
     * WAITING state, and thus all times will be erased.
     * 
     * @param workflow
     *            sets the workflow this execution is for.
     * 
     */
    public void setWorkflow(Workflow workflow) {
        this.workflowId = workflow.getId();
        this.stepState.clear();
        this.stepsQueued.clear();
        this.stepsStart.clear();
        this.stepsEnd.clear();
        for (WorkflowStep step : workflow.getSteps()) {
            setStepState(step.getId(), State.WAITING);
        }
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
     * 
     * @param uuid
     *            the uuid for parameter
     * @return the value of the parameter.
     */
    public String getParameter(String uuid) {
        // note if the parameter is not specified the executor should use the
        // default value from the tool.
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

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
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
     * @param id
     *            the uuid for dataset
     * @return the id of the dataset.
     */
    public String getDataset(String id) {
        return this.datasets.get(id);
    }

    /**
     * Sets the dataset associated with that specific uuid.
     * 
     * @param id
     *            the uuid of the dataset to set.
     * @param datasetid
     *            the id of the dataset to be set.
     */
    public void setDataset(String id, String datasetid) {
        this.datasets.put(id, datasetid);
    }

    public void setDatasets(Map<String, String> datasets) {
        this.datasets = datasets;
    }

    public Map<String, String> getDatasets() {
        return this.datasets;
    }

    /**
     * Returns the state for a specific step.
     * 
     * @param id
     *            the id for the step in the workflow
     * @return the state of the step.
     */
    public State getStepState(String id) {
        return this.stepState.get(id);
    }

    public Map<String, State> getStepState() {
        return this.stepState;
    }

    /**
     * Sets the state of the specific step in the workflow.
     * 
     * @param id
     *            the id of the workflow step.
     * @param state
     *            the state of the workflow step.
     */
    public void setStepState(String id, State state) {
        this.stepState.put(id, state);
    }

    public void setStepState(Map<String, State> stepStates) {
        this.stepState = stepStates;
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
        if (this.stepsQueued.containsKey(id)) {
            return new Date(this.stepsQueued.get(id));
        } else {
            return null;
        }
    }

    /**
     * Sets the the the queued time of the given step with the id to now().
     * 
     * @param id
     *            the id of the step that has been queued.
     */
    public void setStepQueued(String id) {
        setStepQueued(id, System.currentTimeMillis());
    }

    public void setStepQueued(String id, Long queued) {
        this.stepsQueued.put(id, queued);
    }

    public Map<String, Long> getStepsQueued() {
        return this.stepsQueued;
    }

    public void setStepsQueued(Map<String, Long> stepQueued) {
        this.stepsQueued = stepQueued;
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
        if (this.stepsStart.containsKey(id)) {
            return new Date(this.stepsStart.get(id));
        } else {
            return null;
        }
    }

    /**
     * Sets the the the start time of the given step with the id to now().
     * 
     * @param id
     *            the id of the step that has started execution
     */
    @JsonIgnore
    public void setStepStart(String id) {
        setStepStart(id, System.currentTimeMillis());
    }

    public void setStepStart(String id, Long start) {
        this.stepsStart.put(id, start);
    }

    public Map<String, Long> getStepsStart() {
        return this.stepsStart;
    }

    public void setStepsStart(Map<String, Long> stepsStart) {
        this.stepsStart = stepsStart;
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
        if (this.stepsEnd.containsKey(id)) {
            return new Date(this.stepsEnd.get(id));
        } else {
            return null;
        }
    }

    /**
     * Sets the the the end time of the given step with the id to now().
     * 
     * @param id
     *            the id of the step that has ended execution
     */
    public void setStepEnd(String id) {
        setStepEnd(id, System.currentTimeMillis());
    }

    public void setStepEnd(String id, Long end) {
        this.stepsEnd.put(id, end);
    }

    public Map<String, Long> getStepsEnd() {
        return this.stepsEnd;
    }

    public void setStepsEnd(Map<String, Long> stepsEnd) {
        this.stepsEnd = stepsEnd;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String val) {
        this.properties.put(key, val);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
