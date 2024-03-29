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

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.ImportExport;
import edu.illinois.ncsa.datawolf.service.utils.LoginUtil;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.domain.util.DatasetUtil;

@Path("/datasets")
public class DatasetsResource {

    @Inject
    private AccountDao          accountDao;

    @Inject
    private PersonDao           personDao;

    @Inject
    private DatasetDao          datasetDao;

    @Inject
    private FileStorage         fileStorage;

    @Inject
    @Named("dataset.permissions")
    // default view permissions for datasets
    private String              permissions = "private";

    private static final Logger log         = LoggerFactory.getLogger(DatasetsResource.class);

    /**
     * 
     * Create dataset from zip file. It expects the following form:
     * <form action="/datasets" method="post" enctype="multipart/form-data">
     * <input type="file" name="uploadedFile" />
     * <input type="text" name="title" />
     * <input type="text" name="description" />
     * <input type="text" name="useremail" />
     * <input type="submit" value="Upload It" />
     * </form>
     * 
     * @param input
     *            a dataset created from Zip or from a file(s)
     * @return
     *         datasetId
     */
    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.TEXT_PLAIN })
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

        // Check if virtual path
        String virtualPath = null;
        if (uploadForm.containsKey("virtualpath")) {
            List<InputPart> formPath = uploadForm.get("virtualpath");
            // Handles special case where file is a URI (e.g. irods:// )
            if (formPath != null) {
                for (InputPart inputPart : formPath) {
                    try {
                        virtualPath = inputPart.getBody(String.class, null);
                    } catch (IOException e) {
                        // Nothing to log since a virtual path is a special case
                    }
                }
            }
        }

        if (fileInputParts != null || virtualPath != null) {
            List<InputPart> userInputParts = uploadForm.get("useremail");

            if (userInputParts == null)
                return null;

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

            List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
            if (virtualPath != null) {
                FileDescriptor fileDescriptor = null;
                try {
                    fileDescriptor = fileStorage.storeFile(virtualPath, null);

                    if (fileDescriptor == null) {
                        log.warn("Could not store the file");
                    } else {
                        fileDescriptors.add(fileDescriptor);
                    }
                } catch (IOException e) {
                    log.error("Error storing virtual file", e);
                    return null;
                }
            } else {
                for (InputPart inputPart : fileInputParts) {
                    try {
                        MultivaluedMap<String, String> header = inputPart.getHeaders();
                        String fileName = getFileName(header);

                        // convert the uploaded file to inputstream
                        InputStream inputStream = inputPart.getBody(InputStream.class, null);

                        // Store the file
                        FileDescriptor fileDescriptor = fileStorage.storeFile(fileName, inputStream, creator, null);
                        if (fileDescriptor == null) {
                            log.warn("Could not store the file");
                        } else {
                            fileDescriptors.add(fileDescriptor);
                        }
                    } catch (IOException e) {
                        log.warn("Could not parse the file", e);
                        return null;
                    }
                }
            }

            String title = "";
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
                        // If no title specified, use the first file in the list
                        if (fileDescriptors.size() > 0) {
                            title = fileDescriptors.get(0).getFilename();
                        }

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
            dataset.setTitle(title);
            dataset.setDescription(description);

            for (FileDescriptor fileDescriptor : fileDescriptors) {
                dataset.addFileDescriptor(fileDescriptor);
            }

            datasetDao.save(dataset);
            log.debug("Dataset uploaded");

            return dataset.getId();
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
     * Get all user datasets
     * 
     * @param size
     *            number of datasets per page
     * @param page
     *            page number starting 0
     * @param pattern
     *            filename pattern such as %.msh
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Dataset> getDatasets(@Context HttpRequest request, @QueryParam("size") @DefaultValue("100") int size, @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pattern") @DefaultValue("") String pattern, @QueryParam("showdeleted") @DefaultValue("false") boolean showdeleted) {
        // TODO add sort capability

        // Check request headers for user information.
        // For now, only users can get their own data
        String email = LoginUtil.getUserInfo(accountDao, request.getHttpHeaders());

        // TODO open Jira issue to allow admins to see all data
        // Eventually we should add support so users can give access to their data

        Iterable<Dataset> results = null;
        // with paging
        if (pattern.equals("")) {
            if (showdeleted) {
                results = datasetDao.findByCreatorEmail(email, page, size);
            } else {
                results = datasetDao.findByCreatorEmailAndDeleted(email, false, page, size);
            }
        } else {
            if (showdeleted) {
                results = datasetDao.findByCreatorEmailAndTitleLike(email, pattern, page, size);
            } else {
                results = datasetDao.findByCreatorEmailAndTitleLikeAndDeleted(email, pattern, false, page, size);
            }
        }

        List<Dataset> list = new ArrayList<Dataset>();
        results.forEach(list::add);

        return list;
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
    public Dataset getDataset(@Context HttpRequest request, @PathParam("dataset-id") String datasetId) {
        Dataset dataset = datasetDao.findOne(datasetId);
        if (dataset == null) {
            return null;
        }

        if (permissions.equalsIgnoreCase("private") && !isAuthorized(request, dataset)) {
            throw new NotAuthorizedException("You are not authorized to view this dataset", Response.status(Response.Status.UNAUTHORIZED));
        }

        return dataset;
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
    @Produces({ MediaType.APPLICATION_JSON })
    public boolean deleteDataset(@Context HttpRequest request, @PathParam("dataset-id") @DefaultValue("") String datasetId) throws Exception {
        if ("".equals(datasetId)) {
            throw (new Exception("Invalid id passed in."));
        }
        Dataset dataset = datasetDao.findOne(datasetId);
        if (dataset == null) {
            throw (new Exception("Invalid id passed in."));
        }

        if (!isAuthorized(request, dataset)) {
            throw new NotAuthorizedException("You are not authorized to delete this dataset", Response.status(Response.Status.UNAUTHORIZED));
        }

        dataset.setDeleted(true);
        datasetDao.save(dataset);

        return true;
    }

    /**
     * Delete dataset from repository
     * 
     * @param datasetId
     *            id of dataset to delete from repository
     * @return response message from delete operation
     */
    @DELETE
    @Path("{dataset-id}/purge")
    public Response purgeDataset(@Context HttpRequest request, @PathParam("dataset-id") @DefaultValue("") String datasetId) {
        if ("".equals(datasetId)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Must have dataset id").build();
        }

        Dataset dataset = datasetDao.findOne(datasetId);

        if (dataset == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!isAuthorized(request, dataset)) {
            throw new NotAuthorizedException("You are not authorized to delete this dataset", Response.status(Response.Status.UNAUTHORIZED));
        }

        if (DatasetUtil.deleteDataset(datasetId)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't delete dataset: " + datasetId).build();
        }
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
    public Response getDatasetZip(@Context HttpRequest request, @PathParam("dataset-id") String datasetId) {
        try {
            Dataset dataset = datasetDao.findOne(datasetId);

            if (dataset == null) {
                Response.status(Response.Status.NOT_FOUND).build();
            }

            if (permissions.equalsIgnoreCase("private") && !isAuthorized(request, dataset)) {
                throw new NotAuthorizedException("You are not authorized to view this dataset", Response.status(Response.Status.UNAUTHORIZED));
            }

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
            log.error("Error zipping dataset", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't delete dataset: " + datasetId).build();
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
    public FileDescriptor getFileDescriptor(@Context HttpRequest request, @PathParam("dataset-id") String datasetId, @PathParam("filedescriptor-id") String fileDescriptorId) {
        Dataset dataset = datasetDao.findOne(datasetId);

        if (dataset == null) {
            return null;
        }

        if (permissions.equalsIgnoreCase("private") && !isAuthorized(request, dataset)) {
            throw new NotAuthorizedException("You are not authorized to view this dataset", Response.status(Response.Status.UNAUTHORIZED));
        }

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
    public Response getFile(@Context HttpRequest request, @PathParam("dataset-id") String datasetId, @PathParam("filedescriptor-id") String fileDescriptorId) {
        Dataset dataset = datasetDao.findOne(datasetId);
        if (dataset == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        if (permissions.equalsIgnoreCase("private") && !isAuthorized(request, dataset)) {
            throw new NotAuthorizedException("You are not authorized to view this dataset", Response.status(Response.Status.UNAUTHORIZED));
        }

        List<FileDescriptor> fileDescriptors = dataset.getFileDescriptors();
        FileDescriptor fileDescriptor = null;
        for (FileDescriptor fd : fileDescriptors) {
            if (fd.getId().equals(fileDescriptorId)) {
                fileDescriptor = fd;
                break;
            }
        }

        if (fileDescriptor == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Can't find the file (id:" + fileDescriptorId + ") in dataset id: " + datasetId).build();
        }

        // FileStorage fileStorage = SpringData.getFileStorage();

        try {
            final InputStream is = fileStorage.readFile(fileDescriptor);

            StreamingOutput stream = new StreamingOutput() {
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        byte[] buf = new byte[10240];
                        int len = 0;
                        while ((len = is.read(buf)) >= 0) {
                            output.write(buf, 0, len);
                        }
                        output.close();
                    } catch (Exception e) {
                        throw new WebApplicationException(e);
                    }
                }
            };

            ResponseBuilder response = Response.ok(stream);
            if (fileDescriptor.getMimeType().equals("")) {
                response.type("application/unknown");
            } else {
                response.type(fileDescriptor.getMimeType());
            }
            response.header("Content-Disposition", "attachment; filename=\"" + fileDescriptor.getFilename() + "\"");
            return response.build();
        } catch (IOException e) {
            log.error("Can't read the file (id:" + fileDescriptorId + ") in dataset id: " + datasetId);
        } catch (WebApplicationException e) {
            log.error("Error streaming the file (id:" + fileDescriptorId + ") in dataset id: " + datasetId);
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't find the file (id:" + fileDescriptorId + ") in dataset id: " + datasetId).build();
    }

    /**
     * get the file with given id
     *
     * @param id
     *            file-descriptor id
     * @return
     *         file
     */
    @DELETE
    @Path("{dataset-id}/{filedescriptor-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response deleteFile(@Context HttpRequest request, @PathParam("dataset-id") String datasetId, @PathParam("filedescriptor-id") String fileDescriptorId) {
        Dataset dataset = datasetDao.findOne(datasetId);

        if (dataset == null) {
            Response.status(Response.Status.NOT_FOUND).build();
        }

        // Eventually we should add support so users can give access to their data
        if (!isAuthorized(request, dataset)) {
            throw new NotAuthorizedException("You are not authorized to modify this dataset", Response.status(Response.Status.UNAUTHORIZED));
        }

        List<FileDescriptor> fileDescriptors = dataset.getFileDescriptors();
        FileDescriptor fileDescriptor = null;
        for (FileDescriptor fd : fileDescriptors) {
            if (fd.getId().equals(fileDescriptorId)) {
                fileDescriptor = fd;
                break;
            }
        }

        if (fileDescriptor == null)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't find the file (id:" + fileDescriptorId + ") in dataset id: " + datasetId).build();
        dataset.getFileDescriptors().remove(fileDescriptor);
        datasetDao.save(dataset);
        return Response.ok().build();
    }

    private boolean isAuthorized(HttpRequest request, Dataset dataset) {
        // Check request headers for user information.
        String userInfo = LoginUtil.getUserInfo(accountDao, request.getHttpHeaders());

        String email = dataset.getCreator().getEmail();
        if (email.equals(userInfo)) {
            return true;
        }

        return false;
    }

}
