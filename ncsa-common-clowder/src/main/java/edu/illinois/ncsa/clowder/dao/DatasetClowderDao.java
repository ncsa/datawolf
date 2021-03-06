package edu.illinois.ncsa.clowder.dao;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import edu.illinois.ncsa.clowder.ClowderRedirectStrategy;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class DatasetClowderDao extends AbstractClowderDao<Dataset, String> implements DatasetDao {
    private static final Logger logger = LoggerFactory.getLogger(DatasetClowderDao.class);

    @Inject
    private PersonDao           personDao;

    public Dataset save(Dataset dataset) {
        // Clowder does not support marking a dataset as deleted
        // We could forward this to the delete method
        if (dataset.isDeleted()) {
            logger.warn("dataset marked as deleted, which Clowder doesn't support, returning");
            return dataset;
        }

        String responseStr = null;

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRedirectStrategy(new ClowderRedirectStrategy());
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpclient = builder.build();

        ResponseHandler<String> responseHandler;
        String token = getToken(dataset.getCreator().getEmail());

        String clowderEndpoint = getServer();
        String key = getKey();
        String requestUrl = clowderEndpoint;
        try {
            // Check if dataset already exists, update is not supported
            requestUrl += "api/datasets/" + dataset.getId();
            if (token != null && !token.isEmpty()) {
                requestUrl += "api/datasets" + "?key=" + token;
            } else {
                requestUrl += "api/datasets?key=" + key.trim();
            }

            HttpGet httpGet = new HttpGet(requestUrl);

            responseHandler = new BasicResponseHandler();
            logger.debug("Executing request " + httpGet.getRequestLine());
            responseStr = httpclient.execute(httpGet, responseHandler);

        } catch (Exception e) {
            try {
                String name = dataset.getTitle();
                String description = dataset.getDescription();

                String fileId = dataset.getFileDescriptors().get(0).getDataURL();
                fileId = fileId.replace(clowderEndpoint + "api/files/", "");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", name);
                jsonObject.addProperty("description", description);
                jsonObject.addProperty("file_id", fileId);

                StringEntity params = new StringEntity(jsonObject.toString());
                requestUrl = clowderEndpoint;
                if (token != null && !token.isEmpty()) {
                    requestUrl += "api/datasets" + "?key=" + token;
                } else {
                    requestUrl += "api/datasets?key=" + key.trim();
                }

                // Create new dataset and associate files
                HttpPost httpPost = new HttpPost(requestUrl);
                httpPost.setEntity(params);
                httpPost.setHeader("content-type", "application/json");

                responseHandler = new BasicResponseHandler();
                responseStr = httpclient.execute(httpPost, responseHandler);

                JsonElement jsonElement = new JsonParser().parse(responseStr);

                // Returned ID from Clowder
                String id = jsonElement.getAsJsonObject().get("id").getAsString();
                dataset.setId(id);

                requestUrl = clowderEndpoint;
                // Add datawolf tag
                if (token != null && !token.isEmpty()) {
                    requestUrl += "api/datasets/" + dataset.getId() + "/tags" + "?key=" + token;
                } else {
                    requestUrl += "api/datasets/" + dataset.getId() + "/tags?key=" + key.trim();
                }

                httpPost = new HttpPost(requestUrl);

                jsonObject = new JsonObject();
                JsonArray array = new JsonArray();
                array.add(new JsonPrimitive("datawolf"));
                jsonObject.add("tags", array);
                jsonObject.addProperty("extractor_id", "add");

                params = new StringEntity(jsonObject.toString());
                httpPost.setEntity(params);
                httpPost.setHeader("content-type", "application/json");

                responseHandler = new BasicResponseHandler();
                responseStr = httpclient.execute(httpPost, responseHandler);
                logger.debug("Add Tag response: " + responseStr);

                // Attach additional files
                for (int idx = 1; idx < dataset.getFileDescriptors().size(); idx++) {
                    fileId = dataset.getFileDescriptors().get(idx).getDataURL();
                    fileId = fileId.replace(getServer() + "api/files/", "");

                    requestUrl = clowderEndpoint;
                    if (token != null && !token.isEmpty()) {
                        requestUrl += "api/datasets/" + dataset.getId() + "/files/" + fileId + "?key=" + token;
                    } else {
                        requestUrl += "api/datasets/" + dataset.getId() + "/files/" + fileId + "?key=" + key.trim();
                    }

                    httpPost = new HttpPost(requestUrl);
                    httpPost.setHeader("content-type", "text/plain");

                    responseHandler = new BasicResponseHandler();
                    responseStr = httpclient.execute(httpPost, responseHandler);

                    logger.debug("Attach file to dataset response: " + responseStr);
                }

            } catch (Exception e1) {
                logger.error("HTTP Create Dataset failed.", e1);
            } finally {
                try {
                    ((CloseableHttpClient) httpclient).close();
                } catch (IOException ignore) {
                    logger.warn("Error closing http client", ignore);
                }
            }
        }
        return dataset;
    }

    public List<Dataset> findAll() {
        List<Dataset> results = null;
        String responseStr = null;
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRedirectStrategy(new ClowderRedirectStrategy());
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpclient = builder.build();

        String clowderEndpoint = getServer();
        String key = getKey();
        String requestUrl = clowderEndpoint;
        try {
            if (key == null || key.trim().equals("")) {
                requestUrl += "api/datasets";
            } else {
                requestUrl += "api/datasets?key=" + key.trim();
            }
            HttpGet httpGet = new HttpGet(requestUrl);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            logger.debug("Executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                JsonElement jsonElement = new JsonParser().parse(responseStr);
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                results = new ArrayList<Dataset>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                for (int index = 0; index < jsonArray.size(); index++) {
                    JsonObject jsonObject = jsonArray.get(index).getAsJsonObject();
                    Dataset dataset = new Dataset();
                    String id = jsonObject.get("id").getAsString();
                    String title = jsonObject.get("name").getAsString();
                    String description = jsonObject.get("description").getAsString();
                    String dateString = jsonObject.get("created").getAsString();
                    String userId = jsonObject.get("authorId").getAsString();

                    Person creator = personDao.findOne(userId);

                    Date date = dateFormat.parse(dateString);
                    dataset.setId(id);
                    dataset.setDate(date);
                    dataset.setTitle(title);
                    dataset.setDescription(description);
                    dataset.setFileDescriptors(getFileDescriptor(id));
                    dataset.setCreator(creator);

                    results.add(dataset);

                }

                logger.debug("Response String: " + responseStr);
            } catch (Exception e) {
                logger.error("HTTP Get failed.", e);
            }

        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {
                logger.warn("Error closing http client", ignore);
            }
        }
        return results;
    }

    @Override
    public List<Dataset> findAll(int page, int size) {

        // TODO need to determine if Clowder has an API to indicate which page
        int limit = (page + 1) * size;

        List<Dataset> results = null;
        String responseStr = null;
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRedirectStrategy(new ClowderRedirectStrategy());
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpclient = builder.build();

        String clowderEndpoint = getServer();
        String key = getKey();
        String requestUrl = clowderEndpoint;
        try {
            if (key == null || key.trim().equals("")) {
                requestUrl += "api/datasets?limit=" + limit;
            } else {
                requestUrl += "api/datasets?key=" + key.trim() + "&limit=" + limit;
            }
            HttpGet httpGet = new HttpGet(requestUrl);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            logger.debug("Executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                JsonElement jsonElement = new JsonParser().parse(responseStr);
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                results = new ArrayList<Dataset>();

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                for (int index = page * size; index < jsonArray.size(); index++) {
                    JsonObject jsonObject = jsonArray.get(index).getAsJsonObject();
                    Dataset dataset = new Dataset();
                    String id = jsonObject.get("id").getAsString();
                    String title = jsonObject.get("name").getAsString();
                    String description = jsonObject.get("description").getAsString();
                    String dateString = jsonObject.get("created").getAsString();
                    String userId = jsonObject.get("authorId").getAsString();

                    Person creator = personDao.findOne(userId);

                    Date date = dateFormat.parse(dateString);
                    dataset.setId(id);
                    dataset.setDate(date);
                    dataset.setTitle(title);
                    dataset.setDescription(description);
                    dataset.setFileDescriptors(getFileDescriptor(id));
                    dataset.setCreator(creator);

                    results.add(dataset);

                }

                logger.debug("Response String: " + responseStr);
            } catch (Exception e) {
                logger.error("HTTP Get failed.", e);
            }

        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {
                logger.warn("Error closing http client", ignore);
            }
        }
        return results;
    }

    public Dataset findOne(String id) {
        // TODO replace with clowder endpoint api/datasets/:id
        List<Dataset> results = findAll();
        for (Dataset dataset : results) {
            if (dataset.getId().equals(id)) {
                return dataset;
            }
        }

        return null;
    }

    private List<FileDescriptor> getFileDescriptor(String id) {

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRedirectStrategy(new ClowderRedirectStrategy());
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpclient = builder.build();

        String clowderEndpoint = getServer();
        String key = getKey();
        String requestUrl = clowderEndpoint;

        if (key == null || key.trim().equals("")) {
            requestUrl += "api/datasets/" + id + "/listFiles";
        } else {
            requestUrl += "api/datasets/" + id + "/listFiles?key=" + key.trim();
        }

        HttpGet httpGet = new HttpGet(requestUrl);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        logger.debug("Executing request " + httpGet.getRequestLine());

        String responseStr = null;
        List<FileDescriptor> results = new ArrayList<FileDescriptor>();
        try {
            responseStr = httpclient.execute(httpGet, responseHandler);
            JsonElement jsonElement = new JsonParser().parse(responseStr);
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int index = 0; index < jsonArray.size(); index++) {
                JsonObject jsonObject = jsonArray.get(index).getAsJsonObject();
                FileDescriptor fileDescriptor = new FileDescriptor();

                String fdId = jsonObject.get("id").getAsString();
                String filename = jsonObject.get("filename").getAsString();
                String mimetype = jsonObject.get("contentType").getAsString();
                fileDescriptor.setFilename(filename);
                fileDescriptor.setMimeType(mimetype);

                String dataUrl = clowderEndpoint + "api/files/" + fdId;
                fileDescriptor.setId(fdId);
                fileDescriptor.setDataURL(dataUrl);

                results.add(fileDescriptor);
            }
        } catch (Exception e) {
            logger.error("HTTP Get failed.", e);
        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {
                logger.warn("Error closing http client", ignore);
            }
        }

        return results;

    }

    public void delete(Dataset dataset) {
        HttpClient httpclient = null;
        try {
            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setRedirectStrategy(new ClowderRedirectStrategy());
            RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
            builder.setDefaultRequestConfig(config);
            httpclient = builder.build();

            ResponseHandler<String> responseHandler;
            String token = getToken(dataset.getCreator().getEmail());

            String clowderEndpoint = getServer();
            String key = getKey();
            String requestUrl = clowderEndpoint;

            if (token != null && !token.isEmpty()) {
                requestUrl += "api/datasets/" + dataset.getId() + "?key=" + token;
            } else {
                requestUrl += "api/datasets/" + dataset.getId() + "?key=" + key.trim();
            }

            HttpDelete httpDelete = new HttpDelete(requestUrl);

            responseHandler = new BasicResponseHandler();
            String responseStr = httpclient.execute(httpDelete, responseHandler);
            logger.debug("Delete response: " + responseStr);
        } catch (Exception e) {
            logger.error("Could not delete dataset. ", e);
        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {
                logger.warn("Error closing http client", ignore);
            }
        }
    }

    @Override
    public List<Dataset> findByDeleted(boolean deleted) {
        return findAll();
    }

    @Override
    public List<Dataset> findByTitleLike(String titlePattern) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmail(String email) {
        return findAll();
    }

    @Override
    public List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        return findAll();
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByDeleted(boolean deleted, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByTitleLike(String titlePattern, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmail(String email, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }

}
