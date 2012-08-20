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
import java.util.Map.Entry;

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

import edu.illinois.ncsa.cyberintegrator.Engine;
import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Submission;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.PersonDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/executions")
public class ExecutionsResource {

    /**
     * Create execution via execution JSON
     * 
     * @param submission
     *            a submission JSON object
     * @return
     *         execution Id
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createExecution(Submission submission) {
        Execution execution = new Execution();
        execution.setWorkflow(SpringData.getBean(WorkflowDAO.class).findOne(submission.getWorkflowId()));
        execution.setCreator(SpringData.getBean(PersonDAO.class).findOne(submission.getCreatorId()));
        for (Entry<String, String> param : submission.getParameters().entrySet()) {
            execution.setParameter(param.getKey(), param.getValue());
        }
        DatasetDAO datasetDAO = SpringData.getBean(DatasetDAO.class);
        for (Entry<String, String> dataset : submission.getDatasets().entrySet()) {
            execution.setDataset(dataset.getKey(), datasetDAO.findOne(dataset.getValue()));
        }

        SpringData.getBean(ExecutionDAO.class).save(execution);
        return execution.getId();
    }

    /**
     * Get all executions
     * 
     * @param size
     *            number of workflows per page (default: 100)
     * @param page
     *            page number starting 0
     * @return
     *         list of executions
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Execution> getExecutions(@QueryParam("size") @DefaultValue("100") int size, @QueryParam("page") @DefaultValue("0") int page) {
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);
        Page<Execution> results = exedao.findAll(new PageRequest(page, size));
        return results.getContent();
    }

    /**
     * 
     * Get a execution by Id
     * 
     * @param executionId
     *            execution id
     * @return
     *         a execution in JSON
     */
    @GET
    @Path("{execution-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Execution getExecution(@PathParam("execution-id") String executionId) {
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);
        return exedao.findOne(executionId);
    }

    /**
     * Get all steps belong to the execution
     * 
     * @param executionId
     *            a execution Id
     * @return
     *         list of workflow steps
     */
    @GET
    @Path("{execution-id}/steps")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<WorkflowStep> getSteps(@PathParam("execution-id") String executionId) {
        return null;
    }

    /**
     * Get a step belong to the execution
     * 
     * @param executionId
     *            a execution id
     * @param stepId
     *            a step id
     * @return
     *         a workflow step
     */
    @GET
    @Path("{execution-id}/steps/{step-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public WorkflowStep getStep(@PathParam("execution-id") String executionId, @PathParam("step-id") String stepId) {

        return null;
    }

    /**
     * Start a execution
     * 
     * @param executionId
     *            a execution id
     */
    @PUT
    @Path("{execution-id}/start")
    public void startExecution(@PathParam("execution-id") String executionId) {
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);
        Execution execution = exedao.findOne(executionId);
        Engine engine = SpringData.getBean(Engine.class);
        engine.execute(execution);
        execution = exedao.findOne(executionId);
    }

    /**
     * Pause a execution
     * 
     * @param executionId
     */
    @PUT
    @Path("{execution-id}/pause")
    public void pauseExecution(@PathParam("execution-id") String executionId) {

    }

    /**
     * Cancel a execution
     * 
     * @param executionId
     */
    @PUT
    @Path("{execution-id}/cancel")
    public void cancelExecution(@PathParam("execution-id") String executionId) {

    }

}
