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
package edu.illinois.ncsa.datawolf.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.Engine;
import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.Execution.State;
import edu.illinois.ncsa.datawolf.domain.LogFile;
import edu.illinois.ncsa.datawolf.domain.Submission;
import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.datawolf.domain.dao.LogFileDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

@Path("/executions")
public class ExecutionsResource {

    private static final Logger log = LoggerFactory.getLogger(ExecutionsResource.class);

    @Inject
    private WorkflowDao         workflowDao;

    @Inject
    private PersonDao           personDao;

    @Inject
    private ExecutionDao        executionDao;

    @Inject
    private Engine              engine;

    @Inject
    private LogFileDao          logfileDao;

    /**
     * Create execution via Submission JSON
     * 
     * @param submission
     *            a submission JSON object
     * @return
     *         execution Id
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_PLAIN })
    public String createExecution(Submission submission) {

        log.warn("Here is the job submission datasets");

        Execution execution = new Execution();
        // find workflow
        Workflow workflow = workflowDao.findOne(submission.getWorkflowId());
        if (workflow != null) {
            execution.setWorkflow(workflow);
            execution.setTitle(submission.getTitle());
            execution.setDescription(submission.getDescription());
            execution.setCreator(personDao.findOne(submission.getCreatorId()));
            for (Entry<String, String> param : submission.getParameters().entrySet()) {
                execution.setParameter(param.getKey(), param.getValue());
            }
            for (Entry<String, String> dataset : submission.getDatasets().entrySet()) {
                execution.setDataset(dataset.getKey(), dataset.getValue());
                log.warn("key = "+dataset.getKey()+" value = "+dataset.getValue());
            }
            log.warn("Saving execution");
            executionDao.save(execution);
            log.warn("submitting job for execution");
            // start execution
            engine.execute(execution);

            return execution.getId();
        } else {
            String error = "Workflow " + submission.getWorkflowId() + " not found.";
            log.error(error);
            return error;
        }
    }

    @GET
    @Path("{workflow-id}/template")
    @Produces({ MediaType.APPLICATION_JSON })
    public Submission getSubmissionTemplate(@PathParam("workflow-id") String workflowId, @QueryParam("email") @DefaultValue("") String email) {

        Workflow workflow = workflowDao.findOne(workflowId);
        Submission submission = new Submission();
        submission.setId(""); //$NON-NLS-1$

        if (workflow != null) {
            submission.setWorkflowId(workflow.getId());
            if (!email.equals("")) { //$NON-NLS-1$
                Person person = personDao.findByEmail(email);
                if (person != null) {
                    submission.setCreatorId(person.getId());
                }
            }

            for (WorkflowStep step : workflow.getSteps()) {
                // Parameter keys to insert into Submission parameter map
                Map<String, String> parameters = step.getParameters();
                Iterator<String> keys = parameters.keySet().iterator();
                while (keys.hasNext()) {
                    String parameterId = keys.next();
                    String mappingKey = parameters.get(parameterId);
                    String defaultValue = step.getTool().getParameter(parameterId).getValue();
                    submission.setParameter(mappingKey, defaultValue);
                }

                // Dataset keys to insert into Submission dataset map
                Map<String, String> inputs = step.getInputs();
                Iterator<String> inputIterator = inputs.keySet().iterator();
                boolean add = true;
                while (inputIterator.hasNext()) {
                    String inputKey = inputIterator.next();
                    String mappingKey = inputs.get(inputKey);
                    for (WorkflowStep step0 : workflow.getSteps()) {
                        if (step0.getOutputs().containsValue(mappingKey)) {
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        submission.setDataset(mappingKey, ""); //$NON-NLS-1$
                    }
                }
            }
        }
        return submission;
    }

    /**
     * Get all executions
     * 
     * @param size
     *            number of workflows per page (default: -1)
     * @param page
     *            page number starting 0
     * @param email
     *            email of creator
     * @param since
     *            executions created after the given date, e.g. 2018-02-10
     * @param until
     *            executions created before the given date, e.g. 2018-02-10
     * @return
     *         list of executions
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Execution> getExecutions(@QueryParam("size") @DefaultValue("-1") int size, @QueryParam("page") @DefaultValue("0") int page, @QueryParam("email") @DefaultValue("") String email,
            @QueryParam("since") @DefaultValue("") String since, @QueryParam("until") @DefaultValue("") String until, @QueryParam("showdeleted") @DefaultValue("false") boolean showdeleted) {

        // without paging
        if (size < 1) {
            if (email.equals("")) {
                Iterable<Execution> tmp;
                if (showdeleted) {
                    tmp = executionDao.findAll();
                } else {
                    tmp = executionDao.findByDeleted(false);
                }
                ArrayList<Execution> list = new ArrayList<Execution>();
                for (Execution d : tmp) {
                    list.add(d);
                }
                return list;
            } else {
                if (showdeleted) {
                    return executionDao.findByCreatorEmail(email, since, until);
                } else {
                    return executionDao.findByCreatorEmailAndDeleted(email, false, since, until);
                }
            }

        } else { // with paging
            if (email.equals("")) {
                if (showdeleted) {
                    return executionDao.findAll(page, size);
                } else {
                    return executionDao.findByDeleted(false, page, size);
                }
            } else {
                if (showdeleted) {
                    return executionDao.findByCreatorEmail(email, page, size, since, until);
                } else {
                    return executionDao.findByCreatorEmailAndDeleted(email, false, page, size, since, until);
                }
            }
        }

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
        return executionDao.findOne(executionId);
    }

    /**
     * Delete execution by id
     * 
     * @param executionId
     *            execution id
     * @throws Exception
     */
    @DELETE
    @Path("{execution-id}")
    public void deleteExecution(@PathParam("execution-id") @DefaultValue("") String executionId) throws Exception {
        if ("".equals(executionId)) {
            throw (new Exception("Invalid id passed in."));
        }
        Execution execution = executionDao.findOne(executionId);
        if (execution == null) {
            throw (new Exception("Invalid id passed in."));
        }
        execution.setDeleted(true);
        executionDao.save(execution);
    }

    /**
     * Get all logfiles belonging to the execution
     * 
     * @param executionId
     *            a execution Id
     * @return
     *         list of logfile ids
     */
    @GET
    @Path("{execution-id}/logfiles")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<LogFile> getLogfiles(@PathParam("execution-id") String executionId) {
        return logfileDao.findByExecutionId(executionId);
    }

    /**
     * Get the logfile belonging to a single step of the execution
     * 
     * @param executionId
     *            a execution Id
     * @param stepId
     *            a step Id
     * @return
     *         list of logfile ids
     */
    @GET
    @Path("{execution-id}/logfiles/{step-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public LogFile getLogfiles(@PathParam("execution-id") String executionId, @PathParam("step-id") String stepId) {
        return logfileDao.findByExecutionIdAndStepId(executionId, stepId);
    }

    /**
     * Get the state all steps in an execution
     * 
     * @param executionId
     *            execution id
     * @return step id and state
     */
    @GET
    @Path("{execution-id}/state")
    @Produces({ MediaType.APPLICATION_JSON })
    public Map<String, State> getState(@PathParam("execution-id") String executionId) {
        Execution e = executionDao.findOne(executionId);
        return e.getStepState();
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
        Execution execution = executionDao.findOne(executionId);
        engine.execute(execution);
        // execution = executionDao.findOne(executionId);
    }

    /**
     * Pause a execution
     * 
     * @param executionId
     */
    @PUT
    @Path("{execution-id}/pause")
    public void pauseExecution(@PathParam("execution-id") String executionId) {
        // TODO implement pauseExecution per CBI-488
    }

    /**
     * Cancel a execution
     * 
     * @param executionId
     */
    @PUT
    @Path("{execution-id}/cancel")
    public void cancelExecution(@PathParam("execution-id") String executionId, @QueryParam("stepids") @DefaultValue("") String stepIds) {
        if (stepIds.equals("")) {
            engine.stop(executionId);
        } else {
            String[] ids = stepIds.split(";");
            engine.stop(executionId, ids);
        }
    }

}
