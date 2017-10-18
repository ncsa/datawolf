package edu.illinois.ncsa.incore.dao;

import java.io.IOException;
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
import com.google.gson.JsonPrimitive;

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

        // Check if this is a new entity (e.g. still has UUID)
        if (entity.getId().contains("-")) {

            // TODO replace this
            // Description should contain type information
            // This should be used to contact the semantic services
            String description = entity.getDescription();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("schema", "buildingDamagev4");
            jsonObject.addProperty("type", "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingDamageVer4.v1.0");
            jsonObject.addProperty("title", entity.getTitle());
            jsonObject.addProperty("sourceDataset", "");
            jsonObject.addProperty("format", "csv");

            JsonArray spaces = new JsonArray();
            spaces.add(new JsonPrimitive("cnavarro"));
            spaces.add(new JsonPrimitive("ergo"));
            jsonObject.add("spaces", spaces);

            String incoreEndpoint = getServer();
            String requestUrl = incoreEndpoint;
            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpClient httpclient = builder.build();

            try {
                HttpResponse response = null;
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseStr = null;
                requestUrl += IncoreDataset.DATASETS_ENDPOINT + "/" + IncoreDataset.CREATE_DATASET;

                HttpPost httpPost = new HttpPost(requestUrl);
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

            response = httpclient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                responseStr = responseHandler.handleResponse(response);
                JsonElement jsonElement = new JsonParser().parse(responseStr);
                JsonObject datasetProperties = jsonElement.getAsJsonObject();
                if (datasetProperties != null) {
                    dataset = IncoreDataset.getDataset(datasetProperties, getCreator());
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
            requestUrl += IncoreDataset.DATASETS_ENDPOINT + "/" + IncoreDataset.LIST;
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
                        dataset = IncoreDataset.getDataset(datasetProperties, getCreator());
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

    private Person getCreator() {
        // TODO Fix this later
        Person creator = personDao.findByEmail("incore-dev@lists.illinois.edu");
        if (creator == null) {
            creator = new Person();
            creator.setEmail("incore-dev@lists.illinois.edu");
            creator.setFirstName("IN-CORE");
            creator.setLastName("Developer");
            personDao.save(creator);
        }

        return creator;
    }

    @Override
    public boolean exists(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long count() {
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

}
