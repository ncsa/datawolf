package edu.illinois.ncsa.medici.dao;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
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
import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.medici.MediciRedirectStrategy;

public class DatasetMediciDao extends AbstractMediciDao<Dataset, String> implements DatasetDao {
    private static final Logger logger = LoggerFactory.getLogger(DatasetMediciDao.class);

    @Inject
    @Named("medici.server")
    private String              SERVER = "http://localhost:9000/";

    @Inject
    @Named("medici.key")
    private String              key;

    public Dataset save(Dataset dataset) {
        String responseStr = null;

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRedirectStrategy(new MediciRedirectStrategy());
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpclient = builder.build();

        ResponseHandler<String> responseHandler;

        try {
            String requestUrl = SERVER + "api/datasets/" + dataset.getId();
            HttpGet httpGet = new HttpGet(requestUrl);

            responseHandler = new BasicResponseHandler();
            logger.debug("Executing request " + httpGet.getRequestLine());
            responseStr = httpclient.execute(httpGet, responseHandler);

        } catch (Exception e) {
            try {
                String name = dataset.getTitle();
                String description = dataset.getDescription();

                // TODO add support for multiple files in a dataset
                String fileId = dataset.getFileDescriptors().get(0).getDataURL();
                fileId = fileId.replace(SERVER + "api/files/", "");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", name);
                jsonObject.addProperty("description", description);
                jsonObject.addProperty("file_id", fileId);

                StringEntity params = new StringEntity(jsonObject.toString());
                String requestUrl;
                if ((key == null) || key.trim().equals("")) {
                    requestUrl = SERVER + "api/datasets";
                } else {
                    requestUrl = SERVER + "api/datasets?key=" + key.trim();
                }
                logger.debug("REQUEST URL = " + requestUrl);

                // Create new dataset and associate files
                HttpPost httpPost = new HttpPost(requestUrl);
                httpPost.setEntity(params);
                httpPost.setHeader("content-type", "application/json");

                responseHandler = new BasicResponseHandler();
                responseStr = httpclient.execute(httpPost, responseHandler);

                JsonElement jsonElement = new JsonParser().parse(responseStr);

                // Returned ID from Medici
                String id = jsonElement.getAsJsonObject().get("id").getAsString();
                dataset.setId(id);

                // Add datawolf tag
                if ((key == null) || key.trim().equals("")) {
                    requestUrl = SERVER + "api/datasets/" + dataset.getId() + "/tags";
                } else {
                    requestUrl = SERVER + "api/datasets/" + dataset.getId() + "/tags?key=" + key.trim();
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
                    fileId = fileId.replace(SERVER + "api/files/", "");
                    requestUrl = SERVER + "api/datasets/" + dataset.getId() + "/files/" + fileId;

                    if ((key != null) || !key.trim().equals("")) {
                        requestUrl += "?key=" + key.trim();
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
        builder.setRedirectStrategy(new MediciRedirectStrategy());
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpclient = builder.build();
        try {
            String requestUrl;
            if ((key == null) || key.trim().equals("")) {
                requestUrl = SERVER + "api/datasets";
            } else {
                requestUrl = SERVER + "api/datasets?key=" + key.trim();
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
                    String title = jsonObject.get("datasetname").getAsString();
                    String description = jsonObject.get("description").getAsString();
                    String dateString = jsonObject.get("created").getAsString();
                    Date date = dateFormat.parse(dateString);
                    dataset.setId(id);
                    dataset.setDate(date);
                    dataset.setTitle(title);
                    dataset.setDescription(description);
                    dataset.setFileDescriptors(getFileDescriptor(id));

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
        // Medici doesn't support single fetch: SERVER + "api/datasets/id"
        // TODO Improve efficiency of finding 1 dataset
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
        builder.setRedirectStrategy(new MediciRedirectStrategy());
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpclient = builder.build();

        String requestUrl = SERVER + "api/datasets/" + id + "/listFiles";
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
                String dataUrl = SERVER + "api/files/" + fdId;
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

    // TODO CMN implement delete
    public void delete(Dataset entity) {
        System.out.println("delete " + entity.getClass().getName() + " from medici not yet implemented");
    }

    @Override
    public List<Dataset> findByDeleted(boolean deleted) {
        // TODO implement this query
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
        // TODO implement this query
        return findAll();
    }

    @Override
    public List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        // TODO implement this query
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

}
