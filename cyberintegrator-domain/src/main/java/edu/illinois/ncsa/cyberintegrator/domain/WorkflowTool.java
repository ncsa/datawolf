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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.jackson.JsonDateSerializer;

@Entity(name = "WorkflowTool")
@Document(collection = "WorkflowTool")
public class WorkflowTool extends AbstractBean implements Serializable {
    /** Used for serialization of object */
    private static final long           serialVersionUID = 1L;

    /** Title of the workflow tool */
    private String                      title            = "";                                    //$NON-NLS-1$

    /** Description of the workflow tool */
    private String                      description      = "";                                    //$NON-NLS-1$

    /** Version of the workflow tool */
    private String                      version          = "1";                                   //$NON-NLS-1$

    /** Previous version of the workflow tool */
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @DBRef
    private WorkflowTool                previousVersion  = null;

    /** Date the workflow tool is created */
    @Temporal(TemporalType.TIMESTAMP)
    private Date                        date             = new Date();

    /** implementation, this is executor specific. */
    @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @DBRef
    private WorkflowToolImplementation  implementation   = null;

    /** creator of the workflow tool */
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @DBRef
    private Person                      creator          = null;

    /** List of contributors to the workflow tool. */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowToolContributors")
    @DBRef
    private Set<Person>                 contributors     = new HashSet<Person>();

    /** List of inputs to the workflow tool. */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowToolInputs")
    @DBRef
    private List<WorkflowToolData>      inputs           = new ArrayList<WorkflowToolData>();

    /** List of outputs to the workflow tool. */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowToolOutputs")
    @DBRef
    private List<WorkflowToolData>      outputs          = new ArrayList<WorkflowToolData>();

    /** List of parameters to the workflow tool. */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "WorkflowToolParameters")
    @DBRef
    private List<WorkflowToolParameter> parameters       = new ArrayList<WorkflowToolParameter>();

    /** Executor of the tool. */
    private String                      executor         = null;

    /** all blobs associated with this tool */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ToolBlobs")
    @DBRef
    private Set<FileDescriptor>         blobs            = null;

    /**
     * Create a new instance of the workflow tool.
     */
    public WorkflowTool() {}

    /**
     * Return the title of the workflow tool.
     * 
     * @return title of the workflow tool
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the workflow tool
     * 
     * @param title
     *            sets the title of the workflow tool.
     * 
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the description of the workflow tool.
     * 
     * @return description of the workflow tool
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the workflow tool
     * 
     * @param description
     *            sets the description of the workflow tool
     * 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the date when the workflow tool was created.
     * 
     * @return date the workflow tool was created.
     */
    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date when the workflow tool was created.
     * 
     * @param date
     *            sets the date when the workflow tool was created.
     * 
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Return the implementation of the tool
     * 
     * @return implementation of the tool.
     */
    public WorkflowToolImplementation getImplementation() {
        return implementation;
    }

    /**
     * Sets the implementation of the tool.
     * 
     * @param implementation
     *            sets the implementation of the tool.
     * 
     */
    public void setImplementation(WorkflowToolImplementation implementation) {
        this.implementation = implementation;
    }

    /**
     * Return the executor of the tool
     * 
     * @return executor of the tool.
     */
    public String getExecutor() {
        return executor;
    }

    /**
     * Sets the executor of the tool.
     * 
     * @param executor
     *            sets the executor of the tool.
     * 
     */
    public void setExecutor(String executor) {
        this.executor = executor;
    }

    /**
     * Return the version of the workflow tool.
     * 
     * @return version of the workflow tool.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the workflow tool.
     * 
     * @param version
     *            sets the version of the workflow tool.
     * 
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Return the previous version of the workflow tool.
     * 
     * @return previous version of the workflow tool.
     */
    public WorkflowTool getPreviousVersion() {
        return previousVersion;
    }

    /**
     * Sets the previous version of the workflow tool.
     * 
     * @param previousVersion
     *            sets the previous version of the workflow tool.
     * 
     */
    public void setPreviousVersion(WorkflowTool previousVersion) {
        this.previousVersion = previousVersion;
    }

    /**
     * Return the PersonBean that is the creator of the workflow tool
     * 
     * @return PersonBean that represents the creator
     */
    public Person getCreator() {
        return creator;
    }

    /**
     * Sets the PersonBean that represents the creator of the workflow tool.
     * 
     * @param creator
     *            sets the PersonBeans that represents the creator of the
     *            workflow tool.
     * 
     */
    public void setCreator(Person creator) {
        this.creator = creator;
    }

    /**
     * Return the set of PersonBeans that represents those that are contributors
     * to the workflow tool.
     * 
     * @return set of PersonBeans that represents all the users that have
     *         contributes to the workflow tool.
     */
    public Set<Person> getContributors() {
        return this.contributors;
    }

    /**
     * Set the set of PersonBeans that represents those that contributed to the
     * workflow tool.
     * 
     * @param contributors
     *            the set of contributors to the workflow tool.
     */
    public void setContributors(Collection<Person> contributors) {
        this.contributors.clear();
        if (contributors != null) {
            this.contributors.addAll(contributors);
        }
    }

    /**
     * Add the contributor to the set of contributors to the workflow tool.
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
     * Remove the contributor from the set of contributors to the worflow tool.
     * 
     * @param contributor
     *            the PersonBean of the contributor to be removed.
     */
    public void removeContributor(Person contributor) {
        this.contributors.remove(contributor);
    }

    /**
     * Return the collection of datasets that are inputs to the tool.
     * 
     * @return collection of DatasetListBean that represents all the inputs to
     *         the workflow tool.
     */
    public List<WorkflowToolData> getInputs() {
        return this.inputs;
    }

    /**
     * Set the list of datasets that make up the inputs to the tool.
     * 
     * @param inputs
     *            the list of datasets to be used as inputs.
     */
    public void setInputs(List<WorkflowToolData> inputs) {
        this.inputs.clear();
        if (inputs != null) {
            this.inputs.addAll(inputs);
        }
    }

    /**
     * Return the input associated with this id.
     * 
     * @param id
     *            the dataId whose input to return.
     * @return input associated with the dataId.
     */
    public WorkflowToolData getInput(String id) {
        if ((id == null) || id.equals("")) { //$NON-NLS-1$
            return null;
        }

        for (WorkflowToolData dlb : inputs) {
            if (id.equals(dlb.getDataId())) {
                return dlb;
            }
        }

        return null;
    }

    /**
     * Add the data to list of inputs to the workflow tool.
     * 
     * @param data
     *            the data to be added as input.
     */
    public void addInput(WorkflowToolData data) {
        inputs.add(data);
    }

    /**
     * Remove the data from list of inputs to the workflow tool.
     * 
     * @param data
     *            the data to be removed as input.
     */
    public void removeInput(WorkflowToolData data) {
        inputs.remove(data);
    }

    /**
     * Return the collection of datasets that are outputs to the tool.
     * 
     * @return collection of beans that represents all the outputs to the
     *         workflow tool.
     */
    public List<WorkflowToolData> getOutputs() {
        return this.outputs;
    }

    /**
     * Set the list of datasets that make up the outputs to the tool.
     * 
     * @param outputs
     *            the list of datasets to be used as outputs.
     */
    public void setOutputs(List<WorkflowToolData> outputs) {
        this.outputs.clear();
        if (outputs != null) {
            this.outputs.addAll(outputs);
        }
    }

    /**
     * Return the output associated with this id.
     * 
     * @param id
     *            the id whose output to return.
     * @return output associated with the id.
     */
    public WorkflowToolData getOutput(String id) {
        if ((id == null) || id.equals("")) { //$NON-NLS-1$
            return null;
        }

        for (WorkflowToolData dlb : outputs) {
            if (id.equals(dlb.getDataId())) {
                return dlb;
            }
        }

        return null;
    }

    /**
     * Add the data to list of outputs to the workflow tool.
     * 
     * @param data
     *            the DatasetBean to be added as output.
     */
    public void addOutput(WorkflowToolData data) {
        outputs.add(data);
    }

    /**
     * Remove the data from list of outputs to the workflow tool.
     * 
     * @param data
     *            the DatasetBean to be removed as output.
     */
    public void removeOutput(WorkflowToolData data) {
        outputs.remove(data);
    }

    /**
     * Return the collection of parameters of the tool.
     * 
     * @return collection of WorkflowParameterBean that represents all the
     *         parameters to the workflow tool.
     */
    public List<WorkflowToolParameter> getParameters() {
        return this.parameters;
    }

    /**
     * Return the parameter associated with this id.
     * 
     * @param id
     *            the id whose parameter to return.
     * @return parameter associated with the id.
     */
    public WorkflowToolParameter getParameter(String id) {
        if ((id == null) || id.equals("")) { //$NON-NLS-1$
            return null;
        }

        for (WorkflowToolParameter wpb : parameters) {
            if (id.equals(wpb.getParameterId())) {
                return wpb;
            }
        }

        return null;
    }

    /**
     * Sets the collection of parameters of the tool.
     * 
     * @param parameters
     *            collection of WorkflowParameterBean that represents all the
     *            parameters to the workflow tool.
     */
    public void setParameters(List<WorkflowToolParameter> parameters) {
        this.parameters.clear();
        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
    }

    /**
     * Add the parameter from the set of parameters to the workflow tool.
     * 
     * @param parameter
     *            the parameter to be added.
     */
    public void addParameter(WorkflowToolParameter parameter) {
        this.parameters.add(parameter);
    }

    /**
     * Remove the parameter from the set of parameters to the workflow tool.
     * 
     * @param parameter
     *            the parameter to be removed.
     */
    public void removeParameter(WorkflowToolParameter parameter) {
        this.parameters.remove(parameter);
    }

    /**
     * Return the set of blobs associated with the workflow too.
     * 
     * @return set of blob associated with the workflow tool.
     */
    public Set<FileDescriptor> getBlobs() {
        if (blobs == null) {
            blobs = new HashSet<FileDescriptor>();
        }
        return blobs;
    }

    /**
     * Set the set of blobs associated with the workflow tool.
     * 
     * @param blobs
     *            the set of blobs to the workflow tool.
     */
    public void setBlobs(Collection<FileDescriptor> blobs) {
        getBlobs().clear();
        if (blobs != null) {
            getBlobs().addAll(blobs);
        }
    }

    /**
     * Add the blob to the set of blobs to the workflow tool.
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
     * Remove the blob from the set of blobs of the worflow tool.
     * 
     * @param blob
     *            the blob to be removed.
     */
    public void removeBlob(FileDescriptor blob) {
        getBlobs().remove(blob);
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
