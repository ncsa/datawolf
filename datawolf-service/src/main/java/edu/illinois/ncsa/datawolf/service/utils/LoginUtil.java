package edu.illinois.ncsa.datawolf.service.utils;

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.dao.AccountDao;

public class LoginUtil {
    private static final String PREFERRED_USERNAME = "preferred_username";
    private static final Logger log                = LoggerFactory.getLogger(LoginUtil.class);

    /**
     * 
     * @param credential
     *            - user credential to parse
     * @return List, first item is the user, second is the token/password
     */
    public static List<String> parseCredentials(String credential) {
        List<String> credentials = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer(credential, ":");
        if (tokenizer.countTokens() == 2) {
            credentials.add(tokenizer.nextToken());
            credentials.add(tokenizer.nextToken());
        }

        return credentials;
    }

    public static String getUserInfo(AccountDao accountDao, HttpHeaders httpHeaders) {
        // Check for authorization header or cookie
        String credential = null;
        List<String> headers = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (headers != null && !headers.isEmpty()) {
            if (!httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION).isEmpty()) {
                credential = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0);
                log.debug("Parsing Authorization header");
            }
        }

        if (httpHeaders.getCookies().containsKey("token")) {
            log.debug("Parsing cookie for user info");
            Cookie cookie = httpHeaders.getCookies().get("token");
            credential = cookie.getValue();
        }

        Account account = null;
        if (credential != null) {
            if (credential.startsWith("Basic")) {
                List<String> credentials = LoginUtil.parseCredentials(new String(Base64.getDecoder().decode(credential.substring(6))));
                account = accountDao.findByUserid(credentials.get(0));
            } else {
                account = accountDao.findByToken(credential);
            }

            if (account != null) {
                return account.getPerson().getEmail();
            }
        }

        // Check for presence of x-userinfo header
        headers = httpHeaders.getRequestHeader("x-userinfo");
        if (headers != null && !headers.isEmpty()) {
            log.debug("Parsing X-Userinfo");
            String userInfoString = headers.get(0);
            String userInfoJson = new String(Base64.getDecoder().decode(userInfoString));

            JsonParser parser = new JsonParser();
            JsonElement userInfoElement = parser.parse(userInfoJson);
            JsonObject userInfo = userInfoElement.getAsJsonObject();

            return userInfo.get(PREFERRED_USERNAME).getAsString();
        }

        // Check for presence of x-auth-userinfo header"
        headers = httpHeaders.getRequestHeader("x-auth-userinfo");
        if (headers != null && !headers.isEmpty()) {
            log.debug("Parsing x-auth-userinfo");
            String userInfoJson = headers.get(0);

            JsonParser parser = new JsonParser();
            JsonElement userInfoElement = parser.parse(userInfoJson);
            JsonObject userInfo = userInfoElement.getAsJsonObject();

            return userInfo.get(PREFERRED_USERNAME).getAsString();
        }

        log.warn("Didn't find user information");

        return null;
    }
}
