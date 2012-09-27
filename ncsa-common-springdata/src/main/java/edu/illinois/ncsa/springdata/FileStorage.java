/**
 * 
 */
package edu.illinois.ncsa.springdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.FileDescriptor;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
// TODO RK :  Make this an interface and code an implementation
public class FileStorage {
    private static Logger logger = LoggerFactory.getLogger(FileStorage.class);

    private int           levels;
    private String        folder;

    public FileStorage() {
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
        // TODO RK : add mimetype
        fd.setDataURL(output.toURI().toURL().toString());
        if (md5 != null) {
            fd.setMd5sum(md5.digest());
        }
        fd.setSize(size);

        SpringData.getBean(FileDescriptorDAO.class).save(fd);

        return fd;
    }

    public boolean deleteFile(FileDescriptor fd) {
        String dataURL = fd.getDataURL();
        File f = new File(dataURL.split(":")[1]);
        return f.delete();
    }

    public InputStream readFile(FileDescriptor fd) throws IOException {
        // TODO RK : add some caching
        return new URL(fd.getDataURL()).openStream();
    }
}
