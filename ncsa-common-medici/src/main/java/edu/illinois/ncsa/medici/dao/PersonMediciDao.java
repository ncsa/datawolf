package edu.illinois.ncsa.medici.dao;

import java.util.List;

import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class PersonMediciDao extends AbstractMediciDao<Person, String> implements PersonDao {

    @Override
    public Person findByEmail(String email) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Person> findByDeleted(boolean deleted) {
        // TODO Auto-generated method stub
        return null;
    }

}
