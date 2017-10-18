package edu.illinois.ncsa.incore.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
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
        // TODO we should clean up this temporary file
        // Write file to temp file to remove source dataset id
        File cwd = File.createTempFile("cib", ".dir"); //$NON-NLS-1$ //$NON-NLS-2$
        cwd.delete();
        if (!cwd.mkdirs()) {
            throw (new IOException("Error creating temp directory."));
        }

        File output = new File(cwd, filename);
        String datasetId = null;
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(is)); BufferedWriter bw = new BufferedWriter(new FileWriter(output));) {
            datasetId = br.readLine();
            String line = null;

            while ((line = br.readLine()) != null) {
                bw.write(line + "\n");
            }
        }

        String incoreEndpoint = server;
        if (!incoreEndpoint.endsWith("/")) {
            incoreEndpoint += "/";
        }

        String requestUrl = incoreEndpoint + IncoreDataset.DATASETS_ENDPOINT + "/" + IncoreDataset.UPDATE;

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        // Hack to update the source dataset
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("datasetId", ds.getId());
        jsonObject.addProperty("property name", "sourceDataset");
        jsonObject.addProperty("property value", datasetId);

        MultipartEntityBuilder paramBuilder = MultipartEntityBuilder.create();
        paramBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        paramBuilder.addTextBody("update", jsonObject.toString());

        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setEntity(paramBuilder.build());

        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        response = httpclient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            logger.warn("failed to update parent dataset");
        }

        // Add the file to the dataset
        requestUrl = incoreEndpoint;
        requestUrl += IncoreDataset.DATASETS_ENDPOINT + "/" + IncoreDataset.ADD_DATASET_FILES;

        jsonObject = new JsonObject();
        jsonObject.addProperty("datasetId", ds.getId());

        paramBuilder = MultipartEntityBuilder.create();
        paramBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        paramBuilder.addTextBody("metadata", jsonObject.toString());
        paramBuilder.addBinaryBody("file", output, ContentType.DEFAULT_BINARY, filename);

        httpPost = new HttpPost(requestUrl);
        httpPost.setEntity(paramBuilder.build());

        response = httpclient.execute(httpPost);
        String responseStr = responseHandler.handleResponse(response);
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
        String requestUrl = fd.getDataURL();
        return new URL(requestUrl).openStream();
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

}
