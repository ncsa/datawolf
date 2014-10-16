package edu.illinois.ncsa.file.service;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
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
        install(new JpaPersistModule("NCSACommonPersistence")); //$NON-NLS-1$

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("disk.folder", System.getProperty("java.io.tmpdir")); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("disk.levels", "2"); //$NON-NLS-1$ //$NON-NLS-2$

        Names.bindProperties(binder(), properties);

        bind(FileStorage.class).to(FileStorageDisk.class);
        bind(FileDescriptorDao.class).to(FileDescriptorJPADao.class);
    }
}
