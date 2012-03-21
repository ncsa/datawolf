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

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;

@Path("/workflows")
public class WorkflowsResource {

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createWorkflow(Workflow workflow) {
        return null;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Workflow> getWorkflows(@QueryParam("limit") @DefaultValue("100") int limit) {

        return null;
    }

    @GET
    @Path("{wid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Workflow getWorkflow(@PathParam("wid") String workflowId) {

        return null;
    }

    @POST
    @Path("{wid}/executions")
    @Consumes({ MediaType.APPLICATION_JSON })
    public String createExecution(@PathParam("wid") String workflowId, @QueryParam("run") @DefaultValue("true") boolean run) {

        return null;
    }

    @GET
    @Path("{wid}/executions")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Execution> getExecutions(@PathParam("wid") String workflowId, @QueryParam("limit") @DefaultValue("100") int limit) {

        return null;
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
