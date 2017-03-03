/**
 * 
 */
package edu.illinois.ncsa.clowder.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.clowder.ClowderRedirectStrategy;
import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class FileStorageClowder implements FileStorage {
    private static Logger     logger = LoggerFactory.getLogger(FileStorageClowder.class);
    @Inject
    @Named("clowder.server")
    private String            server;
    @Inject
    @Named("clowder.key")
    private String            key;

    @Inject
    private FileDescriptorDao fileDescriptorDAO;

    @Inject(optional = true)
    private AccountDao        accountDao;

    public FileStorageClowder() {}

    public String getServer() {
        // Make sure the injected service ends with a slash
        if (!server.endsWith("/")) {
            this.server += "/";
        }
        return server;
    }

    public FileDescriptor storeFile(InputStream is) throws IOException {
        return storeFile(new FileDescriptor().getId(), null, is);
    }

    public FileDescriptor storeFile(String filename, InputStream is, Person creator) throws IOException {
        return storeFile(new FileDescriptor().getId(), filename, is, creator);
    }

    public FileDescriptor storeFile(String filename, InputStream is) throws IOException {
        return storeFile(filename, is, null);
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

    public FileDescriptor storeFile(FileDescriptor fd, String filename, InputStream is) throws IOException {
        return storeFile(fd.getId(), filename, is);
    }

    @Override
    public FileDescriptor storeFile(String id, String filename, InputStream is) throws IOException {
        return storeFile(id, filename, is, null);
    }

    public FileDescriptor storeFile(String id, String filename, InputStream is, Person creator) throws IOException {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int len;
        long size = 0;
        byte[] buffer = new byte[10240];

        String token = creator != null ? getToken(creator.getEmail()) : null;

        // open a URL connection
        URL url;
        if (token != null || key == null || key.trim().equals("")) {
            url = new URL(getServer() + "api/files");
        } else {
            url = new URL(getServer() + "api/files?key=" + key.trim());
        }

        // Open a HTTP connection to the URL
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Check connection for redirection
        int status = conn.getResponseCode();
        boolean redirect = false;
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true;
            }
        }

        // Close connection after checking for redirection
        conn.disconnect();

        if (redirect) {
            // Update URL from server response
            String redirectUrl = conn.getHeaderField("Location");
            conn = (HttpURLConnection) new URL(redirectUrl).openConnection();
        } else {
            // Open a new connection to the server
            conn = (HttpURLConnection) url.openConnection();
        }

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

        if (token != null) {
            conn.setRequestProperty("Authorization", "Basic " + token);
        }

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
        fd.setDataURL(getServer() + "api/files/" + mediciid);
        if (md5 != null) {
            fd.setMd5sum(md5.digest());
        }
        fd.setSize(size);

        fileDescriptorDAO.save(fd);

        return fd;
    }

    public boolean deleteFile(FileDescriptor fd) {
        return deleteFile(fd, null);
    }

    public boolean deleteFile(FileDescriptor fd, Person creator) {
        logger.debug("Deleting file in Clowder");
        if (fd.getDataURL() != null) {
            String token = creator != null ? getToken(creator.getEmail()) : null;

            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setRedirectStrategy(new ClowderRedirectStrategy());
            HttpClient httpClient = builder.build();

            String requestUrl = null;
            if (token != null || key == null || key.trim().equals("")) {
                requestUrl = fd.getDataURL();
            } else {
                requestUrl = fd.getDataURL() + "?key=" + key.trim();
            }

            HttpDelete httpDelete = new HttpDelete(requestUrl);

            if (token != null) {
                httpDelete.setHeader("Authorization", "Basic " + token);
            }

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseString = null;
            try {
                responseString = httpClient.execute(httpDelete, responseHandler);
                logger.debug("Response: " + responseString);
                return true;
            } catch (Exception e) {
                logger.error("HTTP Delete failed.", e);
            }
        }
        return false;
    }

    public InputStream readFile(FileDescriptor fd) throws IOException {
        String requestUrl = fd.getDataURL();
        if (key != null || !key.trim().equals("")) {
            requestUrl += "?key=" + key.trim();
        }
        return new URL(requestUrl).openStream();
    }

    protected String getToken(String userId) {
        String token = null;
        if (accountDao != null) {
            Account acct = accountDao.findByUserid(userId);
            if (acct != null) {
                token = new String(Base64.encodeBase64(new String(acct.getUserid() + ":" + acct.getPassword()).getBytes()));
            }
        }
        return token;
    }

}