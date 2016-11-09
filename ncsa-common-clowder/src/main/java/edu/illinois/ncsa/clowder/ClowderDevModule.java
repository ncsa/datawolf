package edu.illinois.ncsa.clowder;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import edu.illinois.ncsa.clowder.dao.DatasetClowderDao;
import edu.illinois.ncsa.clowder.dao.FileDescriptorClowderDao;
import edu.illinois.ncsa.clowder.dao.PersonClowderDao;
import edu.illinois.ncsa.clowder.impl.FileStorageClowder;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.dao.PersonDao;

public class ClowderDevModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind PersonDAO to use Clowder DAO
        bind(PersonDao.class).to(PersonClowderDao.class);
        bind(DatasetDao.class).to(DatasetClowderDao.class);
        bind(FileDescriptorDao.class).to(FileDescriptorClowderDao.class);

        bindConstant().annotatedWith(Names.named("medici.key")).to("");
        bindConstant().annotatedWith(Names.named("medici.server")).to("http://localhost:9000/");
        bind(FileStorage.class).to(FileStorageClowder.class);
    }

}
