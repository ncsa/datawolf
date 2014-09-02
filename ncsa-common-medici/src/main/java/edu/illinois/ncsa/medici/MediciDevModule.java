package edu.illinois.ncsa.medici;

import com.google.inject.AbstractModule;

import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.medici.dao.PersonMediciDao;

public class MediciDevModule extends AbstractModule {

    @Override
    protected void configure() {

        // Bind PersonDAO to use Medici DAO
        bind(PersonDao.class).to(PersonMediciDao.class);
    }

}
