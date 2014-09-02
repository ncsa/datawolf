package edu.illinois.ncsa.jpa.dao;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class PersonJPADao extends AbstractJPADao<Person, String> implements PersonDao {

    @Inject
    PersonJPADao(EntityManager entityManager) {
        super(entityManager);
    }

}
