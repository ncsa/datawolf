package edu.illinois.ncsa.jpa.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class PersonJPADao extends AbstractJPADao<Person, String> implements PersonDao {

    @Inject
    PersonJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    public Person findByEmail(String email) {

        EntityManager em = getEntityManager();
        List<Person> list = null;
        try {
            em.getTransaction().begin();
            String queryString = "SELECT p FROM Person p " + "WHERE p.email = :email";

            TypedQuery<Person> typedQuery = em.createQuery(queryString, Person.class);
            typedQuery.setParameter("email", email);
            list = typedQuery.getResultList();

            if (list.isEmpty()) {
                return null;
            }
        } finally {
            em.getTransaction().commit();
        }

        return list.get(0);
    }

    @Override
    public List<Person> findByDeleted(boolean deleted) {
        List<Person> results = null;
        try {
            getEntityManager().getTransaction().begin();
            String queryString = "SELECT p FROM Person p " + "WHERE p.deleted = :deleted";
            TypedQuery<Person> q = getEntityManager().createQuery(queryString, Person.class);
            q.setParameter("deleted", deleted);
            results = q.getResultList();

            return results;
        } finally {
            getEntityManager().getTransaction().commit();
        }
    }

}
