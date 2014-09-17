package edu.illinois.ncsa.medici;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.medici.dao.DatasetMediciDao;
import edu.illinois.ncsa.medici.dao.FileDescriptorMediciDao;
import edu.illinois.ncsa.medici.dao.PersonMediciDao;
import edu.illinois.ncsa.medici.impl.FileStorageMedici;

public class MediciDevModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind PersonDAO to use Medici DAO
        bind(PersonDao.class).to(PersonMediciDao.class);
        bind(DatasetDao.class).to(DatasetMediciDao.class);
        bind(FileDescriptorDao.class).to(FileDescriptorMediciDao.class);

        bindConstant().annotatedWith(Names.named("medici.key")).to("");
        bindConstant().annotatedWith(Names.named("medici.server")).to("http://localhost:9000/");
        bind(FileStorage.class).to(FileStorageMedici.class);
    }

}
