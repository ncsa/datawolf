package edu.illinois.ncsa.clowder.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

import edu.illinois.ncsa.clowder.ClowderRedirectStrategy;
import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.TokenProvider;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class ClowderTokenProvider implements TokenProvider {
    private Logger       log    = LoggerFactory.getLogger(ClowderTokenProvider.class);

    @Inject
    private AccountDao   accountDao;

    @Inject
    private PersonDao    personDao;

    @Inject
    @Named("clowder.server")
    private String       server;

    @Inject
    @Named("initialAdmins")
    private String       initialAdmins;

    private List<String> admins = null;

    @Override
    public String getToken(String username, String password) {
        HttpClient httpclient = null;
        try {
            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setRedirectStrategy(new ClowderRedirectStrategy());
            RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
            builder.setDefaultRequestConfig(config);
            httpclient = builder.build();

            String requestUrl = getServer();
            requestUrl += "api/users/keys";

            HttpGet httpGet = new HttpGet(requestUrl);

            String auth = "Basic " + new String(Base64.encodeBase64(new String(username + ":" + password).getBytes()));
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, auth);

            BasicResponseHandler responseHandler = new BasicResponseHandler();
            String responseStr = httpclient.execute(httpGet, responseHandler);

            JsonElement jsonElement = new JsonParser().parse(responseStr);
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            // Users is authorized, check if DataWolf Account exists
            Account account = accountDao.findByUserid(username);
            if (account != null) {
                if (account.getToken() != null && !account.getToken().isEmpty()) {
                    // Make sure the token still matches what's in DataWolf or generate a new key
                    if (jsonArray.size() != 0) {
                        for (int index = 0; index < jsonArray.size(); index++) {
                            JsonObject obj = jsonArray.get(index).getAsJsonObject();
                            if (obj.get("key").getAsString().equals(account.getToken())) {
                                return account.getToken();
                            }
                        }

                    }
                    // Account exists, but no matching keys
                    log.debug("Account token doesn't match Clowder keys, create a new one.");
                    requestUrl = getServer();
                    requestUrl += "api/users/keys?name=datawolf";

                    HttpPost httpPost = new HttpPost(requestUrl);
                    httpPost.setHeader(HttpHeaders.AUTHORIZATION, auth);

                    responseHandler = new BasicResponseHandler();
                    responseStr = httpclient.execute(httpPost, responseHandler);
                    jsonElement = new JsonParser().parse(responseStr);
                    JsonObject obj = jsonElement.getAsJsonObject();

                    return obj.get("key").getAsString();
                } else {
                    // Create a key named datawolf if no keys exist
                    if (jsonArray.size() == 0) {
                        log.debug("Account doesn't have a token, create a new one in Clowder.");
                        requestUrl = getServer();
                        requestUrl += "api/users/keys?name=datawolf";

                        HttpPost httpPost = new HttpPost(requestUrl);
                        httpPost.setHeader(HttpHeaders.AUTHORIZATION, auth);

                        responseHandler = new BasicResponseHandler();
                        responseStr = httpclient.execute(httpPost, responseHandler);
                        jsonElement = new JsonParser().parse(responseStr);
                        JsonObject obj = jsonElement.getAsJsonObject();
                        log.debug("No API keys, create one for DataWolf");
                        return obj.get("key").getAsString();
                    } else {
                        // Use existing API key - should we always just generate a datawolf key?
                        JsonObject obj = jsonArray.get(0).getAsJsonObject();
                        return obj.get("key").getAsString();
                    }
                }
            } else {
                // Need to create an account
                // Initialize the list of admins the first time this is called
                log.debug("No Account exists in DataWolf, creating a new one from Clowder information.");
                if (admins == null) {
                    // Remove double quotes around string
                    initialAdmins = initialAdmins.replaceAll("^\"|\"$", "").trim();
                    if (initialAdmins.isEmpty()) {
                        admins = new ArrayList<String>();
                    } else {
                        admins = Arrays.asList(initialAdmins.split(","));
                    }
                }
                Person person = personDao.findByEmail(username);
                account = new Account();
                account.setPerson(person);
                account.setUserid(username);
                if (admins.contains(username)) {
                    account.setActive(true);
                    account.setAdmin(true);
                }
                accountDao.save(account);
            }

        } catch (

        Exception e) {
            log.error("Error getting a token from Clowder.", e);
        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {}
        }

        return null;
    }

    @Override
    public boolean isTokenValid(String token) {
        HttpClient httpclient = null;
        try {
            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setRedirectStrategy(new ClowderRedirectStrategy());
            RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
            builder.setDefaultRequestConfig(config);
            httpclient = builder.build();

            String requestUrl = getServer();
            requestUrl += "api/me?key=" + token;

            HttpGet httpGet = new HttpGet(requestUrl);

            HttpResponse response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            } else {
                log.debug("HTTP Response " + response.getStatusLine().getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error validating token", e);
        } finally {
            try {
                ((CloseableHttpClient) httpclient).close();
            } catch (IOException ignore) {}
        }

        return false;
    }

    private String getServer() {
        if (server.endsWith("/")) {
            return server;
        } else {
            return server + "/";
        }
    }

}
