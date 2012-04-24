package edu.illinois.ncsa.file.service;

import java.io.FileDescriptor;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/files")
public class FilesResource {
    /**
     * get all file-descriptors
     * 
     * @return
     *         list of FileDescriptor
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<FileDescriptor> getAllFileDescriptors() {
        return null;
    }

    /**
     * get a file-descriptor with given id
     * 
     * @param id
     *            file-desciptor id
     * @return
     *         a FileDescriptor
     */
    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public FileDescriptor getFileDescriptor(@PathParam("id") String id) {
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
    @Path("{id}/file")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response getFile(@PathParam("id") String id) {
        return null;
    }

    /**
     * upload a file
     * 
     * @param input
     *            form data with file and other information
     * @return
     *         generated new FileDescriptor
     */
    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public FileDescriptor uploadFile(MultipartFormDataInput input) {
        return null;
    }

    /**
     * delete the file with given id
     * 
     * @param id
     *            file-descriptor id
     * @return
     *         response with 200 and a message
     */
    @DELETE
    @Path("{id}")
    public Response deleteFile(@PathParam("id") String id) {
        return null;
    }
}
