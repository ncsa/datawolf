package edu.illinois.ncsa.datawolf.service.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class LoginUtil {
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
}
