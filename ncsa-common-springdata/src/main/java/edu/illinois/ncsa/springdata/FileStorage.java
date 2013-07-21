/**
 * 
 */
package edu.illinois.ncsa.springdata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import edu.illinois.ncsa.domain.FileDescriptor;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface FileStorage {
    public FileDescriptor storeFile(InputStream is) throws IOException;

    public FileDescriptor storeFile(String filename, InputStream is) throws IOException;

    public URL storeFile(FileDescriptor fd, InputStream is) throws IOException;

    public FileDescriptor storeFile(String id, String filename, InputStream is) throws IOException;

    public InputStream readFile(FileDescriptor fd) throws IOException;

    public boolean deleteFile(FileDescriptor fd);
}
