package edu.illinois.ncsa.incore.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.incore.IncoreDataset;
import junit.framework.Assert;

public class IncoreDatasetDaoTest {

    private String incoreEndpoint = "http://localhost:8080/";

    @BeforeClass
    public static void setUp() throws Exception {

    }

    @AfterClass
    public static void tearDown() {

    }

    // @Test
    public void testFindOne() throws Exception {

        List<String> typeIds = getTypeIds();
        String datasetId = "Shelby_County_RES31224702005658";

        String incoreEndpoint = "http://localhost:8080/";

        String requestUrl = incoreEndpoint + IncoreDataset.DATASETS_ENDPOINT + "/" + IncoreDataset.LIST;
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();
        HttpResponse response = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseStr = null;
        Dataset dataset = null;
        try {
            for (String typeId : typeIds) {
                requestUrl = incoreEndpoint;
                try {
                    requestUrl += IncoreDataset.DATASETS_ENDPOINT + "/" + typeId + "/" + datasetId;
                    HttpGet httpGet = new HttpGet(requestUrl);

                    response = httpclient.execute(httpGet);

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        responseStr = responseHandler.handleResponse(response);
                        JsonElement jsonElement = new JsonParser().parse(responseStr);
                        JsonObject datasetProperties = jsonElement.getAsJsonObject();
                        JsonObject maevizMapping = datasetProperties.get(IncoreDataset.MAEVIZ_MAPPING).getAsJsonObject();

                        // JsonObject datasetProperties =
                        // IncoreDataset.getDatasetResponse(jsonObject);
                        if (datasetProperties != null && !maevizMapping.get(IncoreDataset.SCHEMA).isJsonNull()) {
                            dataset = IncoreDataset.getDataset(datasetProperties, null);
                            break;
                        }
                    } else {
                        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        // since you won't use the response content, just close
                        // the stream
                        br.close();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }

        Assert.assertNotNull(dataset);
    }

    private List<String> getTypeIds() throws Exception {

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
        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }

        return typeIds;
    }

    @Test
    public void testReadIncoreDatasetJson() throws Exception {
//        String requestUrl = incoreEndpoint + "repo/api/datasets";
//        HttpClientBuilder builder = HttpClientBuilder.create();
//        HttpClient httpclient = builder.build();
//        HttpGet httpGet = new HttpGet(requestUrl);
//
//        ResponseHandler<String> responseHandler = new BasicResponseHandler();
//        String responseStr = httpclient.execute(httpGet, responseHandler);
//        PrintWriter pw = new PrintWriter(new File("/home/cnavarro/incore-repo.json"));
//
//        pw.println(responseStr);
//        pw.close();
        List<Dataset> datasets = new ArrayList<Dataset>();
        File file = new File("src/test/resources/incore-repo.json");
        BufferedReader br = new BufferedReader(new FileReader(file));

        StringBuffer buffer = new StringBuffer();
        String line = null;
        while ((line = br.readLine()) != null) {
            buffer.append(line);
        }

        String json = buffer.toString();

        JsonElement jsonElement = new JsonParser().parse(json);
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        System.out.println(jsonArray.size());

        Person creator = null;

        for (int index = 0; index < jsonArray.size(); index++) {
            JsonObject datasetProperties = jsonArray.get(index).getAsJsonObject();

            if (datasetProperties != null) {
                Dataset dataset = IncoreDataset.getDataset(datasetProperties, creator);
                datasets.add(dataset);
            }
        }
        Assert.assertEquals(208, datasets.size());
    }
}
