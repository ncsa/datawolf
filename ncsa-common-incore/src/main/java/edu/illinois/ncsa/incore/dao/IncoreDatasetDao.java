package edu.illinois.ncsa.incore.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
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
        // This should create a dataset in in-core

        return null;
    }

    @Override
    public void delete(Dataset entity) {}

    @Override
    public Dataset findOne(String id) {

        String incoreEndpoint = getServer();
        String requestUrl = incoreEndpoint;
        // Hack to work around no direct /datasets/dataset-id endpoint
        if (typeIds == null) {
            typeIds = getTypeIds();
        }

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();
        Dataset dataset = null;

        try {
            HttpResponse response = null;
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseStr = null;
            for (String typeId : typeIds) {
                requestUrl = incoreEndpoint;
                requestUrl += IncoreDataset.DATASETS_ENDPOINT + "/" + typeId + "/" + id;
                HttpGet httpGet = new HttpGet(requestUrl);

                response = httpclient.execute(httpGet);

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    responseStr = responseHandler.handleResponse(response);
                    JsonElement jsonElement = new JsonParser().parse(responseStr);
                    JsonObject datasetProperties = jsonElement.getAsJsonObject();
                    JsonObject maevizMapping = datasetProperties.get(IncoreDataset.MAEVIZ_MAPPING).getAsJsonObject();
                    if (datasetProperties != null && !maevizMapping.get(IncoreDataset.SCHEMA).isJsonNull()) {
                        dataset = IncoreDataset.getDataset(datasetProperties, getCreator());
                        break;
                    }
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    // since you won't use the response content, just close
                    // the stream
                    br.close();
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

    private List<String> getTypeIds() {
        String incoreEndpoint = getServer();
        String requestUrl = incoreEndpoint + IncoreDataset.DATASETS_ENDPOINT + "/" + IncoreDataset.LIST;

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();
        List<String> typeIds = new LinkedList<String>();
        try {
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseStr = httpclient.execute(httpGet, responseHandler);

            Pattern patternTag = Pattern.compile(IncoreDataset.HTML_A_TAG_PATTERN);
            Pattern patternLink = Pattern.compile(IncoreDataset.HTML_A_HREF_TAG_PATTERN);
            Matcher matcherTag = patternTag.matcher(responseStr);

            while (matcherTag.find()) {
                String href = matcherTag.group(1);
                // String linkText = matcherTag.group(2);
                Matcher matcherLink = patternLink.matcher(href);
                while (matcherLink.find()) {
                    String link = matcherLink.group(1);
                    link = link.replace("\"", "");
                    typeIds.add(link);
                }
            }
        } catch (ClientProtocolException e) {
            logger.error("Error getting list of type ids.", e);
        } catch (IOException e) {
            logger.error("Error getting list of type ids.", e);
        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }

        return typeIds;
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
        // TODO Auto-generated method stub
        return null;
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
