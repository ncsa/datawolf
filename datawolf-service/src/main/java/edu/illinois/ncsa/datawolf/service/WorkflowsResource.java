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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import edu.illinois.ncsa.datawolf.ImportExport;
import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.springdata.ExecutionDAO;
import edu.illinois.ncsa.datawolf.springdata.WorkflowDAO;
import edu.illinois.ncsa.datawolf.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/workflows")
public class WorkflowsResource {
    private static final Logger log = LoggerFactory.getLogger(WorkflowsResource.class);

    /**
     * Create a new workflow
     * 
     * @param workflow
     *            Workflow to create
     * @return created workflow
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Workflow createWorkflow(Workflow workflow) {
        log.debug("Creating new workflow with id " + workflow.getId());
        workflow = SpringData.removeDuplicate(workflow);
        WorkflowDAO workflowDAO = SpringData.getBean(WorkflowDAO.class);
        Workflow tmp = workflowDAO.save(workflow);
        return tmp;
    }

    /**
     * Update workflow by id
     * 
     * @param workflow
     *            Updated workflow
     * @return Updated workflow
     */
    @PUT
    @Path("{workflow-id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Workflow updateWorkflow(Workflow workflow) {
        log.debug("Updating workflow. workflow id " + workflow.getId());
        workflow = SpringData.removeDuplicate(workflow);
        WorkflowDAO workflowDAO = SpringData.getBean(WorkflowDAO.class);
        Workflow tmp = workflowDAO.save(workflow);
        return tmp;
    }

    /**
     * 
     * Create workflow via workflow JSON. Expects the following form:
     * <form action="rest/workflows" method="post"
     * enctype="multipart/form-data">
     * <input type="file" name="uploadedFile" />
     * <input type="submit" value="Upload It" />
     * </form>
     * 
     * @param workflow
     *            a workflow created from JSON
     * @return
     *         workflow URI
     */
    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.TEXT_PLAIN })
    public String createWorkflow(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("workflow");
        Workflow workflow = null;

        for (InputPart inputPart : inputParts) {
            try {
                // convert the uploaded file to zipfile
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                File tempfile = File.createTempFile("workflow", ".zip");
                OutputStream outputStream = new FileOutputStream(tempfile);
                byte[] buf = new byte[10240];
                int len = 0;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();

                workflow = ImportExport.importWorkflow(tempfile);
                tempfile.delete();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        return workflow.getId();
    }

    /**
     * Get all workflows
     * 
     * @param size
     *            number of workflows per page (default -1)
     * @param page
     *            page number starting 0
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Workflow> getWorkflows(@QueryParam("size") @DefaultValue("-1") int size, @QueryParam("page") @DefaultValue("0") int page, @QueryParam("email") @DefaultValue("") String email,
            @QueryParam("showdeleted") @DefaultValue("false") boolean showdeleted) {
        log.debug("get workflows");

        WorkflowDAO wfdao = SpringData.getBean(WorkflowDAO.class);
        Sort sort = new Sort(Sort.Direction.DESC, "created");
        if (size < 1) {
            if (email.equals("")) {
                Iterable<Workflow> tmp;
                if (showdeleted) {
                    tmp = wfdao.findAll(sort);
                } else {
                    tmp = wfdao.findByDeleted(false, sort);
                }
                ArrayList<Workflow> list = new ArrayList<Workflow>();
                for (Workflow d : tmp) {
                    list.add(d);
                }
                return list;
            } else {
                if (showdeleted) {
                    return wfdao.findByCreatorEmail(email, sort);
                } else {
                    return wfdao.findByCreatorEmailAndDeleted(email, false, sort);
                }
            }
        } else {
            Page<Workflow> results;
            if (email.equals("")) {
                if (showdeleted) {
                    results = wfdao.findAll(new PageRequest(page, size));
                } else {
                    results = wfdao.findByDeleted(false, new PageRequest(page, size));
                }
            } else {
                if (showdeleted) {
                    results = wfdao.findByCreatorEmail(email, new PageRequest(page, size, sort));
                } else {
                    results = wfdao.findByCreatorEmailAndDeleted(email, false, new PageRequest(page, size, sort));
                }

            }
            return results.getContent();
        }
    }

    /**
     * 
     * Get a workflow by Id
     * 
     * @param workflowId
     *            workflow Id
     * @return
     *         a workflow in JSON
     */
    @GET
    @Path("{workflow-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Workflow getWorkflow(@PathParam("workflow-id") String workflowId) {
        log.warn("get workflow by id");
        WorkflowDAO wfdao = SpringData.getBean(WorkflowDAO.class);
        return wfdao.findOne(workflowId);
    }

    /**
     * Delete a workflow by Id
     * 
     * @param workflowId
     *            id of workflow to delete
     * @return true if delete successful, false otherwise
     * @throws Exception
     */
    @DELETE
    @Path("{workflow-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public boolean deleteWorkflow(@PathParam("workflow-id") @DefaultValue("") String workflowId) throws Exception {
        if ("".equals(workflowId)) {
            throw (new Exception("Invalid id passed in."));
        }
        WorkflowDAO workflowDao = SpringData.getBean(WorkflowDAO.class);
        Workflow workflow = workflowDao.findOne(workflowId);
        if (workflow == null) {
            throw (new Exception("Invalid id passed in."));
        }
        workflow.setDeleted(true);
        workflowDao.save(workflow);
        return true;
    }

    /**
     * 
     * Get a workflow by Id
     * 
     * @param workflowId
     *            workflow Id
     * @return
     *         a workflow in JSON
     */
    @GET
    @Path("{workflow-id}/zip")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response getWorkflowZip(@PathParam("workflow-id") String workflowId) {
        try {
            final File tempfile = File.createTempFile("workflow", ".zip");
            ImportExport.exportWorkflow(tempfile, workflowId);
            ResponseBuilder response = Response.ok(new FileInputStream(tempfile) {
                @Override
                public void close() throws IOException {
                    super.close();
                    tempfile.delete();
                }
            });
            response.type("application/zip");
            response.header("Content-Disposition", "attachment; filename=\"workflow.zip\"");
            return response.build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a execution by execution JSON
     * 
     * @param workflowId
     *            workflow id
     * @param run
     *            after create an execution, whether run it or not
     * @return
     *         execution id
     */
    @POST
    @Path("{workflow-id}/executions")
    @Consumes({ MediaType.APPLICATION_JSON })
    public String createExecution(@PathParam("workflow-id") String workflowId, @QueryParam("run") @DefaultValue("true") boolean run) {
        // TODO implement createExecution
        return null;
    }

    /**
     * 
     * Get all executions by workflow id
     * 
     * @param workflowId
     *            workflow id
     * @param size
     *            number of workflows per page
     * @param page
     *            page number starting 0
     * 
     * @return
     *         a execution in JSON
     */
    @GET
    @Path("{workflow-id}/executions")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Execution> getExecutions(@PathParam("workflow-id") String workflowId, @QueryParam("size") @DefaultValue("100") int size, @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("showdeleted") @DefaultValue("false") boolean showdeleted) {
        ExecutionDAO execDao = SpringData.getBean(ExecutionDAO.class);
        Page<Execution> results;
        if (showdeleted) {
            results = execDao.findByWorkflowId(workflowId, new PageRequest(page, size));
        } else {
            results = execDao.findByWorkflowIdAndDeleted(workflowId, false, new PageRequest(page, size));
        }
        return results.getContent();
    }

    /**
     * Get steps associated with a workflow
     * 
     * @param workflowId
     *            id of workflow to get steps for
     * @return workflow steps for the workflow requested
     * @throws Exception
     */
    @GET
    @Path("{workflow-id}/steps")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<WorkflowStep> getSteps(@PathParam("workflow-id") @DefaultValue("") String workflowId) throws Exception {
        WorkflowDAO workflowDao = SpringData.getBean(WorkflowDAO.class);
        Workflow workflow = workflowDao.findOne(workflowId);
        if (workflow == null) {
            throw (new Exception("Invalid workflow id passed in."));
        }
        return workflow.getSteps();
    }

    /**
     * Get a workflow step associated with a workflow
     * 
     * @param workflowId
     *            id of workflow to get step for
     * @param stepId
     *            id of the step to get
     * @return requested workflow step
     * @throws Exception
     */
    @GET
    @Path("{workflow-id}/steps/{step-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public WorkflowStep getStep(@PathParam("workflow-id") @DefaultValue("") String workflowId, @PathParam("step-id") @DefaultValue("") String stepId) throws Exception {
        if ("".equals(workflowId) || "".equals(stepId)) {
            throw (new Exception("Invalid id passed in."));
        }

        WorkflowDAO workflowDao = SpringData.getBean(WorkflowDAO.class);
        Workflow workflow = workflowDao.findOne(workflowId);
        if (workflow == null) {
            throw (new Exception("Invalid workflow id passed in."));
        }

        WorkflowStepDAO workflowStepDao = SpringData.getBean(WorkflowStepDAO.class);
        WorkflowStep workflowStep = workflowStepDao.findOne(stepId);

        if (workflowStep == null) {
            throw (new Exception("Invalid step id passed in."));
        }

        return workflowStep;
    }

}