package edu.illinois.ncsa.clowder.impl;

import java.io.IOException;

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
import edu.illinois.ncsa.domain.TokenProvider;

public class ClowderTokenProvider implements TokenProvider {
    private Logger log = LoggerFactory.getLogger(ClowderTokenProvider.class);

    @Inject
    @Named("clowder.server")
    private String server;

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

            // Create a key named datawolf if no keys exist
            if (jsonArray.size() == 0) {
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
                // Use existing API key
                JsonObject obj = jsonArray.get(0).getAsJsonObject();
                return obj.get("key").getAsString();
            }

        } catch (Exception e) {
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
