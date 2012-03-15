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
package edu.illinois.ncsa.cyberintegrator.domain;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Person;

@Entity(name = "WorkflowStep")
@Document(collection = "WorkflowStep")
public class WorkflowStep extends AbstractBean {
    /** Used for serialization of object */
    private static final long                  serialVersionUID = 1L;

    /** Title of the workflow step */
    private String                             title            = "";                                          //$NON-NLS-1$

    /** creator of the workflow step */
    @DBRef
    private Person                             creator          = null;

    /** Date the workflow step is created */
    private Date                               createDate       = new Date();

    /** Tool the workflow step is executing */
    @DBRef
    private WorkflowTool                       tool             = null;

    /**
     * List of parameters from the workflow step. This is a map from a specific
     * UUID to a specific WorkflowToolParameter.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowStepOutputs")
    @DBRef
    private Map<String, WorkflowToolParameter> parameters       = new HashMap<String, WorkflowToolParameter>();

    /**
     * List of inputs to the workflow step. This is a map from a specific UUID
     * to a specific WorkflowToolData.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowStepInputs")
    @DBRef
    private Map<String, WorkflowToolData>      inputs           = new HashMap<String, WorkflowToolData>();

    /**
     * List of outputs from the workflow step. This is a map from a specific
     * UUID to a specific WorkflowToolData.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowStepOutputs")
    @DBRef
    private Map<String, WorkflowToolData>      outputs          = new HashMap<String, WorkflowToolData>();

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
     * Return the collection of id's that are inputs to the step.
     * 
     * @return collection of id's that are mapped to an input of the workflow
     *         tool.
     */
    public Collection<String> getInputs() {
        return this.inputs.keySet();
    }

    public WorkflowToolData getInput(String input) {
        return this.inputs.get(input);
    }

    public void setInput(WorkflowToolData data, String input) {
        this.inputs.put(input, data);
    }

    /**
     * Return the collection of id's that are outputs of the step.
     * 
     * @return collection of id's that are mapped to an outputs of the workflow
     *         tool.
     */
    public Collection<String> getOutputs() {
        return this.outputs.keySet();
    }

    public WorkflowToolData getOutput(String output) {
        return this.outputs.get(output);
    }

    public String createOutput(WorkflowToolData data) {
        String output = UUID.randomUUID().toString();
        this.outputs.put(output, data);
        return output;
    }
}