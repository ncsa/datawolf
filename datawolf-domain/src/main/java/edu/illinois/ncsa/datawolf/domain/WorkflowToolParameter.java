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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.illinois.ncsa.domain.AbstractBean;

public class WorkflowToolParameter extends AbstractBean {
    public enum ParameterType {
        BOOLEAN, STRING, NUMBER, OPTION; // , FILE, SELECTION, GEOLOCATION;
    }

    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** Id of the workflow parameter used to match the step and tool */
    private String            paramid          = UUID.randomUUID().toString();

    /** Title of the workflow parameter */
    private String            title            = "";                          //$NON-NLS-1$

    /** Description of the workflow parameter */
    private String            description      = "";                          //$NON-NLS-1$

    /** Type of the workflow parameter */
    private ParameterType     type             = ParameterType.STRING;

    /** Value of the workflow parameter */
    private String            value            = "";                          //$NON-NLS-1$

    /** Should the parameter be shown */
    private boolean           hidden           = false;

    /** Can the parameter be null */
    private boolean           allowNull        = false;

    /** List of options */
    private List<String>      options          = new ArrayList<String>();

    /**
     * Create a new instance of the workflow parameter.
     */
    public WorkflowToolParameter() {
        setId(UUID.randomUUID().toString());
    }

    /**
     * Return the id of the workflow parameter.
     * 
     * @return id of the workflow parameter.
     */
    public String getParameterId() {
        return paramid;
    }

    /**
     * Sets the id of the workflow parameter.
     * 
     * @param title
     *            sets the id of the workflow parameter.
     * 
     */
    public void setParameterId(String paramid) {
        this.paramid = paramid;
    }

    /**
     * Return the title of the workflow parameter.
     * 
     * @return title of the workflow parameter.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the workflow parameter.
     * 
     * @param title
     *            sets the title of the workflow parameter.
     * 
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the description of the workflow parameter.
     * 
     * @return description of the workflow parameter.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the workflow parameter.
     * 
     * @param description
     *            sets the description of the workflow parameter.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the type of the workflow parameter.
     * 
     * @return type of the workflow parameter.
     */
    public ParameterType getType() {
        return type;
    }

    /**
     * Sets the type of the workflow parameter.
     * 
     * @param type
     *            sets the type of the workflow parameter.
     */
    @JsonProperty("type")
    public void setType(ParameterType type) {
        this.type = type;
    }

    /**
     * Sets the type of the workflow parameter.
     * 
     * @param type
     *            sets the type of the workflow parameter.
     * @deprecated see setType(ParameterType type)
     */
    @JsonIgnore
    @Deprecated
    public void setType(String type) {
        try {
            setType(ParameterType.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            setType(ParameterType.STRING);
        }
    }

    /**
     * Return the value of the workflow parameter.
     * 
     * @return value of the workflow parameter.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the workflow parameter.
     * 
     * @param value
     *            sets the value of the workflow parameter.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns true if the workflow parameter should not be shown to the user.
     * 
     * @return true if the workflow parameter should not be shown.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Sets whether or not the workflow parameter should be shown to the user.
     * 
     * @param hidden
     *            if set to true the workflow parameter will not be shown to the
     *            user.
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Return true if the parameter is allowed to be empty.
     * 
     * @return the true if the parameter can be empty.
     */
    public boolean isAllowNull() {
        return allowNull;
    }

    /**
     * Sets whether or not the workflow parameter can be empty.
     * 
     * @param allowNull
     *            the allowNull to set
     */
    public void setAllowNull(boolean allowNull) {
        this.allowNull = allowNull;
    }

    /**
     * Return the list of options for this parameter.
     * 
     * @return the list of options this parameter can be.
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * Sets the list of options this parameter can be.
     * 
     * @param options
     *            the list of options for this parameter.
     */
    public void setOptions(List<String> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return getTitle() + "[" + getType() + "] = " + getValue();
    }
}
