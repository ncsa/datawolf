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

import java.util.UUID;

import edu.illinois.ncsa.domain.AbstractBean;

public class WorkflowToolData extends AbstractBean {
    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** Id of the workflow tool data */
    private String            dataid           = UUID.randomUUID().toString();

    /** Title of the workflow tool data */
    private String            title            = "";                          //$NON-NLS-1$

    /** Description of the workflow tool data */
    private String            description      = "";                          //$NON-NLS-1$

    /** Mime type of the workflow tool data */
    private String            mimeType         = "";                          //$NON-NLS-1$

    /**
     * Create a new instance of the workflow tool.
     */
    public WorkflowToolData() {
        setId(UUID.randomUUID().toString());
    }

    /**
     * Return the id of the workflow tool data.
     * 
     * @return id of the workflow tool data
     */
    public String getDataId() {
        return dataid;
    }

    /**
     * Sets the id of the workflow tool data
     * 
     * @param id
     *            sets the id of the workflow tool data.
     * 
     */
    public void setDataId(String dataid) {
        this.dataid = dataid;
    }

    /**
     * Return the title of the workflow tool data.
     * 
     * @return title of the workflow tool data
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the workflow tool data
     * 
     * @param title
     *            sets the title of the workflow tool data.
     * 
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the description of the workflow tool data.
     * 
     * @return description of the workflow tool data
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the workflow tool data
     * 
     * @param description
     *            sets the description of the workflow tool data
     * 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the mime type of the workflow tool data.
     * 
     * @return mime type of the workflow tool data
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the mime type of the workflow tool data
     * 
     * @param mimeType
     *            sets the mime type of the workflow tool data
     * 
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return title + " [" + mimeType + "]";
    }
}
