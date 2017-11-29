package edu.illinois.ncsa.domain;

public interface TokenProvider {
    public String getToken(String username, String password);

    public boolean isTokenValid(String token);
}
