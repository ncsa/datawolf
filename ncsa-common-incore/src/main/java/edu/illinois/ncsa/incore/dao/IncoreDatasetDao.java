package edu.illinois.ncsa.incore.dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.incore.IncoreDataset;

public class IncoreDatasetDao extends AbstractIncoreDao<Dataset, String> implements DatasetDao {
    private static final Logger logger  = LoggerFactory.getLogger(IncoreDatasetDao.class);

    private static List<String> typeIds = null;

    @Inject
    PersonDao                   personDao;

    @Override
    public Dataset save(Dataset entity) {
        // Uncomment for local testing
//        String bearerToken = getToken();

        // Check if this is a new entity (e.g. still has UUID)
        if (entity.getId().contains("-")) {
            // The description should contain the type and format to store
            // TODO is there a better way?
            String description = entity.getDescription();
            String[] datasetInfo = description.split(",");
            String type = "Unknown";
            String format = "Unknown";
            if (datasetInfo.length == 2) {
                type = datasetInfo[0];
                format = datasetInfo[1];
                logger.debug("Saving data with dataType = " + type + ", format = " + format);
            } else {
                // Assume this is something like stdout which didn't define anything
                logger.debug("Saving plain text file with type = incore:stdout");
                type = "incore:stdout";
                format = "text";
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("dataType", type);
            jsonObject.addProperty("title", entity.getTitle());
            jsonObject.addProperty("sourceDataset", "");
            jsonObject.addProperty("format", format);

            String creatorId = null;
            String creatorEmail = null;
            if (entity.getCreator() != null) {
                creatorId = entity.getCreator().getId();
                creatorEmail = entity.getCreator().getEmail();
            }

            String incoreEndpoint = getServer();
            String requestUrl = incoreEndpoint;

            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpClient httpclient = builder.build();

            try {
                HttpResponse response = null;
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseStr = null;
                requestUrl += IncoreDataset.DATASETS_ENDPOINT;

                HttpPost httpPost = new HttpPost(requestUrl);

                if (creatorId != null) {
                    // This will only work for internal communication, for testing, we add the auth token
                    String creatorGroup = "[\""+ this.getGroup() + "\"]";
                    httpPost.setHeader(IncoreDataset.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creatorEmail + "\"}");
                    httpPost.setHeader(IncoreDataset.X_AUTH_USERGROUP, "{\"groups\": " + creatorGroup + "}" );
                    // Uncomment for local testing
//                    httpPost.setHeader("Authorization", bearerToken);
                }

                MultipartEntityBuilder params = MultipartEntityBuilder.create();
                params.addTextBody("dataset", jsonObject.toString());

                httpPost.setEntity(params.build());
                response = httpclient.execute(httpPost);
                responseStr = responseHandler.handleResponse(response);

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JsonElement jsonElement = new JsonParser().parse(responseStr);
                    JsonObject datasetProperties = jsonElement.getAsJsonObject();
                    if (datasetProperties != null) {
                        Dataset dataset = IncoreDataset.getDataset(datasetProperties, null);
                        return dataset;
                    }
                } else {
                    logger.error("Error saving the dataset. " + responseStr);
                }
            } catch (ClientProtocolException e) {
                logger.error("Error saving dataset", e);
            } catch (IOException e) {
                logger.error("Error saving dataset ", e);
            } finally {
                try {
                    ((CloseableHttpClient) httpclient).close();
                } catch (IOException e) {
                    logger.warn("Error closing http client", e);
                }
            }
        }

        return entity;
    }

    @Override
    public void delete(Dataset entity) {}

    @Override
    public Dataset findOne(String id) {
        // Uncomment for local testing
//        String bearerToken = getToken();

        String incoreEndpoint = getServer();
        String requestUrl = incoreEndpoint;

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();
        Dataset dataset = null;

        try {
            HttpResponse response = null;
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseStr = null;
            requestUrl += IncoreDataset.DATASETS_ENDPOINT + "/" + id;
            HttpGet httpGet = new HttpGet(requestUrl);

            // This is a workaround for data that is stored outside of DataWolf
            // DataWolf assumes a level of trust that only authenticated/authorized users could reach here
            // This works fine with local storage/data; however, when accessing an external source, the user
            // info, which we don't have, might be needed.
            // A potential solution could be to add the requester info to the method
            String user = this.getIncoreUser();
            String creatorGroup = "[\""+ this.getGroup() + "\"]";

            httpGet.setHeader(IncoreDataset.X_AUTH_USERINFO, "{\"preferred_username\": \"" + user + "\"}");
            httpGet.setHeader(IncoreDataset.X_AUTH_USERGROUP, "{\"groups\": " + creatorGroup + "}" );

            // Uncomment for local testing
//            httpGet.setHeader("Authorization", bearerToken);

            response = httpclient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                responseStr = responseHandler.handleResponse(response);
                JsonElement jsonElement = new JsonParser().parse(responseStr);
                JsonObject datasetProperties = jsonElement.getAsJsonObject();
                if (datasetProperties != null) {
                    Person creator = null;
                    if (datasetProperties.has(IncoreDataset.CREATOR)) {
                        String creatorId = datasetProperties.get(IncoreDataset.CREATOR).getAsString();
                        if (creatorId != null) {
                            creator = personDao.findOne(creatorId);
                        }
                    }
                    dataset = IncoreDataset.getDataset(datasetProperties, creator);
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            logger.error("Error find dataset with id " + id, e);
        } catch (IOException e) {
            logger.error("Error find dataset with id " + id, e);
        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {
                logger.warn("Error closing http client", ignore);
            }
        }

        return dataset;
    }

    @Override
    public List<Dataset> findAll() {
        List<Dataset> results = new ArrayList<Dataset>();

        String responseStr = null;
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String incoreEndpoint = getServer();
        String requestUrl = incoreEndpoint;
        try {
            requestUrl += IncoreDataset.DATASETS_ENDPOINT;
            HttpGet httpGet = new HttpGet(requestUrl);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                JsonElement jsonElement = new JsonParser().parse(responseStr);
                JsonArray jsonArray = jsonElement.getAsJsonArray();

                Dataset dataset = null;
                for (int index = 0; index < jsonArray.size(); index++) {

                    JsonObject datasetProperties = jsonArray.get(index).getAsJsonObject();

                    if (datasetProperties != null) {
                        Person creator = null;
                        if (datasetProperties.has(IncoreDataset.CREATOR)) {
                            String creatorId = datasetProperties.get(IncoreDataset.CREATOR).getAsString();
                            if (creatorId != null) {
                                creator = personDao.findOne(creatorId);
                            }
                        }
                        dataset = IncoreDataset.getDataset(datasetProperties, creator);
                        results.add(dataset);
                    }

                }
            } catch (Exception e) {
                logger.error("HTTP Get failed.", e);
            }

        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException e) {
                logger.warn("Error closing http client", e);
            }
        }
        return results;

    }

    @Override
    public List<Dataset> findAll(int page, int size) {
        List<Dataset> results = new ArrayList<Dataset>();

        String responseStr = null;
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String incoreEndpoint = getServer();
        String requestUrl = incoreEndpoint;

        // TODO if this method is needed, we will need to get this from the request
        String creatorEmail = "";
        try {
            requestUrl += IncoreDataset.DATASETS_ENDPOINT;
            HttpGet httpGet = new HttpGet(requestUrl);
            String creatorGroup = "[\""+ this.getGroup() + "\"]";

            httpGet.setHeader(IncoreDataset.X_AUTH_USERINFO, "{\"preferred_username\": \"" + creatorEmail + "\"}");
            httpGet.setHeader(IncoreDataset.X_AUTH_USERGROUP, "{\"groups\": " + creatorGroup + "}" );
            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                JsonElement jsonElement = new JsonParser().parse(responseStr);
                JsonArray jsonArray = jsonElement.getAsJsonArray();

                Dataset dataset = null;
                for (int index = 0; index < jsonArray.size(); index++) {

                    JsonObject datasetProperties = jsonArray.get(index).getAsJsonObject();

                    if (datasetProperties != null) {
                        Person creator = null;
                        if (datasetProperties.has(IncoreDataset.CREATOR)) {
                            String creatorId = datasetProperties.get(IncoreDataset.CREATOR).getAsString();
                            if (creatorId != null) {
                                creator = personDao.findOne(creatorId);
                            }
                        }
                        dataset = IncoreDataset.getDataset(datasetProperties, creator);
                        results.add(dataset);
                    }

                }
            } catch (Exception e) {
                logger.error("HTTP Get failed.", e);
            }

        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException e) {
                logger.warn("Error closing http client", e);
            }
        }
        return results;

    }

    @Override
    public boolean exists(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long count(boolean deleted) {
        // TODO Auto-generated method stub
        return 0;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        // TODO fix this
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
        // Uncomment for local testing
//        String bearerToken = getToken();

        List<Dataset> results = new ArrayList<Dataset>();

        String responseStr = null;
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();

        String incoreEndpoint = getServer();
        String requestUrl = incoreEndpoint;
        try {
            requestUrl += IncoreDataset.DATASETS_ENDPOINT + "?skip="+page + "&limit="+size;
            HttpGet httpGet = new HttpGet(requestUrl);

            String creatorGroup = "[\""+ this.getGroup() + "\"]";
            httpGet.setHeader(IncoreDataset.X_AUTH_USERINFO, "{\"preferred_username\": \"" + email + "\"}");
            httpGet.setHeader(IncoreDataset.X_AUTH_USERGROUP, "{\"groups\": " + creatorGroup + "}" );
            // Uncomment for local testing
//            httpGet.setHeader("Authorization", bearerToken);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                JsonElement jsonElement = new JsonParser().parse(responseStr);
                JsonArray jsonArray = jsonElement.getAsJsonArray();

                Dataset dataset = null;
                for (int index = 0; index < jsonArray.size(); index++) {

                    JsonObject datasetProperties = jsonArray.get(index).getAsJsonObject();

                    if (datasetProperties != null) {
                        Person creator = null;
                        if (datasetProperties.has(IncoreDataset.CREATOR)) {
                            String creatorId = datasetProperties.get(IncoreDataset.CREATOR).getAsString();
                            if (creatorId != null) {
                                creator = personDao.findByEmail(creatorId);
                            }
                        }
                        dataset = IncoreDataset.getDataset(datasetProperties, creator);
                        results.add(dataset);
                    }

                }
            } catch (Exception e) {
                logger.error("HTTP Get failed.", e);
            }

        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException e) {
                logger.warn("Error closing http client", e);
            }
        }
        return results;
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

    // Helper method for testing with a local instance
    public String getToken() {
        String bearerToken = null;
        try {
            String userHome = System.getProperty("user.home");
            byte[] encoded = Files.readAllBytes(Paths.get(userHome + "/.incore/.a0309ff368531845cbeef9a617fcf11361da600ec8cc1467b2a579cb52890f85_token"));
            bearerToken = new String(encoded, StandardCharsets.UTF_8);
        } catch(IOException e) {
            logger.error("Error getting token", e);
        }
        return bearerToken;
    }

}
