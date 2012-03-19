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
package edu.illinois.ncsa.cyberintegrator.executor.java.tool;

/**
 * Definition of a parameter. This class is used to return a parameter needed by
 * the tool.
 * 
 * @author Rob Kooper
 * 
 */
public class Parameter {
    public enum ParameterType {
        BOOLEAN, STRING, NUMBER, OPTION
    }

    private String        id;
    private String        name;
    private String        description;
    private ParameterType type;
    private String        value;
    private String[]      options;
    private boolean       hidden;
    private boolean       allowEmpty;

    /**
     * Define a parameter that will be shown to the user of the tool.
     * 
     * @param id
     *            the id of the parameter, used when setting the parameter.
     * @param name
     *            the name of the parameter, shown to the user.
     * @param description
     *            description of the parameter, shown as help to the user.
     * @param type
     *            type of the parameter, string for example.
     * @param value
     *            the default value of the parameter.
     */
    public Parameter(String id, String name, String description, ParameterType type, String value) {
        this(id, name, description, type, value, false, false, new String[0]);
    }

    /**
     * Define a parameter that will be shown to the user of the tool, if hidden
     * is not set to true.
     * 
     * @param id
     *            the id of the parameter, used when setting the parameter.
     * @param name
     *            the name of the parameter, shown to the user.
     * @param description
     *            description of the parameter, shown as help to the user.
     * @param type
     *            type of the parameter, string for example.
     * @param value
     *            the default value of the parameter.
     * @param hidden
     *            if true don't show the parameter to the user.
     */
    public Parameter(String id, String name, String description, ParameterType type, String value, boolean hidden) {
        this(id, name, description, type, value, hidden, false, new String[0]);
    }

    /**
     * Define a parameter that will be shown to the user of the tool, if hidden
     * is not set to true.
     * 
     * @param id
     *            the id of the parameter, used when setting the parameter.
     * @param name
     *            the name of the parameter, shown to the user.
     * @param description
     *            description of the parameter, shown as help to the user.
     * @param type
     *            type of the parameter, string for example.
     * @param value
     *            the default value of the parameter.
     * @param hidden
     *            if true don't show the parameter to the user.
     * @param allowEmpty
     *            if true the parameter can be empty.
     */
    public Parameter(String id, String name, String description, ParameterType type, String value, boolean hidden, boolean allowEmpty) {
        this(id, name, description, type, value, hidden, allowEmpty, new String[0]);
    }

    /**
     * Define a parameter that will be shown to the user of the tool. This will
     * be an parameter of type OPTION.
     * 
     * @param id
     *            the id of the parameter, used when setting the parameter.
     * @param name
     *            the name of the parameter, shown to the user.
     * @param description
     *            description of the parameter, shown as help to the user.
     * @param value
     *            the default value of the parameter.
     * @param options
     *            possible values to select from.
     */
    public Parameter(String id, String name, String description, String value, String... options) {
        this(id, name, description, ParameterType.OPTION, value, false, false, options);
    }

    /**
     * Define a parameter that will be shown to the user of the tool, if hidden
     * is not set to true. This will be an parameter of type OPTION.
     * 
     * @param id
     *            the id of the parameter, used when setting the parameter.
     * @param name
     *            the name of the parameter, shown to the user.
     * @param description
     *            description of the parameter, shown as help to the user.
     * @param value
     *            the default value of the parameter.
     * @param hidden
     *            if true don't show the parameter to the user.
     * @param options
     *            possible values to select from.
     */
    public Parameter(String id, String name, String description, String value, boolean hidden, String... options) {
        this(id, name, description, ParameterType.OPTION, value, hidden, false, options);
    }

    /**
     * Define a parameter that will be shown to the user of the tool, if hidden
     * is not set to true. This will be an parameter of type OPTION.
     * 
     * @param id
     *            the id of the parameter, used when setting the parameter.
     * @param name
     *            the name of the parameter, shown to the user.
     * @param description
     *            description of the parameter, shown as help to the user.
     * @param value
     *            the default value of the parameter.
     * @param hidden
     *            if true don't show the parameter to the user.
     * @param allowEmpty
     *            if true the parameter can be empty.
     * @param options
     *            possible values to select from.
     */
    public Parameter(String id, String name, String description, String value, boolean hidden, boolean allowEmpty, String... options) {
        this(id, name, description, ParameterType.OPTION, value, hidden, allowEmpty, options);
    }

    /**
     * Define a parameter that will be shown to the user of the tool, if hidden
     * is not set to true.
     * 
     * @param id
     *            the id of the parameter, used when setting the parameter.
     * @param name
     *            the name of the parameter, shown to the user.
     * @param description
     *            description of the parameter, shown as help to the user.
     * @param type
     *            type of the parameter, string for example.
     * @param value
     *            the default value of the parameter.
     * @param hidden
     *            if true don't show the parameter to the user.
     * @param allowEmpty
     *            if true the parameter can be empty.
     */
    private Parameter(String id, String name, String description, ParameterType type, String value, boolean hidden, boolean allowEmpty, String... options) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
        this.options = options;
        this.hidden = hidden;
        this.allowEmpty = allowEmpty;
    }

    /**
     * Return the id of the parameter.
     * 
     * @return id of the parameter.
     */
    public String getID() {
        return id;
    }

    /**
     * Set the id of the parameter.
     * 
     * @param id
     *            of the parameter.
     */
    public void setID(String id) {
        this.id = id.trim();
    }

    /**
     * Return the name of the paraemter.
     * 
     * @return name of the parameter.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the parameter.
     * 
     * @param name
     *            the new name of the parameter.
     */
    public void setName(String name) {
        this.name = name.trim();
    }

    /**
     * Return the description of the parameter.
     * 
     * @return description of the parameter.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the parameter.
     * 
     * @param description
     *            the new description of the parameter.
     */
    public void setDescription(String description) {
        this.description = description.trim();
    }

    /**
     * Return the type of the parameter.
     * 
     * @return type of the parameter.
     */
    public ParameterType getType() {
        return type;
    }

    /**
     * Set the type of the parameter.
     * 
     * @param type
     *            the new type of the parameter.
     */
    public void setType(ParameterType type) {
        this.type = type;
    }

    /**
     * Return the value of the parameter.
     * 
     * @return value of the parameter.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value of the parameter.
     * 
     * @param value
     *            the new value of the parameter.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns true if the parameter should not be shown to the user.
     * 
     * @return true if the parameter is hidden.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Set to true if the parameter should not be shown to the user.
     * 
     * @param hidden
     *            true if the parameter should not be shown to the user.
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Returns true if the parameter can be empty
     * 
     * @return true if the parameter is hidden.
     */
    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    /**
     * Set to true if the parameter should not be shown to the user.
     * 
     * @param allowNull
     *            true if the parameter should not be shown to the user.
     */
    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    /**
     * Returns array with all allowable options.
     * 
     * @return all allowable options.
     */
    public String[] getOptions() {
        return options;
    }

    /**
     * Sets an array with all allowable options.
     * 
     * @return all allowable options.
     */
    public void setOptions(String... options) {
        this.options = options;
    }
}
