package edu.illinois.ncsa.jpa.dao;

import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        JpaPersistModule jpa = new JpaPersistModule("NCSACommonPersistence");
        install(jpa);

        bind(PersonDao.class).to(PersonJPADao.class);
        bind(FileDescriptorDao.class).to(FileDescriptorJPADao.class);
        bind(AccountDao.class).to(AccountJPADao.class);
        bind(DatasetDao.class).to(DatasetJPADao.class);

    }

}
