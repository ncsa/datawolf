package edu.illinois.ncsa.incore;

import com.google.gson.JsonObject;

import edu.illinois.ncsa.domain.Person;

public class IncoreUser {
    public static String USERS_ENDPOINT = "auth/api/users";

    public static Person getUser(JsonObject userInfo) {

        String id = userInfo.get("login").getAsString();
        String firstName = userInfo.get("firstName").getAsString();
        String lastName = userInfo.get("lastName").getAsString();
        String email = userInfo.get("email").getAsString();

        Person person = new Person();
        person.setId(id);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmail(email);

        return person;
    }
}
