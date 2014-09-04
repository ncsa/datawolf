package edu.illinois.ncsa.file.service;

import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.impl.FileStorageDisk;
import edu.illinois.ncsa.jpa.dao.FileDescriptorJPADao;

/**
 * Binds Persistence DAOs
 * 
 * @author Chris Navarro <cmnavarr@illinois.edu>
 * 
 */
public class PersistenceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new JpaPersistModule("NCSACommonPersistence"));

        bind(FileStorage.class).to(FileStorageDisk.class);
        bind(FileDescriptorDao.class).to(FileDescriptorJPADao.class);
    }
}
