package edu.illinois.ncsa.incore.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.AccountDao;

public class IncoreFileStorage implements FileStorage {
    @Inject
    @Named("incore.server")
    private String     server;

    @Inject(optional = true)
    private AccountDao accountDao;

    @Override
    public FileDescriptor storeFile(InputStream is) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileDescriptor storeFile(String filename, InputStream is) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileDescriptor storeFile(String filename, InputStream is, Person creator) throws IOException {
        return null;
    }

    @Override
    public URL storeFile(FileDescriptor fd, InputStream is) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileDescriptor storeFile(String id, String filename, InputStream is) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileDescriptor storeFile(String id, String filename, InputStream is, Person creator) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream readFile(FileDescriptor fd) throws IOException {
        String requestUrl = fd.getDataURL();
        return new URL(requestUrl).openStream();
    }

    @Override
    public boolean deleteFile(FileDescriptor fd) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteFile(FileDescriptor fd, Person creator) {
        // TODO Auto-generated method stub
        return false;
    }

}
