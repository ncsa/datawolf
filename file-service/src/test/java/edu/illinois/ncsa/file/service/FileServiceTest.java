/**
 * 
 */
package edu.illinois.ncsa.file.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class FileServiceTest {

    @BeforeClass
    public static void setUp() throws Exception {
        RestServer.jettyServer("src/test/resources", "testContext.xml");
    }

    @Test
    public void testPost() throws Exception {
        String x = "Some text";

        FileDescriptor fd = new FileDescriptor();
        fd.setFilename("text.txt");
        fd.setId("11223344556677");
        fd.setMd5sum(new BigInteger(MessageDigest.getInstance("MD5").digest(x.getBytes())));
        fd.setSize(x.length());

        FileStorage fs = SpringData.getFileStorage();
        fs.storeFile(fd, new ByteArrayInputStream(x.getBytes()));

        assertTrue(fd.getDataURL().toExternalForm().endsWith("/11/22/33/11223344556677/text.txt"));

        BufferedReader br = new BufferedReader(new InputStreamReader(fs.readFile(fd)));
        String line = br.readLine();
        assertEquals(x, line);

        line = br.readLine();
        assertEquals(null, line);

        br.close();
    }
}
