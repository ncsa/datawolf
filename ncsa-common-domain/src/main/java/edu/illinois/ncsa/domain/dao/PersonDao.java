package edu.illinois.ncsa.domain.dao;

import java.util.List;

import edu.illinois.ncsa.domain.Person;

public interface PersonDao extends IDao<Person, String> {
    Person findByEmail(String email);

    List<Person> findByDeleted(boolean deleted);
}
