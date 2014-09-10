package edu.illinois.ncsa.jpa.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;

import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class PersonJPADao extends AbstractJPADao<Person, String> implements PersonDao {

    @Inject
    PersonJPADao(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Person findByEmail(String email) {
        String queryString = "SELECT a FROM Person a " + "WHERE a.email = :email";

        TypedQuery<Person> q = getEntityManager().createQuery(queryString, Person.class);
        q.setParameter("email", email);
        List<Person> list = q.getResultList();
        return list.get(0);
    }

    @Override
    public List<Person> findByDeleted(boolean deleted) {
        String queryString = "SELECT a FROM Person a " + "WHERE a.deleted = :deleted";
        TypedQuery<Person> q = getEntityManager().createQuery(queryString, Person.class);
        q.setParameter("deleted", deleted);
        return q.getResultList();
    }

}
