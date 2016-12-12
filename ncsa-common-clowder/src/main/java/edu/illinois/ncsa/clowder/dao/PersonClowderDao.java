package edu.illinois.ncsa.clowder.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class PersonClowderDao extends AbstractClowderDao<Person, String> implements PersonDao {
    private static final Logger logger = LoggerFactory.getLogger(PersonClowderDao.class);

    public Person findOne(String id) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpClient = builder.build();

        String requestUrl = getServer();
        String key = getKey();
        if (key == null || key.trim().equals("")) {
            requestUrl += "api/users/" + id;
        } else {
            requestUrl += "api/users/" + id + "?key=" + key.trim();
        }

        HttpGet httpGet = new HttpGet(requestUrl);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String responseStr = null;
        Person user = null;
        try {
            responseStr = httpClient.execute(httpGet, responseHandler);
            JsonElement jsonElement = new JsonParser().parse(responseStr);

            JsonObject person = jsonElement.getAsJsonObject();
            user = parseResponse(person);
        } catch (IOException e) {
            logger.error("Failed to get users", e);
        } finally {
            try {
                ((CloseableHttpClient) httpClient).close();
            } catch (IOException ignore) {
                logger.error("Error closing http client", ignore);
            }
        }

        return user;
    }

    @Override
    public Person findByEmail(String email) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpClient = builder.build();

        String requestUrl = getServer();
        String key = getKey();
        if (key == null || key.trim().equals("")) {
            requestUrl += "api/users/email" + email;
        } else {
            requestUrl += "api/users/email/" + email + "?key=" + key.trim();
        }

        HttpGet httpGet = new HttpGet(requestUrl);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String responseStr = null;
        Person user = null;
        try {
            responseStr = httpClient.execute(httpGet, responseHandler);
            JsonElement jsonElement = new JsonParser().parse(responseStr);

            JsonObject person = jsonElement.getAsJsonObject();
            user = parseResponse(person);
        } catch (IOException e) {
            logger.error("Failed to get users", e);
        } finally {
            try {
                ((CloseableHttpClient) httpClient).close();
            } catch (IOException ignore) {
                logger.error("Error closing http client", ignore);
            }
        }

        return user;
    }

    @Override
    public List<Person> findByDeleted(boolean deleted) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpClient = builder.build();

        String requestUrl = getServer();
        String key = getKey();
        if (key == null || key.trim().equals("")) {
            requestUrl += "api/users";
        } else {
            requestUrl += "api/users?key=" + key.trim();
        }

        HttpGet httpGet = new HttpGet(requestUrl);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String responseStr = null;

        List<Person> results = null;
        try {
            responseStr = httpClient.execute(httpGet, responseHandler);
            JsonElement jsonElement = new JsonParser().parse(responseStr);
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            results = new ArrayList<Person>();

            // Parse users
            for (int index = 0; index < jsonArray.size(); index++) {
                JsonObject person = jsonArray.get(index).getAsJsonObject();

                Person user = parseResponse(person);
                results.add(user);
            }
        } catch (IOException e) {
            logger.error("Failed to get users", e);
        } finally {
            try {
                ((CloseableHttpClient) httpClient).close();
            } catch (IOException ignore) {
                logger.error("Error closing http client", ignore);
            }
        }

        return results;
    }

    // Parses a return user from the Clowder API
    private Person parseResponse(JsonObject person) {
        String id = person.get("id").getAsString();
        String firstName = person.get("firstName").getAsString();
        String lastName = person.get("lastName").getAsString();
        String email = null;
        // Handle case with no email
        if (!(person.get("email") instanceof JsonNull)) {
            email = person.get("email").getAsString();
        }

        Person user = new Person();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        return user;
    }

}
