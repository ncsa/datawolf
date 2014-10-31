package edu.illinois.ncsa.irods.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.activation.MimetypesFileTypeMap;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;

/**
 * Storage implementation for the iRODS file storage system. There are 7
 * required parameters to establish a connection to iRODS including a
 * host, port, username, password, user home, zone, and default storage resource
 * name. Optional parameters include the root folder to store the data in and
 * the number of levels to use when storing files to prevent to many files per
 * folder. The folder should be an absolute path since this could differ from
 * the user home folder.
 * 
 * See @FileStorageDisk
 * 
 * 
 * @author Chris Navarro <cmnavarr@illinois.edu>
 * 
 */

public class FileStorageIRODS implements FileStorage {
    private static final Logger logger         = LoggerFactory.getLogger(FileStorageIRODS.class);

    @Inject
    @Named("disk.levels")
    private int                 levels;

    @Inject
    @Named("disk.folder")
    private String              folder;

    @Inject
    @Named("irods.userhome")
    private String              userhome;

    @Inject
    @Named("irods.username")
    private String              username;

    @Inject
    @Named("irods.password")
    private String              password;

    @Inject
    @Named("irods.host")
    private String              host;

    @Inject
    @Named("irods.port")
    private int                 port;

    @Inject
    @Named("irods.zone")
    private String              zone;

    @Inject
    @Named("irods.storageresource")
    private String              defaultStorageResource;

    private IRODSAccount        account;
    private boolean             initConnection = false;
    private IRODSFileSystem     irodsFileSystem;

    public FileStorageIRODS() {
        setLevels(2);
        folder = "";
    }

    private void openIRODSConnection() throws IOException {
        try {
            irodsFileSystem = IRODSFileSystem.instance();
            account = IRODSAccount.instance(host, port, username, password, userhome, zone, defaultStorageResource);
            initConnection = true;
        } catch (JargonException e) {
            logger.error("Error connecting to iRODS server.", e);
            throw new IOException("Error connection to iRODS server.", e);
        }
    }

    /**
     * Number of levels to create on file storage system
     * 
     * @return the number of levels
     */
    public int getLevels() {
        return levels;
    }

    /**
     * Sets the number of levels to use on the file storage system.
     * 
     * @param levels
     *            the number of levels
     */
    public void setLevels(int levels) {
        this.levels = levels;
    }

    /**
     * Returns the location of the file storage
     * 
     * @return the location on disk of the file storage
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets the location to use for file storage.
     * 
     * @param folder
     *            the location on disk to store files
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * Sets the iRODS username
     * 
     * @param username
     *            the user account to store files under
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the iRODS password for the user account
     * 
     * @param password
     *            password for the user account
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the home directory for the iRODS account
     * 
     * @return home directory for the iRODS account
     */
    public String getUserhome() {
        return userhome;
    }

    /**
     * Sets the home directory for the iRODS user account
     * 
     * @param userhome
     *            directory for the iRODS user account
     */
    public void setUserhome(String userhome) {
        this.userhome = userhome;
    }

    /**
     * Returns the hostname for the iRODS file storage
     * 
     * @return the hostname
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the hostname for the iRODS file storage
     * 
     * @param host
     *            the hostname
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the port number for the iRODS file storage
     * 
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number for the iRODS file storage
     * 
     * @param port
     *            the port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the zone for the iRODS file storage
     * 
     * @return the zone for the iRODS
     */
    public String getZone() {
        return zone;
    }

    /**
     * Sets the zone for the iRODS file storage
     * 
     * @param zone
     *            the zone for the iRODS installation
     */
    public void setZone(String zone) {
        this.zone = zone;
    }

    /**
     * Returns the default storage resource name
     * 
     * @return the default storage resource name
     */
    public String getDefaultStorageResource() {
        return defaultStorageResource;
    }

    /**
     * Sets the default storage resource name
     * 
     * @param defaultStorageResource
     *            the default storage resource name
     */
    public void setDefaultStorageResource(String defaultStorageResource) {
        this.defaultStorageResource = defaultStorageResource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.springdata.FileStorage#storeFile(java.io.InputStream)
     */
    public FileDescriptor storeFile(InputStream is) throws IOException {
        return storeFile(new FileDescriptor().getId(), null, is);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.illinois.ncsa.springdata.FileStorage#storeFile(java.lang.String,
     * java.io.InputStream)
     */
    public FileDescriptor storeFile(String filename, InputStream is) throws IOException {
        return storeFile(new FileDescriptor().getId(), filename, is);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.springdata.FileStorage#storeFile(edu.illinois.ncsa.
     * domain.FileDescriptor, java.io.InputStream)
     */
    public URL storeFile(FileDescriptor fd, InputStream is) throws IOException {
        FileDescriptor check = storeFile(fd.getId(), fd.getFilename(), is);
        // simple checks
        if ((fd.getMd5sum() != null) && !fd.getMd5sum().equals(check.getMd5sum())) {
            throw (new IOException("MD5SUM does not match, error saving data."));
        }
        if ((fd.getSize() > 0) && (fd.getSize() != check.getSize())) {
            throw (new IOException("File size does not match, error saving data."));
        }

        fd.setDataURL(check.getDataURL());

        return new URL(check.getDataURL());
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.illinois.ncsa.springdata.FileStorage#storeFile(java.lang.String,
     * java.lang.String, java.io.InputStream)
     */
    public FileDescriptor storeFile(String id, String filename, InputStream is) throws IOException {
        FileDescriptor fd = new FileDescriptor();
        fd.setId(id);
        fd.setFilename(filename);

        String path = "";

        // create the folders
        for (int i = 0; i < levels * 2 && id.length() >= i + 2; i += 2) {
            path = path + id.substring(i, i + 2) + IRODSFile.PATH_SEPARATOR;
        }
        if (id.length() > 0) {
            path = path + id + IRODSFile.PATH_SEPARATOR;
        }

        // append the filename
        if (filename != null) {
            path = path + filename;
        } else {
            path = path + "unknown.unk";
        }

        if (!initConnection) {
            openIRODSConnection();
        }

        IRODSFileOutputStream ifos = null;
        MessageDigest md5 = null;
        int size = 0;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not create MD5 digest.", e);
        }

        IRODSFile destFile = null;
        try {
            IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(account);
            destFile = irodsFileFactory.instanceIRODSFile(folder, path);
            if (!destFile.getParentFile().isDirectory()) {
                if (!destFile.getParentFile().mkdirs()) {
                    throw (new IOException("Could not create folder to store data in"));
                }
            }

            ifos = irodsFileFactory.instanceIRODSFileOutputStream(destFile);
            byte[] buf = new byte[10240];
            int len = 0;
            while ((len = is.read(buf)) >= 0) {
                ifos.write(buf, 0, len);
                if (md5 != null) {
                    md5.update(buf, 0, len);
                }
                size += len;
            }

        } catch (JargonException e) {
            logger.error("Error saving dataset to iRODS storage.", e);
            throw new IOException("Error saving dataset to iRODS storage", e);
        } finally {
            if (ifos != null) {
                ifos.close();
            }

            is.close();
        }

        fd.setMimeType(new MimetypesFileTypeMap().getContentType(filename));
        fd.setDataURL(destFile.toURI().toString());
        fd.setSize(size);

        if (md5 != null) {
            fd.setMd5sum(md5.digest());
        }

        return fd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.springdata.FileStorage#readFile(edu.illinois.ncsa.domain
     * .FileDescriptor)
     */
    public InputStream readFile(FileDescriptor fd) throws IOException {
        if (!initConnection) {
            openIRODSConnection();
        }

        try {
            IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(account);
            IRODSFile file = irodsFileFactory.instanceIRODSFile(new URI(fd.getDataURL()));
            return irodsFileFactory.instanceIRODSFileInputStream(file);
        } catch (JargonException e) {
            logger.error("Error getting an IRODS stream for: id(" + fd.getId() + ") : " + fd.getDataURL(), e);
            throw new IOException("Error getting an IRODS stream for: id(" + fd.getId() + ") : " + fd.getDataURL(), e);
        } catch (URISyntaxException e) {
            logger.error("File data URI is not correct: id(" + fd.getId() + ") : " + fd.getDataURL());
            throw new IOException("File data URI is not correct: id(" + fd.getId() + ") : " + fd.getDataURL(), e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.springdata.FileStorage#deleteFile(edu.illinois.ncsa
     * .domain.FileDescriptor)
     */
    public boolean deleteFile(FileDescriptor fd) {
        try {
            if (!initConnection) {
                openIRODSConnection();
            }

            String dataURL = fd.getDataURL();
            IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(account);
            IRODSFile file = irodsFileFactory.instanceIRODSFile(new URI(dataURL));
            if (file.exists()) {
                if (file.delete()) {
                    logger.debug("file is deleted:" + dataURL);
                    return true;
                } else {
                    logger.error("Error deleting a file id(" + fd.getId() + ") : " + fd.getDataURL());
                    return false;
                }
            } else {
                logger.error("The file does not exist: id(" + fd.getId() + ") : " + fd.getDataURL());
                return true;
            }
        } catch (IOException e) {
            // Do nothing, any error here will be logged in openIRODSConnection
        } catch (JargonException e) {
            logger.error("Error accessing iRODS storage.", e);
        } catch (URISyntaxException e) {
            logger.error("File data URI is not correct: id(" + fd.getId() + ") : " + fd.getDataURL());
        }
        return false;
    }
}
