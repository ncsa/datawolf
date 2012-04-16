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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Person;

@Entity(name = "WorkflowStep")
@Document(collection = "WorkflowStep")
public class WorkflowStep extends AbstractBean {
    /** Used for serialization of object */
    private static final long           serialVersionUID = 1L;

    /** Title of the workflow step */
    private String                      title            = "";                                    //$NON-NLS-1$

    /** creator of the workflow step */
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @DBRef
    private Person                      creator          = null;

    /** Date the workflow step is created */
    private Date                        createDate       = new Date();

    /** Tool the workflow step is executing */
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @DBRef
    private WorkflowTool                tool             = null;

    /**
     * List of parameters from the workflow step. This is a map from a specific
     * UUID to a specific WorkflowToolParameter.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowStepParameters")
    @DBRef
    private List<WorkflowToolParameter> parameters       = new ArrayList<WorkflowToolParameter>();

    /**
     * List of inputs to the workflow step. This is a map from a specific UUID
     * to a specific WorkflowToolData.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowStepInputs")
    @DBRef
    private List<WorkflowToolData>      inputs           = new ArrayList<WorkflowToolData>();

    /**
     * List of outputs from the workflow step. This is a map from a specific
     * UUID to a specific WorkflowToolData.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowStepOutputs")
    @DBRef
    private List<WorkflowToolData>      outputs          = new ArrayList<WorkflowToolData>();

    /**
     * Create a new instance of the workflow step.
     */
    public WorkflowStep() {}

    /**
     * Return the title of the workflow step.
     * 
     * @return title of the workflow step.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the workflow step.
     * 
     * @param title
     *            sets the title of the workflow step.
     * 
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the date when the workflow step was created.
     * 
     * @return date the workflow step was created.
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Sets the date when the workflow step was created.
     * 
     * @param date
     *            sets the date when the workflow step was created.
     * 
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
     * Return the tool for the workflow step.
     * 
     * @return tool for the workflow step.
     */
    public WorkflowTool getTool() {
        return tool;
    }

    /**
     * Sets the tool for the workflow step.
     * 
     * @param title
     *            sets the tool for the workflow step.
     * 
     */
    public void setTool(WorkflowTool tool) {
        this.tool = tool;
    }

    /**
     * Return the collection of datasets that are inputs to the step.
     * 
     * @return collection of datasets that represent the inputs to the workflow
     *         tool.
     */
    public List<WorkflowToolData> getInputs() {
        return this.inputs;
    }

    /**
     * Return the input associated with this id
     * 
     * @param id
     *            the id whose input to return
     * @return input associated with the id, null if no match found
     */
    public WorkflowToolData getInput(String id) {
        if ((id == null) || id.equals("")) {
            return null;
        }

        for (WorkflowToolData input : inputs) {
            if (id.equals(input.getDataId())) {
                return input;
            }
        }

        return null;
    }

    /**
     * Set the list of datasets that make up the inputs to the step
     * 
     * @param inputs
     *            the list of datasets to be used as inputs
     */
    public void setInputs(List<WorkflowToolData> inputs) {
        this.inputs.clear();
        if (inputs != null) {
            this.inputs.addAll(inputs);
        }
    }

    /**
     * Add the data to the list of inputs for the workflow step
     * 
     * @param data
     *            data to be added as input
     */
    public void addInput(WorkflowToolData data) {
        this.inputs.add(data);
    }

    /**
     * Remove the data from the list of inputs for the workflow step
     * 
     * @param data
     *            data to be removed
     */
    public void removeInput(WorkflowToolData data) {
        this.inputs.remove(data);
    }

    /**
     * Return the output associated with this id
     * 
     * @param id
     *            the id whose output to return
     * @return output associated with the id, null if not found
     */
    public WorkflowToolData getOutput(String id) {
        if ((id == null) || id.equals("")) {
            return null;
        }

        for (WorkflowToolData output : outputs) {
            if (id.equals(output.getDataId())) {
                return output;
            }
        }

        return null;
    }

    /**
     * Return the collection of id's that are outputs of the step.
     * 
     * @return collection of id's that are mapped to an outputs of the workflow
     *         tool.
     */
    public List<WorkflowToolData> getOutputs() {
        return this.outputs;
    }

    /**
     * Set the list of datasets that make up the outputs to the tool
     * 
     * @param outputs
     *            the list of datasets to be used as outputs
     */
    public void setOutputs(List<WorkflowToolData> outputs) {
        this.outputs.clear();
        if (outputs != null) {
            this.outputs.addAll(outputs);
        }
    }

    /**
     * Add the data to the list of outputs for the step
     * 
     * @param data
     *            the dataset to be added as output
     */
    public void addOutput(WorkflowToolData data) {
        outputs.add(data);
    }

    /**
     * Remove the data from the list of outputs for the step
     * 
     * @param data
     *            the dataset to be removed
     */
    public void removeOutput(WorkflowToolData data) {
        outputs.remove(data);
    }

    /**
     * Return the collection of parameters for the step
     * 
     * @return collection of WorkToolParameter that represents all the
     *         parameters for the step
     * 
     */
    public List<WorkflowToolParameter> getParameters() {
        return this.parameters;
    }

    /**
     * Return the parameter associated with this id
     * 
     * @param id
     *            the id whose parameter to return
     * @return parameter associated with the id, null if not found
     */
    public WorkflowToolParameter getParameter(String id) {
        if ((id == null) || id.equals("")) {
            return null;
        }

        for (WorkflowToolParameter parameter : parameters) {
            if (id.equals(parameter.getParameterId())) {
                return parameter;
            }
        }

        return null;
    }

    /**
     * Sets the collection of parameters for the step
     * 
     * @param parameters
     *            collection of WorkflowToolParameters for the step
     */
    public void setParameters(List<WorkflowToolParameter> parameters) {
        this.parameters.clear();
        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
    }

    /**
     * Add the parameter to the list of parameters for the step
     * 
     * @param parameter
     *            parameter to be added
     */
    public void addParameter(WorkflowToolParameter parameter) {
        this.parameters.add(parameter);
    }

    /**
     * Remove the parameter from the set of parameters for the step
     * 
     * @param parameter
     *            the parameter to be removed
     */
    public void removeParameter(WorkflowToolParameter parameter) {
        this.parameters.remove(parameter);
    }
    /*
     * public String createOutput(WorkflowToolData data) {
     * String output = UUID.randomUUID().toString();
     * this.outputs.put(output, data);
     * return output;
     * }
     */
}
