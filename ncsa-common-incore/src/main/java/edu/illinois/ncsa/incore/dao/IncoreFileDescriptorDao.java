package edu.illinois.ncsa.incore.dao;

import java.util.List;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

public class IncoreFileDescriptorDao extends AbstractIncoreDao<FileDescriptor, String> implements FileDescriptorDao {

    @Override
    public FileDescriptor save(FileDescriptor entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(FileDescriptor entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public FileDescriptor findOne(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FileDescriptor> findAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean exists(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long count() {
        // TODO Auto-generated method stub
        return 0;
    }

}
