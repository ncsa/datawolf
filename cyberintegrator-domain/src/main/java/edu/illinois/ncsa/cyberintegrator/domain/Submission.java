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

import java.util.HashMap;
import java.util.Map;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * Used for submission of a workflow to the cyberintegrator service. This is all
 * the information needed to be able to execute a workflow.
 * 
 * @author Rob Kooper
 * @author Luigi Marini
 */
public class Submission extends AbstractBean {
    /** Used for serialization of object */
    private static final long   serialVersionUID = 1L;

    /** Title of the execution */
    private String              title            = "";                           //$NON-NLS-1$

    /** Description of the execution */
    private String              description      = "";                           //$NON-NLS-1$

    /** Workflow that is executed */
    private String              workflowId       = null;

    /** creator of the execution */
    private String              creatorId        = null;

    /** maping a parameter to a specific parameter in the workflow */
    private Map<String, String> parameters       = new HashMap<String, String>();

    /** maping a dataset to a specific dataset in the workflow */
    private Map<String, String> datasets         = new HashMap<String, String>();

    /**
     * Create a new instance of the execution.
     */
    public Submission() {}

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

    /**
     * Sets the workflow that is executed.
     * 
     * @param workflow
     *            sets the workflow this execution is for.
     * 
     */
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * Return the id of the creator of the execution
     * 
     * @return the id of the PersonBean that represents the creator
     */
    public String getCreatorId() {
        return creatorId;
    }

    /**
     * Sets the the id that represents the creator of the execution.
     * 
     * @param creator
     *            sets the the id of the PersonBean that represents the creator
     *            of the execution.
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * @param id
     * @return
     */
    public boolean hasParameter(String id) {
        return this.parameters.containsKey(id);
    }

    /**
     * Returs the map of all parameters.
     * 
     * @return map of all parameters.
     */
    public Map<String, String> getParameters() {
        return parameters;
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
     * Returns a map fof all datasets to be used as inputs.
     * 
     * @return map of inputs.
     */
    public Map<String, String> getDatasets() {
        return datasets;
    }

    /**
     * Returns the value for a specific dataset.
     * 
     * @param uuid
     *            the uuid for dataset
     * @return the dataset.
     */
    public String getDataset(String id) {
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
    public void setDataset(String id, String datasetId) {
        this.datasets.put(id, datasetId);
    }
}
