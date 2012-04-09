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
package edu.illinois.ncsa.domain;

import java.math.BigInteger;
import java.net.URL;

import javax.persistence.Entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Entity(name = "FileDescriptor")
@Document(collection = "FileDescriptor")
public class FileDescriptor extends AbstractBean {
    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** Original filename */
    private String            filename         = null;

    /** Mime type of the dataset data */
    private String            mimetype         = "";  //$NON-NLS-1$

    /** size of the blob associated */
    private long              size             = -1;

    /** url where the actual data is stored */
    private URL               dataURL;

    /** md5 sum of the actual data */
    private BigInteger        md5sum;

    public FileDescriptor() {}

    /**
     * Return the mime type of the artifact.
     * 
     * @return mime type of the artifact
     */
    public String getMimeType() {
        return mimetype;
    }

    /**
     * Sets the mime type of the artifact
     * 
     * @param mimetype
     *            sets the mime type of the artifact
     * 
     */
    public void setMimeType(String mimetype) {
        this.mimetype = mimetype;
    }

    /**
     * Returns the size of the blob, this is -2 if there is no blob associated
     * or the size has not been computed.
     * 
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the size of the blob, this can be set to -2 if there is no blob
     * associated with this dataset.
     * 
     * @param size
     *            the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * If this dataset originated in a filesystem, or is intended to be stored
     * in a filesystem, what filename is/should be used? If not, set to null.
     * 
     * @param filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * If this dataset originated in a filesystem, or is intended to be stored
     * in a filesystem, what filename is/should be used? If not, will return
     * null.
     * 
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return the dataURL
     */
    public URL getDataURL() {
        return dataURL;
    }

    /**
     * @param dataURL the dataURL to set
     */
    public void setDataURL(URL dataURL) {
        this.dataURL = dataURL;
    }

    /**
     * @return the md5sum
     */
    public BigInteger getMd5sum() {
        return md5sum;
    }

    /**
     * @param md5sum the md5sum to set
     */
    public void setMd5sum(BigInteger md5sum) {
        this.md5sum = md5sum;
    }
}
