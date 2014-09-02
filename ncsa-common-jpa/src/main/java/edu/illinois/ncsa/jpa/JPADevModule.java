package edu.illinois.ncsa.jpa;

import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.domain.impl.FileStorageDisk;
import edu.illinois.ncsa.jpa.dao.FileDescriptorJPADao;
import edu.illinois.ncsa.jpa.dao.PersonJPADao;

// TODO this should probably be in a different project that manages DAO binding
public class JPADevModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("NCSACommonPersistence"));

        // try {
        // Properties props = loadProperties();
        // Names.bindProperties(binder(), props);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        bind(PersonDao.class).to(PersonJPADao.class);
        bind(FileDescriptorDao.class).to(FileDescriptorJPADao.class);
        bind(FileStorage.class).to(FileStorageDisk.class);
    }

    // private Properties loadProperties() throws Exception {
    // Properties properties = new Properties();
    // File file = new File("src/test/resources/config.properties");
    // properties.load(new FileInputStream(file));

    // return properties;
    // }

}
