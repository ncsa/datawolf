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

import java.io.File;
import java.io.FileOutputStream;
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import edu.illinois.ncsa.cyberintegrator.ImportExport;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/workflowtools")
public class WorkflowToolsResource {
    private static Logger logger = LoggerFactory.getLogger(WorkflowToolsResource.class);

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
    public List<WorkflowTool> getWorkflowTools(@QueryParam("size") @DefaultValue("-1") int size, @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("showdeleted") @DefaultValue("false") boolean showdeleted, @QueryParam("email") @DefaultValue("") String email) {
        WorkflowToolDAO toolDao = SpringData.getBean(WorkflowToolDAO.class);

        if (size < 1) {
            if (email.equals("")) {
                Iterable<WorkflowTool> tmp;
                if (showdeleted) {
                    tmp = toolDao.findAll();
                } else {
                    tmp = toolDao.findByDeleted(false);
                }
                ArrayList<WorkflowTool> list = new ArrayList<WorkflowTool>();
                for (WorkflowTool d : tmp) {
                    list.add(d);
                }
                return list;
            } else {
                if (showdeleted) {
                    return toolDao.findByCreatorEmail(email);
                } else {
                    return toolDao.findByCreatorEmailAndDeleted(email, false);
                }
            }
        } else {
            Page<WorkflowTool> results = null;

            if (email.equals("")) {
                if (showdeleted) {
                    results = toolDao.findAll(new PageRequest(page, size));
                } else {
                    results = toolDao.findByDeleted(false, new PageRequest(page, size));
                }
            } else {
                if (showdeleted) {
                    results = toolDao.findByCreatorEmail(email, new PageRequest(page, size));
                } else {
                    results = toolDao.findByCreatorEmailAndDeleted(email, false, new PageRequest(page, size));
                }
            }
            return results.getContent();

        }
    }

    /**
     * 
     * Create workflow via workflow JSON. Expects the following form:
     * <form action="/workflowtools" method="post"
     * enctype="multipart/form-data">
     * <input type="tool" name="uploadedFile" />
     * <input type="submit" value="Upload It" />
     * </form>
     * 
     * @param input
     *            a form posted
     * @return
     *         tool URI
     */
    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public String[] createTool(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("tool");
        List<String> toolids = new ArrayList<String>();

        for (InputPart inputPart : inputParts) {
            try {
                // convert the uploaded file to zipfile
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                File tempfile = File.createTempFile("tool", ".zip");
                OutputStream outputStream = new FileOutputStream(tempfile);
                byte[] buf = new byte[10240];
                int len = 0;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();

                WorkflowTool tool = ImportExport.importTool(tempfile);
                toolids.add(tool.getId());
                tempfile.delete();

            } catch (Exception e) {
                logger.warn("Could not load tool.", e);
            }
        }
        return toolids.toArray(new String[toolids.size()]);
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

    /**
     * Delete the tool given the specific id.
     * 
     * @param toolId
     *            the tool to be deleted
     */
    @DELETE
    @Path("{tool-id}")
    public void deleteTool(@PathParam("tool-id") String toolId) throws Exception {
        if ("".equals(toolId)) {
            throw (new Exception("Invalid id passed in."));
        }
        WorkflowToolDAO toolDao = SpringData.getBean(WorkflowToolDAO.class);
        WorkflowTool tool = toolDao.findOne(toolId);
        if (tool == null) {
            throw (new Exception("Invalid id passed in."));
        }
        tool.setDeleted(true);
        toolDao.save(tool);
    }

}
