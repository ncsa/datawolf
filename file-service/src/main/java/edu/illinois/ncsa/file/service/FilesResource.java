package edu.illinois.ncsa.file.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

@Path("/files")
public class FilesResource {

    @Inject
    private FileDescriptorDao fileDescriptorDAO;

    @Inject
    private FileStorage       fileStorage;

    /**
     * get all file-descriptors
     * 
     * @return
     *         list of FileDescriptor
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<FileDescriptor> getAllFileDescriptors(@QueryParam("size") @DefaultValue("100") int size, @QueryParam("page") @DefaultValue("0") int page) {
        return fileDescriptorDAO.findAll();
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
    @Path("form")
    @Produces({ MediaType.TEXT_HTML })
    public String getForm() {
        return "<html><body><form action='/files' method='post' enctype='multipart/form-data'><input type='file' name='uploadedFile' /><br/><input type='submit' /></form></body></html>";
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
        return fileDescriptorDAO.findOne(id);
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
        FileDescriptor file = fileDescriptorDAO.findOne(id);
        try {
            InputStream is = fileStorage.readFile(file);
            ResponseBuilder response = Response.ok(is);
            if (file.getMimeType().equals("")) {
                response.type("application/unknown");
            } else {
                response.type(file.getMimeType());
            }
            if (file.getFilename() != null) {
                response.header("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"");
            }
            // logger.debug("Downloading dataset " + decoded);
            return response.build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.status(500).entity("Can't find the file (id:" + id + ")").build();
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
    public FileDescriptor uploadFile(MultipartFormDataInput input) throws IOException {

        FileDescriptor fileDescriptor = null;
        String fileName = "";

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("uploadedFile");

        for (InputPart inputPart : inputParts) {
            MultivaluedMap<String, String> header = inputPart.getHeaders();
            fileName = getFileName(header);

            // convert the uploaded file to inputstream
            InputStream inputStream = inputPart.getBody(InputStream.class, null);

            // Store the file
            fileDescriptor = fileStorage.storeFile(fileName, inputStream);
        }

        // return
// Response.status(200).entity("uploadFile is called, Uploaded file name : " +
// fileName).build();
        return fileDescriptor;

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

    /**
     * header sample
     * {
     * Content-Type=[image/png],
     * Content-Disposition=[form-data; name="file";
     * filename="filename.extension"]
     * }
     **/
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

}
