package edu.illinois.ncsa.medici.dao;

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class PersonMediciDao extends AbstractMediciDao<Person, String> implements PersonDao {
    private static final Logger logger = LoggerFactory.getLogger(PersonMediciDao.class);
    @Inject
    @Named("medici.server")
    private String              SERVER = "http://localhost:9000/";

    @Inject
    @Named("medici.key")
    private String              key;

    @Override
    public Person findByEmail(String email) {
        List<Person> users = findByDeleted(false);
        for (Person p : users) {
            if (p.getEmail().equals(email)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public List<Person> findByDeleted(boolean deleted) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        // builder.setRedirectStrategy(new MediciRedirectStrategy());
        RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        builder.setDefaultRequestConfig(config);
        HttpClient httpClient = builder.build();

        String requestUrl;
        if ((key == null) || key.trim().equals("")) {
            requestUrl = SERVER + "api/users";
        } else {
            requestUrl = SERVER + "api/users?key=" + key.trim();
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
                String id = person.get("id").getAsString();
                String firstName = person.get("firstName").getAsString();
                String lastName = person.get("lastName").getAsString();
                String email = person.get("email").getAsString();

                Person p = new Person();
                p.setId(id);
                p.setEmail(email);
                p.setFirstName(firstName);
                p.setLastName(lastName);
                results.add(p);
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

}
