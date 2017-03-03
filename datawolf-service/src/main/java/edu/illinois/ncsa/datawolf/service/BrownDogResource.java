/*******************************************************************************
 * Copyright (c) 2016 University of Illinois/NCSA.  All rights reserved.
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

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.datawolf.service.utils.WorkflowUtil;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

@Path("/browndog")
public class BrownDogResource {

    private static final Logger log = LoggerFactory.getLogger(BrownDogResource.class);

    @Inject
    private WorkflowDao         workflowDao;

    @Inject
    private PersonDao           personDao;

    /**
     * 
     * @param username
     *            User requesting workflow
     * @param dwUrl
     *            datawolf endpoint to build response
     * @param token
     *            authorization token for BD-API
     * @return Link to workflow
     */
    @POST
    @Path("provenance")
    @Produces({ MediaType.TEXT_HTML })
    public Response createWorkflow(@Context HttpServletRequest request, @HeaderParam("X-BD-Username") String username, @HeaderParam("Host") String dwUrl, @HeaderParam("Authorization") String token) {

        if (username == null || username.isEmpty()) {
            return Response.status(500).entity("Username not found in the request header").build();
        }

        if (dwUrl == null || dwUrl.isEmpty()) {
            return Response.status(500).entity("DataWolf host not found in the request header").build();
        }

        // TODO token should be handled by Authentication interceptor
        if (token != null && !token.isEmpty()) {
            try {
                JsonElement jsonElement = new JsonParser().parse(request.getReader());
                JsonObject body = jsonElement.getAsJsonObject();

                String fileId = body.get("file").getAsString();

                if (!fileId.isEmpty()) {
                    String fenceURL = body.get("fence").getAsString();
                    String type = fileId.startsWith("http") ? "dap" : "dts";

                    if (!type.isEmpty() && !fenceURL.isEmpty()) {
                        String datawolfUrl = dwUrl;
                        if (!datawolfUrl.endsWith("/")) {
                            datawolfUrl += "/";
                        }

                        Person creator = personDao.findByEmail(username);
                        if (creator == null) {
                            String signupUrl = datawolfUrl + "editor/login.html";
                            return Response.status(500).entity(username + " is not a registered DataWolf user, please sign up for an account at " + signupUrl).build();
                        }

                        Workflow workflow = null;
                        try {
                            if (type.equals("dts")) {
                                workflow = WorkflowUtil.createDTSWorkflow(fileId, fenceURL, token, creator);
                            } else {
                                workflow = WorkflowUtil.createDAPWorkflow(fileId, fenceURL, token, creator);
                            }

                        } catch (Exception e) {
                            log.error("Error creating workflow", e);
                            return Response.status(500).entity("Could not build workflow for file with id " + fileId).build();
                        }
                        workflowDao.save(workflow);

                        String workflowURL = datawolfUrl + "editor/index.html#" + workflow.getId();
                        String workflowLink = "<html><p>Follow the link to the DataWolf workflow generated for the file:</p>\n";
                        workflowLink += "<a href=\"" + workflowURL + "\" target=\"_blank\">" + workflowURL + "</a>\n</html>";

                        ResponseBuilder response = Response.ok(workflowLink);
                        return response.build();

                    } else {
                        return Response.status(500).entity("This DAP or DTS file doesn't appear to be valid: " + fileId).build();
                    }
                } else {
                    return Response.status(500).entity("Must specify a file.").build();
                }

            } catch (IOException e) {
                e.printStackTrace();
                return Response.status(500).entity("Could not build workflow for file").build();
            }

        } else {
            log.debug("User not authenticated");
            return unauthorizedResponse("Not authenticated");
        }
    }

    /**
     * Response in case of failed authentication.
     * 
     * TODO This is a temporary solution for this service endpoint. All
     * authentication should go through the interceptor; however, this is
     * checking the token against the bd-api, not DataWolf's user base.
     * 
     * @param preprocessedPath
     * @return
     */
    private ServerResponse unauthorizedResponse(String preprocessedPath) {
        ServerResponse response = new ServerResponse();
        response.setStatus(HttpResponseCodes.SC_UNAUTHORIZED);
        MultivaluedMap<String, Object> headers = new Headers<Object>();
        headers.add("Content-Type", "text/plain");
        response.setMetadata(headers);
        response.setEntity("Error 401 Unauthorized: " + preprocessedPath);
        return response;
    }

}
