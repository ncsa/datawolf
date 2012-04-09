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

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/workflows")
public class WorkflowsResource {

    /**
     * 
     * Create workflow via workflow JSON
     * 
     * @param workflow
     *            a workflow created from JSON
     * @return
     *         workflow URI
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createWorkflow(Workflow workflow) {
        return null;
    }

    /**
     * Get all workflows
     * 
     * @param size
     *            number of workflows per page
     * @param page
     *            page number starting 0
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Workflow> getWorkflows(@QueryParam("size") @DefaultValue("100") int size, @QueryParam("page") @DefaultValue("0") int page) {
        WorkflowDAO wfdao = SpringData.getDAO(WorkflowDAO.class);
        Page<Workflow> results = wfdao.findAll(new PageRequest(page, size));
        return results.getContent();
    }

    /**
     * 
     * Get a workflow by URI
     * 
     * @param workflowId
     *            workflow URI
     * @return
     *         a workflow in JSON
     */
    @GET
    @Path("{wid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Workflow getWorkflow(@PathParam("wid") String workflowId) {
        WorkflowDAO wfdao = SpringData.getDAO(WorkflowDAO.class);
        return wfdao.findByUriString(workflowId);
    }

    @POST
    @Path("{wid}/executions")
    @Consumes({ MediaType.APPLICATION_JSON })
    public String createExecution(@PathParam("wid") String workflowId, @QueryParam("run") @DefaultValue("true") boolean run) {
        return null;
    }

    /**
     * 
     * Get all executions by workflow id
     * 
     * @param workflowId
     *            workflow id (it should be URI)
     * @param size
     *            number of workflows per page
     * @param page
     *            page number starting 0
     * 
     * @return
     *         a execution in JSON
     */
    @GET
    @Path("{wid}/executions")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Execution> getExecutions(@PathParam("wid") String workflowId, @QueryParam("size") @DefaultValue("100") int size, @QueryParam("page") @DefaultValue("0") int page) {
        WorkflowDAO wfdao = SpringData.getDAO(WorkflowDAO.class);
        Workflow wf = wfdao.findByUriString(workflowId);

        ExecutionDAO execDao = SpringData.getDAO(ExecutionDAO.class);
        List<Execution> execList = execDao.findByWorkflow(wf);
        return execList;
    }

    @GET
    @Path("{wid}/executions/{eid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Execution getExecution(@PathParam("wid") String workflowId, @PathParam("eid") String executionId) {

        return null;
    }

    @GET
    @Path("{wid}/executions/{eid}/steps")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<WorkflowStep> getSteps(@PathParam("wid") String workflowId, @PathParam("eid") String executionId) {
        return null;
    }

    @GET
    @Path("{wid}/executions/{eid}/steps/{stid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public WorkflowStep getStep(@PathParam("wid") String workflowId, @PathParam("eid") String executionId, @PathParam("stid") String stepId) {

        return null;
    }

    @PUT
    @Path("{wid}/executions/{eid}/start")
    public void startExecution(@PathParam("wid") String workflowId, @PathParam("eid") String executionId) {

    }

    @PUT
    @Path("{wid}/executions/{eid}/pause")
    public void pauseExecution(@PathParam("wid") String workflowId, @PathParam("eid") String executionId) {

    }

    @PUT
    @Path("{wid}/executions/{eid}/cancel")
    public void cancelExecution(@PathParam("wid") String workflowId, @PathParam("eid") String executionId) {

    }

}
