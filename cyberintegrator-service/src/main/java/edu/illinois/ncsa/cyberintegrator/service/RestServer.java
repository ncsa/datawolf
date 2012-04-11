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
package edu.illinois.ncsa.cyberintegrator.service;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

public class RestServer {
    public static final int PORT = 8088;

    public static void main(String[] args) {
        SpringData.loadXMLContext("applicationContext.xml");

        Workflow wf = new Workflow();
        Execution e = new Execution();
        e.setWorkflow(wf);

        SpringData.getBean(ExecutionDAO.class).save(e);

        TJWSEmbeddedJaxrsServer tjws = new TJWSEmbeddedJaxrsServer();
        tjws.setPort(PORT);
        tjws.start();
        tjws.setRootResourcePath("/");
        tjws.getDeployment().getRegistry().addSingletonResource(new WorkflowsResource());
        tjws.getDeployment().getRegistry().addSingletonResource(new ExecutionsResource());
        System.out.println("http://localhost:" + PORT);
    }
}
