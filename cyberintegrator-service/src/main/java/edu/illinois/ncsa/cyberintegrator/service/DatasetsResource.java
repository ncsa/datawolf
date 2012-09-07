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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import edu.illinois.ncsa.cyberintegrator.ImportExport;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/datasets")
public class DatasetsResource {

    /**
     * 
     * Create dataset from zip file. It expects the following form:
     * <form action="rest/workflows" method="post"
     * enctype="multipart/form-data">
     * <input type="file" name="uploadedFile" />
     * <input type="submit" value="Upload It" />
     * </form>
     * 
     * @param input
     *            a workflow created from JSON
     * @return
     *         datasetId
     */
    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createDataset(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("uploadedFile");
        Dataset dataset = null;

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

                dataset = ImportExport.importDataset(tempfile);
                tempfile.delete();
                SpringData.getBean(DatasetDAO.class).save(dataset);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        return dataset.getId();
    }

    /**
     * Get all datasets
     * 
     * @param size
     *            number of datasets per page
     * @param page
     *            page number starting 0
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Dataset> getDatasets(@QueryParam("size") @DefaultValue("100") int size, @QueryParam("page") @DefaultValue("0") int page) {
        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);
        Page<Dataset> results = datasetDao.findAll(new PageRequest(page, size));
        return results.getContent();
    }

    /**
     * 
     * Get a dataset by Id
     * 
     * @param datasetId
     *            dataset Id
     * @return
     *         a dataset in JSON
     */
    @GET
    @Path("{dataset-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Dataset getDataset(@PathParam("dataset-id") String datasetId) {
        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);
        return datasetDao.findOne(datasetId);
    }

    /**
     * 
     * Get a dataset in zip with all other files by Id
     * 
     * @param datasetId
     *            dataset Id
     * @return
     *         a dataset in zip
     */
    @GET
    @Path("{dataset-id}/zip")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response getDatasetZip(@PathParam("dataset-id") String datasetId) {
        try {
            final File tempfile = File.createTempFile("dataset", ".zip");
            ImportExport.exportDataset(tempfile, datasetId);
            ResponseBuilder response = Response.ok(new FileInputStream(tempfile) {
                @Override
                public void close() throws IOException {
                    super.close();
                    tempfile.delete();
                }
            });
            response.type("application/zip");
            response.header("Content-Disposition", "attachment; filename=\"dataset.zip\"");
            return response.build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 
     * Get a FileDescriptor by dataset-Id and filedescriptor-id
     * 
     * @param datasetId
     *            dataset Id
     * @param fileDescriptorId
     *            filedescriptor Id
     * @return
     *         a FileDescriptor in JSON
     */
    @GET
    @Path("{dataset-id}/{filedescriptor-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public FileDescriptor getFileDescriptor(@PathParam("dataset-id") String datasetId, @PathParam("filedescriptor-id") String fileDescriptorId) {
        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);
        Dataset dataset = datasetDao.findOne(datasetId);
        List<FileDescriptor> fileDescriptors = dataset.getFileDescriptors();
        for (FileDescriptor fd : fileDescriptors) {
            if (fd.getId().equals(fileDescriptorId))
                return fd;
        }
        return null;
    }

    /**
     * get the file with given id
     * 
     * @param id
     *            file-descriptor id
     * @return
     *         file
     */
    @GET
    @Path("{dataset-id}/{filedescriptor-id}/file")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response getFile(@PathParam("dataset-id") String datasetId, @PathParam("filedescriptor-id") String fileDescriptorId) {
        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);
        Dataset dataset = datasetDao.findOne(datasetId);
        List<FileDescriptor> fileDescriptors = dataset.getFileDescriptors();
        FileDescriptor fileDescriptor = null;
        for (FileDescriptor fd : fileDescriptors) {
            if (fd.getId().equals(fileDescriptorId)) {
                fileDescriptor = fd;
                break;
            }
        }

        if (fileDescriptor == null)
            return Response.status(500).entity("Can't find the file (id:" + fileDescriptorId + ") in dataset id: " + datasetId).build();

        FileStorage fileStorage = SpringData.getFileStorage();
        try {
            InputStream is = fileStorage.readFile(fileDescriptor);
            ResponseBuilder response = Response.ok(is);
            if (fileDescriptor.getMimeType().equals("")) {
                response.type("application/unknown");
            } else {
                response.type(fileDescriptor.getMimeType());
            }
            response.header("Content-Disposition", "attachment; filename=\"" + fileDescriptor.getFilename() + "\"");
            // logger.debug("Downloading dataset " + decoded);
            return response.build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.status(500).entity("Can't find the file (id:" + fileDescriptorId + ") in dataset id: " + datasetId).build();
    }
}
