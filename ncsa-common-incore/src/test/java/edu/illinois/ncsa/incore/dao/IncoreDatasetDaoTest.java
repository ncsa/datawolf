package edu.illinois.ncsa.incore.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.incore.IncoreDataset;

public class IncoreDatasetDaoTest {

    private String incoreEndpoint = "http://localhost:8080/";

    @BeforeClass
    public static void setUp() throws Exception {

    }

    @AfterClass
    public static void tearDown() {

    }

    // @Test
    public void testGetAllDatasets() throws Exception {
        String requestUrl = incoreEndpoint + IncoreDataset.DATASETS_ENDPOINT + "/" + IncoreDataset.LIST;
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();
        HttpGet httpGet = new HttpGet(requestUrl);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = httpclient.execute(httpGet, responseHandler);

        List<Dataset> datasets = new ArrayList<Dataset>();

        JsonElement jsonElement = new JsonParser().parse(responseStr);
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        System.out.println(jsonArray.size());

        Person creator = null;

        for (int index = 0; index < jsonArray.size(); index++) {
            JsonObject datasetProperties = jsonArray.get(index).getAsJsonObject();
            System.out.println(datasetProperties.toString());

            if (datasetProperties != null) {
                Dataset dataset = IncoreDataset.getDataset(datasetProperties, creator);
                datasets.add(dataset);
            }
        }
    }
}
