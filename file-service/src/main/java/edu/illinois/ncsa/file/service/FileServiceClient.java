package edu.illinois.ncsa.file.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.FileDescriptor;

public class FileServiceClient {

    private static Logger logger = LoggerFactory.getLogger(FileServiceClient.class);

    public static String  SERVER = "";

    public static FileDescriptor post(File file) {
        String responseStr = null;
        FileDescriptor fd = null;

        HttpClient httpclient = new DefaultHttpClient();

        try {
            String requestUrl = SERVER + "/files";
            HttpPost httppost = new HttpPost(requestUrl);

            FileBody bin = new FileBody(file);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("uploadedFile", bin);

            httppost.setEntity(reqEntity);

            logger.info("executing request " + httppost.getRequestLine());

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            try {
                responseStr = httpclient.execute(httppost, responseHandler);
                logger.debug("response string" + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                fd = mapper.readValue(responseStr, FileDescriptor.class);
            } catch (Exception e) {
                logger.error("HTTP post failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return fd;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static FileDescriptor get(String id) {
        String responseStr = null;
        FileDescriptor fd = null;

        HttpClient httpclient = new DefaultHttpClient();

        try {
            String requestUrl = SERVER + "/files/" + id;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                fd = mapper.readValue(responseStr, FileDescriptor.class);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return fd;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static InputStream getFile(String id) {
        HttpResponse response = null;
        InputStream is = null;

        HttpClient httpclient = new DefaultHttpClient();

        try {
            String requestUrl = SERVER + "/files/" + id + "/file";
            HttpGet httpGet = new HttpGet(requestUrl);

            logger.info("executing request " + httpGet.getRequestLine());

            try {
                response = httpclient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                String tmpdir = System.getProperty("java.io.tmpdir");
                File tmpFile = new File(tmpdir, UUID.randomUUID().toString());
                FileOutputStream fos = new FileOutputStream(tmpFile);
                entity.writeTo(fos);
                fos.close();
                logger.debug("File is written at " + tmpFile.getAbsolutePath());
                is = new FileInputStream(tmpFile);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return is;
            } catch (Exception ignore) {}
        }
        return null;
    }
}
