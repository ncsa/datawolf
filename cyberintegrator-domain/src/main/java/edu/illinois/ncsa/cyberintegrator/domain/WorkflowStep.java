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

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Person;

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
    private Date                createDate       = new Date();

    /** Tool the workflow step is executing */
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @DBRef
    private WorkflowTool        tool             = null;

    /**
     * List of parameters from the workflow step. This maps the id of the
     * parameter to a specific value. The id of the parameter refers to the id
     * of the parameter in the tool.
     */
    @ElementCollection
    @JoinTable(name = "WorkflowStepParameters")
    private Map<String, String> parameters       = new HashMap<String, String>();

    /**
     * List of inputs to the workflow step. This maps the id of a workflow tool
     * input to the id of a specific output generated in the wokflow.
     */
    @ElementCollection
    @JoinTable(name = "WorkflowStepInputs")
    private Map<String, String> inputs           = new HashMap<String, String>();

    /**
     * List of outputs from the workflow step. This maps a random id to a
     * specific output generated by the workflow tool.
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
        for (WorkflowToolData output : tool.getOutputs()) {
            this.outputs.put(UUID.randomUUID().toString(), output.getDataId());
        }
    }

    /**
     * Returns a map of outputs. This will return a map that has unique id's for
     * each of the outputs generated by the tool associated with this step.
     * 
     * @return the mapping from id to workflow tool output.
     */
    public Map<String, WorkflowToolData> getOutputs() {
        Map<String, WorkflowToolData> result = new HashMap<String, WorkflowToolData>();
        for (Entry<String, String> entry : outputs.entrySet()) {
            // result.put(tool.getInput(entry.getValue()), entry.getKey());
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
     */
    public WorkflowToolData getOutput(String id) {
        if (outputs.containsKey(id)) {
            return tool.getOutput(outputs.get(id));
        } else {
            throw (new IllegalArgumentException(String.format("No parameter with [%s] known to tool.", id)));
        }
    }

    /**
     * Returns a map of inputs. This will return a map of inputs and the unique
     * id's generated by other tools in other steps.
     * 
     * @return the mapping from tool inputs to unique id's generated by other
     *         steps.
     */
    public Map<WorkflowToolData, String> getInputs() {
        Map<WorkflowToolData, String> result = new HashMap<WorkflowToolData, String>();
        for (Entry<String, String> entry : inputs.entrySet()) {
            result.put(tool.getInput(entry.getKey()), entry.getValue());
        }
        return result;
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
     * Sets a value for the specific parameter.
     * 
     * @param param
     *            the parameter whose value to set.
     * @param value
     *            the value for the parameter
     * @throws IllegalArgumentException
     *             throws IllegalArgumentException if the parameter is not
     *             found.
     */
    public void setParameter(WorkflowToolParameter param, String value) {
        setParameter(param.getParameterId(), value);
    }

    /**
     * Sets a value for the specific parameter.
     * 
     * @param id
     *            the parameter id (see WorkflowToolParameter.getParameterId()).
     * @param value
     *            the value for the parameter
     * @throws IllegalArgumentException
     *             throws IllegalArgumentException if the parameter is not
     *             found.
     */
    public void setParameter(String id, String value) throws IllegalArgumentException {
        if (tool.getParameter(id) == null) {
            throw (new IllegalArgumentException(String.format("No parameter with [%s] known to tool.", id)));
        }
        parameters.put(id, value);
    }

    /**
     * Returns the value of the parameter. This either returns the value
     * specifically set, or the default value for the tool.
     * 
     * @param id
     *            the parameter id (see WorkflowToolParameter.getParameterId()).
     * @return the parameter value.
     * @throws IllegalArgumentException
     *             throws IllegalArgumentException if the parameter is not
     *             found.
     */
    public String getParameter(String id) throws IllegalArgumentException {
        if (parameters.containsKey(id)) {
            return parameters.get(id);
        }
        WorkflowToolParameter param = tool.getParameter(id);
        if (param != null) {
            return param.getValue();
        }
        throw (new IllegalArgumentException(String.format("No parameter with [%s] known to tool.", id)));
    }
}
