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
import java.util.Map.Entry;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.jackson.JsonDateSerializer;

@Entity(name = "WorkflowStep")
@Document(collection = "WorkflowStep")
public class WorkflowStep extends AbstractBean {
    /** Used for serialization of object */
    private static final long   serialVersionUID = 1L;

    /** Title of the workflow step */
    private String              title            = "";                           //$NON-NLS-1$

    /** creator of the workflow step */
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @DBRef
    private Person              creator          = null;

    /** Date the workflow step is created */
    @Temporal(TemporalType.TIMESTAMP)
    private Date                createDate       = new Date();

    /** Tool the workflow step is executing */
    @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @DBRef
    private WorkflowTool        tool             = null;

    /**
     * Map of parameters from the workflow step. This maps a specific
     * parameterId to an unique id. This allows the same tool to be used
     * multiple times with unique values.
     */
    @ElementCollection
    @JoinTable(name = "WorkflowStepParameters")
    private Map<String, String> parameters       = new HashMap<String, String>();

    /**
     * Map of inputs to the workflow step. This maps a specific input to an
     * unique id. The unique id points to a specific output by another step.
     * This allows the same tool to be used multiple times with unique values.
     */
    @ElementCollection
    @JoinTable(name = "WorkflowStepInputs")
    private Map<String, String> inputs           = new HashMap<String, String>();

    /**
     * Map of outputs from the workflow step. This maps a specific output to an
     * unique id. The unique id can be used by another step as input. This
     * allows the same tool to be used multiple times with unique values.
     */
    @ElementCollection
    @JoinTable(name = "WorkflowStepOutputs")
    private Map<String, String> outputs          = new HashMap<String, String>();

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
    @JsonSerialize(using = JsonDateSerializer.class)
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
     * Sets the tool for the workflow step. This will also generate a new map
     * that uniquely defines the outputs of this step.
     * 
     * @param t
     *            sets the tool for the workflow step.
     */
    public void setTool(WorkflowTool tool) {
        this.tool = tool;

        // create a new input/output table
        this.inputs.clear();
        this.outputs.clear();
        this.parameters.clear();
        for (WorkflowToolData output : tool.getOutputs()) {
            this.outputs.put(output.getDataId(), UUID.randomUUID().toString());
        }
        for (WorkflowToolParameter param : tool.getParameters()) {
            this.parameters.put(param.getParameterId(), UUID.randomUUID().toString());
        }
    }

    /**
     * Returns a map of outputs from the workflow step. This maps a specific
     * output to an unique id. The unique id can be used by another step as
     * input. This allows the same tool to be used multiple times with unique
     * values.
     * 
     * @return the mapping from workflow tool output dataId to an unique id.
     */
    public Map<String, String> getOutputs() {
        return outputs;
    }

    /**
     * Returns a map of id of step's output to the tool's data definition.
     * 
     * @return map of id of step's output to the tool's data definition.
     */
    public Map<String, WorkflowToolData> getOutputsToolData() {
        Map<String, WorkflowToolData> result = new HashMap<String, WorkflowToolData>();
        for (Entry<String, String> entry : outputs.entrySet()) {
            result.put(entry.getKey(), tool.getOutput(entry.getValue()));
        }
        return result;
    }

    /**
     * Returns a specific output. The output returned is based on the unique id
     * generated for the outputs of the tool for this step.
     * 
     * @param id
     *            the unique id for this step for the outputs generated by the
     *            tool.
     * @return the output data definition for this tool.
     * @throws IllegalArgumentException
     *             throws IllegalArgumentException if the output is not
     *             found.
     */
    public WorkflowToolData getOutput(String id) {
        for (Entry<String, String> entry : outputs.entrySet()) {
            if (entry.getValue().equals(id)) {
                return tool.getOutput(entry.getKey());
            }
        }
        throw (new IllegalArgumentException("No output with [" + id + "] known to tool."));
    }

    /**
     * Returns a map of inputs from the workflow step. This maps a specific
     * input to an unique id. The unique id is generated by another step in the
     * workflow and points to a dataset. This allows the same tool to be used
     * multiple times with unique values.
     * 
     * @return the mapping from tool inputs to unique id's generated by other
     *         steps.
     */
    public Map<String, String> getInputs() {
        return inputs;
    }

    /**
     * Returns a map of id of step's input to the tool's data definition.
     * 
     * @return map of id of step's input to the tool's data definition.
     */
    public Map<String, WorkflowToolData> getInputsToolData() {
        Map<String, WorkflowToolData> result = new HashMap<String, WorkflowToolData>();
        for (Entry<String, String> entry : inputs.entrySet()) {
            result.put(entry.getKey(), tool.getOutput(entry.getValue()));
        }
        return result;
    }

    /**
     * Returns a specific input. The input returned is based on the unique id
     * generated for the outputs of the tool for another step.
     * 
     * @param id
     *            the unique id for another step for the outputs generated by
     *            the tool.
     * @return the input data definition for this tool.
     * @throws IllegalArgumentException
     *             throws IllegalArgumentException if the input is not
     *             found.
     */
    public WorkflowToolData getInput(String id) {
        for (Entry<String, String> entry : inputs.entrySet()) {
            if (entry.getValue().equals(id)) {
                return tool.getInput(entry.getKey());
            }
        }
        throw (new IllegalArgumentException("No input with [" + id + "] known to tool."));
    }

    /**
     * Maps the input of the tool to a specific output generated in another
     * step.
     * 
     * @param input
     *            the input of the tool that will be mapped.
     * @param outputid
     *            the unique id generated as output by another step.
     */
    public void setInput(WorkflowToolData input, String outputid) {
        inputs.put(input.getDataId(), outputid);
    }

    /**
     * Maps the input of the tool to a specific output generated in another
     * step.
     * 
     * @param input
     *            the input of the tool that will be mapped.
     * @param outputid
     *            the unique id generated as output by another step.
     */
    public void setInput(String inputid, String outputid) {
        if (tool.getInput(inputid) == null) {
            throw (new IllegalArgumentException("No input with [" + inputid + "] known to tool."));
        }
        inputs.put(inputid, outputid);
    }

    /**
     * Returns a map of parameters from the workflow step. This maps a specific
     * parameter to an unique id. This allows the same tool to be used multiple
     * times with unique values.
     * 
     * @return the mapping from tool parameter to unique id's.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Returns a specific parameter. The parameter returned is based on the
     * unique id generated for the parameters of this step.
     * 
     * @param id
     *            the unique id for this step for the parameter generated by
     *            the tool.
     * @return the parameter data definition for this tool.
     * @throws IllegalArgumentException
     *             throws IllegalArgumentException if the parameter is not
     *             found.
     */
    public WorkflowToolParameter getParameter(String id) {
        for (Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getValue().equals(id)) {
                return tool.getParameter(entry.getKey());
            }
        }
        throw (new IllegalArgumentException("No parameter with [" + id + "] known to tool."));
    }
}
