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

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/workflowtools")
public class WorkflowToolsResource {

    /**
     * Get all workflow tools
     * 
     * @param size
     *            number of workflow tools per page
     * 
     * @param page
     *            page number starting at 0
     * @return
     *         list of workflow tools
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<WorkflowTool> getWorkflowTools(@QueryParam("size") @DefaultValue("100") int size, @QueryParam("page") @DefaultValue("0") int page) {
        WorkflowToolDAO toolDAO = SpringData.getBean(WorkflowToolDAO.class);
        Page<WorkflowTool> results = toolDAO.findAll(new PageRequest(page, size));
        return results.getContent();
    }

    /**
     * Get a workflow tool by Id
     * 
     * @param toolId
     *            workflow tool Id
     * @return
     *         a workflow tool in JSON
     */
    @GET
    @Path("{tool-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public WorkflowTool getWorkflowTool(@PathParam("tool-id") String toolId) {
        WorkflowToolDAO toolDAO = SpringData.getBean(WorkflowToolDAO.class);
        return toolDAO.findOne(toolId);
    }

    // TODO add endpoint for creating a workflow tool
}
