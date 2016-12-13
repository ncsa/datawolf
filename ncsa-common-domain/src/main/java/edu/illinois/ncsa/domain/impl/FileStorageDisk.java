/**
 * 
 */
package edu.illinois.ncsa.domain.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

/**
 * Stores files on disk. This class has 2 parameters, level and folder. The
 * default is to store the files in the temp folder and have 2 levels. The
 * levels are to prevent to many files in a single folder.
 * 
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class FileStorageDisk implements FileStorage {
    private static Logger     logger = LoggerFactory.getLogger(FileStorageDisk.class);

    @Inject
    @Named("disk.levels")
    private int               levels;
    @Inject
    @Named("disk.folder")
    private String            folder;

    @Inject
    private FileDescriptorDao fileDescriptorDAO;

    public FileStorageDisk() {
        setLevels(2);
        setFolder(System.getProperty("java.io.tmpdir"));
    }

    /**
     * @return the levels
     */
    public int getLevels() {
        return levels;
    }

    /**
     * @param levels
     *            the levels to set
     */
    public void setLevels(int levels) {
        this.levels = levels;
    }

    /**
     * @return the folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * @param folder
     *            the folder to set
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    public FileDescriptor storeFile(InputStream is) throws IOException {
        return storeFile(new FileDescriptor().getId(), null, is);
    }

    public FileDescriptor storeFile(String filename, InputStream is) throws IOException {
        return storeFile(new FileDescriptor().getId(), filename, is);
    }

    public URL storeFile(FileDescriptor fd, InputStream is) throws IOException {
        FileDescriptor check = storeFile(fd.getId(), fd.getFilename(), is);

        // simple checks
        if ((fd.getMd5sum() != null) && !fd.getMd5sum().equals(check.getMd5sum())) {
            throw (new IOException("MD5SUM does not match, error saving data."));
        }
        if ((fd.getSize() > 0) && (fd.getSize() != check.getSize())) {
            throw (new IOException("File size does not match, error saving data."));
        }

        // set the url to the new url
        fd.setDataURL(check.getDataURL());

        return new URL(check.getDataURL());
    }

    public FileDescriptor storeFile(String id, String filename, InputStream is) throws IOException {
        FileDescriptor fd = new FileDescriptor();
        fd.setId(id);
        fd.setFilename(filename);

        String path = "";

        // create the folders
        for (int i = 0; i < levels * 2 && id.length() >= i + 2; i += 2) {
            path = path + id.substring(i, i + 2) + File.separator;
        }
        if (id.length() > 0) {
            path = path + id + File.separator;
        }

        // append the filename
        if (filename != null) {
            path = path + filename;
        } else {
            path = path + "unknown.unk";
        }

        // create the output file
        File output = new File(folder, path);
        if (!output.getParentFile().isDirectory()) {
            if (!output.getParentFile().mkdirs()) {
                throw (new IOException("Could not create folder to store data in."));
            }
        }

        // Write the file to disk, storing the md5sum
        FileOutputStream fos = null;
        MessageDigest md5 = null;
        int size = 0;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not create MD5 digest.", e);
        }
        try {
            fos = new FileOutputStream(output);
            byte[] buf = new byte[10240];
            int len = 0;
            while ((len = is.read(buf)) >= 0) {
                fos.write(buf, 0, len);
                if (md5 != null) {
                    md5.update(buf, 0, len);
                }
                size += len;
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
            is.close();
        }

        // create the fd fields

        fd.setMimeType(new MimetypesFileTypeMap().getContentType(output));

        fd.setDataURL(output.toURI().toURL().toString());
        if (md5 != null) {
            fd.setMd5sum(md5.digest());
        }
        fd.setSize(size);

        fileDescriptorDAO.save(fd);

        return fd;
    }

    public boolean deleteFile(FileDescriptor fd) {
        String dataURL = fd.getDataURL();
        File f = new File(dataURL.split(":")[1]);

        if (f.exists()) {
            if (f.delete()) {
                logger.debug("file deleted, attempting to cleanup empty folders");
                // Delete parent folders if they are empty
                deleteParentFolders(f.getParentFile());
                return true;
            } else {
                logger.error("Can't delete a file id(" + fd.getId() + ") :" + fd.getDataURL());
                return false; // file still exist
            }
        } else {
            logger.error("The file does not exist: id(" + fd.getId() + ") :" + fd.getDataURL());
            return true;
        }
    }

    private void deleteParentFolders(File file) {
        File parent = file.getParentFile();
        if (file.list().length == 0) {
            // Delete empty folder
            file.delete();
            logger.debug("deleted: " + file.getAbsolutePath());

            String dataDirectory = folder;
            // Remove end separator before comparing strings
            if (dataDirectory.endsWith(File.separator)) {
                dataDirectory = dataDirectory.substring(0, dataDirectory.length() - 1);
            }

            // Delete until we reach the data directory or non-empty folder
            if (!parent.getAbsolutePath().endsWith(dataDirectory)) {
                deleteParentFolders(parent);
            }
        }
    }

    public InputStream readFile(FileDescriptor fd) throws IOException {
        // TODO RK : add some caching
        return new URL(fd.getDataURL()).openStream();
    }
}
