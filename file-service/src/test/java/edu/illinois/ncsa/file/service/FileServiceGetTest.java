/**
 * 
 */
package edu.illinois.ncsa.file.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.file.service.client.FileServiceClient;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class FileServiceGetTest {
    private static Logger         logger = LoggerFactory.getLogger(FileServiceGetTest.class);

    private static FileDescriptor fd     = null;
    private static String         id     = null;
    private static byte[]         ba     = null;

    @BeforeClass
    public static void setUp() throws Exception {
        FileServiceClient.SERVER = "http://localhost:8088";
        EmbededJetty.jettyServer("src/test/resources", "testContext.xml");
        FileStorage fs = SpringData.getFileStorage();

        // store file via FileStorage

        File file = new File("src/test/resources/test.txt");
        String filename = "test.txt";
        InputStream o = new FileInputStream(file);

        fd = fs.storeFile(filename, o);

        o = new FileInputStream(file);
        ba = IOUtil.toByteArray(o);

        id = fd.getId();
        logger.debug(id);
    }

    @Test
    public void testGetIdForFileDescriptor() throws Exception {

        FileDescriptor tfd = FileServiceClient.getFileDescriptor(id);
        assertEquals(fd, tfd);
    }

    @Test
    public void testGetFile() throws Exception {
        InputStream is = FileServiceClient.getFile(id);
        byte[] newBa = IOUtil.toByteArray(is);
        assertArrayEquals(ba, newBa);
    }
}
