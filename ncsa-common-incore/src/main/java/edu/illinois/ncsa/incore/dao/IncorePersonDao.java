package edu.illinois.ncsa.incore.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.incore.IncoreUser;

public class IncorePersonDao extends AbstractIncoreDao<Person, String> implements PersonDao {
    private static final Logger logger = LoggerFactory.getLogger(IncorePersonDao.class);

    @Override
    public Person save(Person entity) {
        return null;
    }

    @Override
    public void delete(Person entity) {}

    @Override
    public Person findOne(String id) {
        String incoreEndpoint = getServer();
        String requestUrl = incoreEndpoint;

        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient httpclient = builder.build();
        requestUrl += IncoreUser.USERS_ENDPOINT + "/" + id;
        try {
            HttpGet httpGet = new HttpGet(requestUrl);

            if (id != null) {
                httpGet.setHeader("X-Credential-Username", id);
            }

            BasicResponseHandler responseHandler = new BasicResponseHandler();
            String responseStr = httpclient.execute(httpGet, responseHandler);
            JsonElement jsonElement = new JsonParser().parse(responseStr);

            JsonObject personObj = jsonElement.getAsJsonObject();

            return IncoreUser.getUser(personObj);
        } catch (ClientProtocolException e) {
            logger.debug("Error fetching person.", e);
        } catch (IOException e) {
            logger.debug("Error fetching person.", e);
        } catch (Exception e) {
            logger.debug("Error fetching person.", e);
        }

        return null;

    }

    @Override
    public List<Person> findAll() {
        return new ArrayList<Person>();
    }

    @Override
    public boolean exists(String id) {
        return false;
    }

    @Override
    public long count(boolean deleted) {
        return 0;
    }

    @Override
    public Person findByEmail(String email) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Person> findByDeleted(boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Person> findAll(int page, int size) {
        // TODO Auto-generated method stub
        return new ArrayList<Person>();
    }

}
