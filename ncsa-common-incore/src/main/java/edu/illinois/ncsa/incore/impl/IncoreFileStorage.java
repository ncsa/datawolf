package edu.illinois.ncsa.incore.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.incore.IncoreDataset;

public class IncoreFileStorage implements FileStorage {
    private static final Logger logger = LoggerFactory.getLogger(IncoreFileStorage.class);

    @Inject
    @Named("incore.server")
    private String              server;

    @Inject
    @Named("incore.group")
    private String              group;

    @Inject(optional = true)
    private AccountDao          accountDao;

    @Override
    public FileDescriptor storeFile(InputStream is) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileDescriptor storeFile(String filename, InputStream is) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileDescriptor storeFile(String filename, InputStream is, Person creator, Dataset ds) throws IOException {
        return storeFile(null, filename, is, creator, ds);
    }

    @Override
    public URL storeFile(FileDescriptor fd, InputStream is) throws IOException {
        return null;
    }

    @Override
    public FileDescriptor storeFile(String id, String filename, InputStream is) throws IOException {
        return null;
    }

    @Override
    public FileDescriptor storeFile(String id, String filename, InputStream is, Person creator, Dataset ds) throws IOException {
        logger.debug("Store the file on the IN-CORE service");
        // Uncomment this for testing locally
//        String bearerToken = getToken();

        String incoreEndpoint = server;
        if (!incoreEndpoint.endsWith("/")) {
            incoreEndpoint += "/";
        }

        String requestUrl = incoreEndpoint + IncoreDataset.DATASETS_ENDPOINT + "/" + ds.getId();

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        logger.debug("Filename in storeFile is "+filename);

        // This was an old Hack to update the source dataset, but doesn't work with the current pyincore
        // This might be something to handle on the frontend when visualizing the outputs and updating the parent from
        // the execution
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("property name", "sourceDataset");
//        jsonObject.addProperty("property value", datasetId);
//
//        MultipartEntityBuilder paramBuilder = MultipartEntityBuilder.create();
//        paramBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//        paramBuilder.addTextBody("update", jsonObject.toString());
//
//        HttpPut httpPut = new HttpPut(requestUrl);
//        httpPut.setEntity(paramBuilder.build());
//
//        if (creator != null) {
//            httpPut.setHeader("X-Credential-Username", creator.getId());
//        }
//
        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

//        response = httpclient.execute(httpPut);
//        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//            logger.warn("failed to update parent dataset - " + response);
//        }

        // Add the file to the dataset and update the name
        requestUrl = incoreEndpoint;
        requestUrl += IncoreDataset.DATASETS_ENDPOINT + "/" + ds.getId() + "/" + IncoreDataset.DATASET_FILES;

        String updateRequestUrl = incoreEndpoint + IncoreDataset.DATASETS_ENDPOINT + "/" + ds.getId();

        String responseStr = "";
        if (filename != null && !filename.isEmpty()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("property name", "title");
            jsonObject.addProperty("property value", filename);

            MultipartEntityBuilder params = MultipartEntityBuilder.create();
            params.addTextBody("update", jsonObject.toString());

            HttpPut httpPut = new HttpPut(updateRequestUrl);
            httpPut.setEntity(params.build());

            if (creator != null) {
                // Uncomment this for testing locally
//                httpPut.setHeader("Authorization", bearerToken);

                String creatorGroup = "[\""+ this.group + "\"]";
                String creatorId = creator.getEmail();
                httpPut.setHeader(IncoreDataset.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creatorId + "\"}");
                httpPut.setHeader(IncoreDataset.X_AUTH_USERGROUP, "{\"groups\": " + creatorGroup + "}" );

            }

            response = httpclient.execute(httpPut);
            responseStr = responseHandler.handleResponse(response);
            System.out.println(responseStr);
            logger.debug("Updating dataset name on the IN-CORE service to "+filename);
            logger.debug("Response is " + responseStr);
        }

        MultipartEntityBuilder paramBuilder = MultipartEntityBuilder.create();
        paramBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//        paramBuilder.addTextBody(IncoreDataset.PARENT_DATASET, jsonObject.toString());
        paramBuilder.addBinaryBody(IncoreDataset.DATASET_FILE, is, ContentType.DEFAULT_BINARY, filename);

        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setEntity(paramBuilder.build());
        if (creator != null) {
            // Uncomment this for testing locally
//            httpPost.setHeader("Authorization", bearerToken);

            String creatorGroup = "[\""+ this.group + "\"]";
            String creatorId = creator.getEmail();
            httpPost.setHeader(IncoreDataset.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creatorId + "\"}");
            httpPost.setHeader(IncoreDataset.X_AUTH_USERGROUP, "{\"groups\": " + creatorGroup + "}" );

        }

        response = httpclient.execute(httpPost);
        responseStr = responseHandler.handleResponse(response);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JsonElement jsonElement = new JsonParser().parse(responseStr);
            JsonObject datasetProperties = jsonElement.getAsJsonObject();
            if (datasetProperties != null) {
                Dataset dataset = IncoreDataset.getDataset(datasetProperties, null);
                int size = dataset.getFileDescriptors().size();
                return dataset.getFileDescriptors().get(size - 1);
            }

        } else {
            logger.error("Error attaching file to dataset. " + responseStr);
        }

        return null;
    }

    @Override
    public InputStream readFile(FileDescriptor fd) throws IOException {
        // Uncomment for local testing
//        String bearerToken = getToken();
        String incoreEndpoint = server;
        if (!incoreEndpoint.endsWith("/")) {
            incoreEndpoint += "/";
        }
        String requestUrl = incoreEndpoint + IncoreDataset.FILES_ENDPOINT + "/" + fd.getId() + "/" + IncoreDataset.FILE_BLOB;
        logger.debug("request is " + requestUrl);

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String creatorId = "";
        String creatorGroup = "[\""+ this.group + "\"]";

        HttpGet httpGet = new HttpGet(requestUrl);
        // Uncomment for local testing
//        httpGet.setHeader("Authorization", bearerToken);
        httpGet.setHeader(IncoreDataset.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creatorId + "\"}");
        httpGet.setHeader(IncoreDataset.X_AUTH_USERGROUP, "{\"groups\": " + creatorGroup + "}" );

        HttpResponse response = httpclient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = response.getEntity();
            return entity.getContent();
        } else {
            logger.error("Error getting file input stream for " + fd.getId() + ", service responded with status code " + response.getStatusLine().getStatusCode());
            return null;
        }
    }

    @Override
    public boolean deleteFile(FileDescriptor fd) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteFile(FileDescriptor fd, Person creator) {
        // TODO Auto-generated method stub
        return false;
    }

    // Helper method for testing with a local instance
    public String getToken() {
        String server = this.server;
        // pyincore uses the server URL without ending in / when creating the token name
        if(server.endsWith("/")) {
            server = server.substring(0, server.length() - 1);
        }

        String tokenFile = "." + DigestUtils.sha256Hex(server) + "_token";
        String bearerToken = null;
        try {
            String userHome = System.getProperty("user.home");
            Path path = Paths.get(userHome + "/.incore/" + tokenFile);
            if (Files.exists(path)) {
                byte[] encoded = Files.readAllBytes(path);
                bearerToken = new String(encoded, StandardCharsets.UTF_8);
            }
        } catch(IOException e) {
            logger.error("Error getting token", e);
        }
        return bearerToken;
    }

}
