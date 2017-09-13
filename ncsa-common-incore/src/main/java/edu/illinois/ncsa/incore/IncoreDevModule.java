package edu.illinois.ncsa.incore;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.incore.dao.IncoreDatasetDao;
import edu.illinois.ncsa.incore.dao.IncoreFileDescriptorDao;
import edu.illinois.ncsa.incore.impl.FileStorageIncore;

public class IncoreDevModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DatasetDao.class).to(IncoreDatasetDao.class);
        bind(FileDescriptorDao.class).to(IncoreFileDescriptorDao.class);

        bindConstant().annotatedWith(Names.named("incore.server")).to("http://localhost:9000/");
        bind(FileStorage.class).to(FileStorageIncore.class);
    }
}
