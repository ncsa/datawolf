package edu.illinois.ncsa.jpa.dao;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

public class FileDescriptorJPADao extends AbstractJPADao<FileDescriptor, String> implements FileDescriptorDao {

    @Inject
    FileDescriptorJPADao(EntityManager entityManager) {
        super(entityManager);
    }
}
