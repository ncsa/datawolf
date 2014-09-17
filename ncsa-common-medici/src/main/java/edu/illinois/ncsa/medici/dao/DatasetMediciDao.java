package edu.illinois.ncsa.medici.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.dao.DatasetDao;

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
        HttpClient httpclient = new DefaultHttpClient();
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

                // TODO Datasets and Descriptors should be independent enough to
                // store the dataset in medici and the file descriptor elsewhere
                // we might need a linked Id to link medici id to object id
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
                HttpPost httpPost = new HttpPost(requestUrl);
                httpPost.setEntity(params);
                httpPost.setHeader("content-type", "application/json");
                responseHandler = new BasicResponseHandler();
                responseStr = httpclient.execute(httpPost, responseHandler);

                JsonElement jsonElement = new JsonParser().parse(responseStr);
                String id = jsonElement.getAsJsonObject().get("id").getAsString();
                dataset.setId(id);

            } catch (Exception e1) {
                logger.error("HTTP Create Dataset failed.", e1);
            }
        }
        return dataset;
    }

    public List<Dataset> findAll() {
        List<Dataset> results = null;
        String responseStr = null;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl;
            if ((key == null) || key.trim().equals("")) {
                requestUrl = SERVER + "api/datasets";
            } else {
                requestUrl = SERVER + "api/datasets?key=" + key.trim();
            }
            HttpGet httpGet = new HttpGet(requestUrl);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            logger.info("Executing request " + httpGet.getRequestLine());

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
                httpclient.getConnectionManager().shutdown();
            } catch (Exception ignore) {
                // Nothing to do with this exception
            }
        }
        return results;
    }

    public Dataset findOne(String id) {
        // TODO Medici 2 API api/datasets/id returns list of files, is there a
        // findOne equivalent?
        List<Dataset> results = findAll();
        for (Dataset dataset : results) {
            if (dataset.getId().equals(id)) {
                return dataset;
            }
        }

        return null;
    }

    private List<FileDescriptor> getFileDescriptor(String id) {

        HttpClient httpclient = new DefaultHttpClient();

        String requestUrl = SERVER + "api/datasets/" + id + "/listFiles";
        HttpGet httpGet = new HttpGet(requestUrl);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        logger.info("Executing request " + httpGet.getRequestLine());

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
        }

        return results;

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
