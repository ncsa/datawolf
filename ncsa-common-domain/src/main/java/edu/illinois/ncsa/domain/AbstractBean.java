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

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class AbstractBean implements Serializable {
    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** Unique identifier for this bean, used by persistence layer */
    @Id
    // TODO RK : fix this to specific size
    private String            id;

    /** Unique identifier for this bean, used for external reference */
    private transient URI     uri              = null;
    private String            uriString        = null;

    public AbstractBean() {
        id = UUID.randomUUID().toString();
        uri = Minter.createURI(this.getClass().getSimpleName(), getId());
        uriString = uri.toString();
    }

    /**
     * Return the id of the bean.
     * 
     * @return id of the bean
     */
    public final String getId() {
        return id;
    }

    /**
     * Return the uri of the bean.
     * 
     * @return uri of the bean
     */
    public final URI getUri() {
        if (uri == null) {
            uri = URI.create(uriString);
        }
        return uri;
    }

    /**
     * Sets the uri of the bean
     * 
     * @param uri
     *            sets the uri of the bean.
     */
    public final void setUri(URI uri) {
        this.uri = uri;
        uriString = uri.toString();
    }
}
