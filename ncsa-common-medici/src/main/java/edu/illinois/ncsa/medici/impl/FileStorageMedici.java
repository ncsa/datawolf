/**
 * 
 */
package edu.illinois.ncsa.medici.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class FileStorageMedici implements FileStorage {
    private static Logger     logger = LoggerFactory.getLogger(FileStorageMedici.class);
    private String            server;
    private String            key;

    @Inject
    private FileDescriptorDao fileDescriptorDAO;

    public FileStorageMedici() {
        server = "http://localhost:9000/";
        setKey("");
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server
     *            the server to post to
     */
    public void setServer(String server) {
        if (server.endsWith("/")) {
            this.server = server;
        } else {
            this.server = server + "/";
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public FileDescriptor storeFile(InputStream is) throws IOException {
        return storeFile(new FileDescriptor().getId(), null, is);
    }

    public FileDescriptor storeFile(String filename, InputStream is) throws IOException {
        return storeFile(new FileDescriptor().getId(), filename, is);
    }

    public URL storeFile(FileDescriptor fd, InputStream is) throws IOException {
        FileDescriptor check = storeFile(fd.getId(), fd.getFilename(), is);

        // simple checks
        if ((fd.getMd5sum() != null) && !fd.getMd5sum().equals(check.getMd5sum())) {
            throw (new IOException("MD5SUM does not match, error saving data."));
        }
        if ((fd.getSize() > 0) && (fd.getSize() != check.getSize())) {
            throw (new IOException("File size does not match, error saving data."));
        }

        // set the url to the new url
        fd.setDataURL(check.getDataURL());

        return new URL(check.getDataURL());
    }

    public FileDescriptor storeFile(String id, String filename, InputStream is) throws IOException {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int len;
        long size = 0;
        byte[] buffer = new byte[10240];

        // open a URL connection
        URL url;
        if ((key == null) || key.trim().equals("")) {
            url = new URL(server + "api/files");
        } else {
            url = new URL(server + "api/files?key=" + key.trim());
        }

        // Open a HTTP connection to the URL
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Allow Inputs
        conn.setDoInput(true);

        // Allow Outputs
        conn.setDoOutput(true);

        // Don't use a cached copy.
        conn.setUseCaches(false);

        // Use a post method.
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

        // Send a binary file
        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"File\";filename=\"" + filename + "\"" + lineEnd);
        dos.writeBytes(lineEnd);

        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not create MD5 digest.", e);
        }

        // read file and write it into form...
        while ((len = is.read(buffer)) > 0) {
            dos.write(buffer, 0, len);
            if (md5 != null) {
                md5.update(buffer, 0, len);
            }
            size += len;
        }

        // send multipart form data necesssary after file data...
        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        // close streams
        is.close();
        dos.flush();

        // read result
        is = conn.getInputStream();
        String mediciid = "";
        while ((len = is.read(buffer)) > 0) {
            mediciid = mediciid + new String(buffer, 0, len);
        }
        mediciid = mediciid.replace("{\"id\":\"", "");
        mediciid = mediciid.replace("\"}", "");

        dos.close();
        is.close();
        conn.disconnect();

        FileDescriptor fd = new FileDescriptor();
        fd.setId(id);
        fd.setFilename(filename);
        fd.setMimeType(new MimetypesFileTypeMap().getContentType(filename));
        fd.setDataURL(server + "api/files/" + mediciid);
        if (md5 != null) {
            fd.setMd5sum(md5.digest());
        }
        fd.setSize(size);

        fileDescriptorDAO.save(fd);

        return fd;
    }

    public boolean deleteFile(FileDescriptor fd) {
        // not supported
        return false;
    }

    public InputStream readFile(FileDescriptor fd) throws IOException {
        return new URL(fd.getDataURL()).openStream();
    }
}
