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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import edu.illinois.ncsa.cyberintegrator.ImportExport;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.DatasetUtil;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.PersonDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/datasets")
public class DatasetsResource {

    Logger log = LoggerFactory.getLogger(DatasetsResource.class);

    /**
     * 
     * Create dataset from zip file. It expects the following form:
     * <form action="/datasets" method="post" enctype="multipart/form-data">
     * <input type="file" name="uploadedFile" />
     * <input type="text" name="title" />
     * <input type="text" name="description" />
     * <input type="submit" value="Upload It" />
     * </form>
     * 
     * @param input
     *            a dataset created from Zip or from a file
     * @return
     *         datasetId
     */
    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createDataset(MultipartFormDataInput input) {

        log.trace("POST /datasets received");

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> datasetZipInputParts = uploadForm.get("dataset");
        List<InputPart> fileInputParts = uploadForm.get("uploadedFile");
        Dataset dataset = null;

        if (datasetZipInputParts != null) {
            for (InputPart inputPart : datasetZipInputParts) {
                try {
                    // convert the uploaded file to zipfile
                    InputStream inputStream = inputPart.getBody(InputStream.class, null);
                    File tempfile = File.createTempFile("dataset", ".zip");
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

                    log.debug("Zipped dataset uploaded");

                } catch (Exception e) {
                    log.error("Could not save dataset.", e);
                    return e.getMessage();
                }

            }
            return dataset.getId();
        }
        if (fileInputParts != null) {
            List<InputPart> userInputParts = uploadForm.get("useremail");

            if (userInputParts == null)
                return null;

            PersonDAO personDao = SpringData.getBean(PersonDAO.class);
            DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);
            Person creator = null;
            for (InputPart inputPart : userInputParts) {
                try {
                    String useremail = inputPart.getBody(String.class, null);
                    log.debug("Finding a person with id: " + useremail);
                    creator = personDao.findByEmail(useremail);
                    if (creator == null) {
                        log.error("Couldn't find a person with id: " + useremail);
                        return null;
                    }
                } catch (IOException e) {
                    log.error("Could not find user.", e);
                    return null;
                }
            }

            FileStorage fileStorage = SpringData.getFileStorage();

            FileDescriptor fileDescriptor = null;
            for (InputPart inputPart : fileInputParts) {
                try {
                    MultivaluedMap<String, String> header = inputPart.getHeaders();
                    String fileName = getFileName(header);

                    // convert the uploaded file to inputstream
                    InputStream inputStream = inputPart.getBody(InputStream.class, null);

                    // Store the file
                    fileDescriptor = fileStorage.storeFile(fileName, inputStream);
                    if (fileDescriptor == null)
                        return null;
                } catch (IOException e) {
                    log.warn("Could not parse the file", e);
                    return null;
                }

            }

            String title = fileDescriptor.getFilename();
            String description = "";

            // if upload the file without "title" in the form,
            // use filename as title
            List<InputPart> formTitle = uploadForm.get("title");
            if (formTitle != null) {
                for (InputPart val : formTitle) {
                    try {
                        title = val.getBody(String.class, null);
                    } catch (IOException e) {
                        log.warn("Could not getbody for title", e);
                    }
                }
            }

            // if upload the file without "description" in the form,
            // use "" as description
            List<InputPart> formDesc = uploadForm.get("description");
            if (formDesc != null) {
                for (InputPart val : formDesc) {
                    try {
                        description = val.getBody(String.class, null);
                    } catch (IOException e) {
                        log.warn("Could not getbody for description", e);
                    }
                }
            }

            dataset = new Dataset();
            dataset.setCreator(creator);
            dataset.addFileDescriptor(fileDescriptor);
            dataset.setTitle(title);
            dataset.setDescription(description);

            Dataset savedDataset = datasetDao.save(dataset);

            log.debug("Dataset uploaded");

            return savedDataset.getId();
        }

        return null;

    }

    private String getFileName(MultivaluedMap<String, String> header) {

        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

    /**
     * Get all datasets
     * 
     * @param size
     *            number of datasets per page
     * @param page
     *            page number starting 0
     * @param email
     *            email of creator
     * @param pattern
     *            filename pattern such as %.msh
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Dataset> getDatasets(@QueryParam("size") @DefaultValue("-1") int size, @QueryParam("page") @DefaultValue("0") int page, @QueryParam("email") @DefaultValue("") String email,
            @QueryParam("pattern") @DefaultValue("") String pattern, @QueryParam("showdeleted") @DefaultValue("false") boolean showdeleted) {
        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);

        Sort sort = new Sort(Sort.Direction.DESC, "date");

        // without paging
        if (size < 1) {
            Iterable<Dataset> results = null;
            if (email.equals("")) {
                if (pattern.equals("")) {
                    if (showdeleted) {
                        results = datasetDao.findAll(sort);
                    } else {
                        results = datasetDao.findByDeleted(false, sort);
                    }
                } else {
                    if (showdeleted) {
                        results = datasetDao.findByTitleLike(pattern, sort);
                    } else {
                        results = datasetDao.findByTitleLikeAndDeleted(pattern, false, sort);
                    }
                }
            } else {
                if (pattern.equals("")) {
                    if (showdeleted) {
                        results = datasetDao.findByCreatorEmail(email, sort);
                    } else {
                        results = datasetDao.findByCreatorEmailAndDeleted(email, false, sort);
                    }
                } else {
                    if (showdeleted) {
                        results = datasetDao.findByCreatorEmailAndTitleLike(email, pattern, sort);
                    } else {
                        results = datasetDao.findByCreatorEmailAndTitleLikeAndDeleted(email, pattern, false, sort);
                    }
                }
            }

            ArrayList<Dataset> list = new ArrayList<Dataset>();
            for (Dataset d : results) {
                list.add(d);
            }
            return list;

        } else { // with paging

            Page<Dataset> results = null;
            if (email.equals("")) {
                if (pattern.equals("")) {
                    if (showdeleted) {
                        results = datasetDao.findAll(new PageRequest(page, size, sort));
                    } else {
                        results = datasetDao.findByDeleted(false, new PageRequest(page, size, sort));
                    }
                } else {
                    if (showdeleted) {
                        results = datasetDao.findByTitleLike(pattern, new PageRequest(page, size, sort));
                    } else {
                        results = datasetDao.findByTitleLikeAndDeleted(pattern, false, new PageRequest(page, size, sort));
                    }
                }
            } else {
                if (pattern.equals("")) {
                    if (showdeleted) {
                        results = datasetDao.findByCreatorEmail(email, new PageRequest(page, size, sort));
                    } else {
                        results = datasetDao.findByCreatorEmailAndDeleted(email, false, new PageRequest(page, size, sort));
                    }
                } else {
                    if (showdeleted) {
                        results = datasetDao.findByCreatorEmailAndTitleLike(email, pattern, new PageRequest(page, size, sort));
                    } else {
                        results = datasetDao.findByCreatorEmailAndTitleLikeAndDeleted(email, pattern, false, new PageRequest(page, size, sort));
                    }
                }
            }
            return results.getContent();
        }
    }

    /**
     * 
     * Get a dataset by Id
     * 
     * @param datasetId
     *            id of dataset to retrieve
     * @return
     *         a dataset in JSON
     */
    @GET
    @Path("{dataset-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Dataset getDataset(@PathParam("dataset-id") String datasetId) {
        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);
        Dataset findOne = datasetDao.findOne(datasetId);

        return findOne;
    }

    /**
     * Mark dataset as deleted
     * 
     * @param datasetId
     *            id of dataset to delete
     * @throws Exception
     */
    @DELETE
    @Path("{dataset-id}")
    public void deleteDataset(@PathParam("dataset-id") @DefaultValue("") String datasetId) throws Exception {
        if ("".equals(datasetId)) {
            throw (new Exception("Invalid id passed in."));
        }
        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);
        Dataset dataset = datasetDao.findOne(datasetId);
        if (dataset == null) {
            throw (new Exception("Invalid id passed in."));
        }
        dataset.setDeleted(true);
        datasetDao.save(dataset);
    }

    /**
     * Delete dataset from repository
     * 
     * @param datasetId
     *            id of dataset to delete from repository
     * @return response message from delete operation
     */
    @PUT
    @Path("{dataset-id}/purge")
    public Response purgeDataset(@PathParam("dataset-id") @DefaultValue("") String datasetId) {
        if ("".equals(datasetId))
            return Response.status(500).entity("Must have dataset id").build();

        if (DatasetUtil.deleteDataset(datasetId))
            return Response.ok().build();
        else
            return Response.status(500).entity("Can't delete dataset: " + datasetId).build();
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
            return response.build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.status(500).entity("Can't find the file (id:" + fileDescriptorId + ") in dataset id: " + datasetId).build();
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
    @Path("{dataset-id}/{filedescriptor-id}/delete")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response deleteFile(@PathParam("dataset-id") String datasetId, @PathParam("filedescriptor-id") String fileDescriptorId) {
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
        dataset.getFileDescriptors().remove(fileDescriptor);
        datasetDao.save(dataset);
        return Response.ok().build();
    }

}
