/**
 * 
 */
package edu.illinois.ncsa.file.service;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.file.service.client.FileServiceClient;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class FileServicePostTest {

    @BeforeClass
    public static void setUp() throws Exception {
        EmbededJetty.jettyServer("src/test/resources", "testContext.xml");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        EmbededJetty.stop();
    }

    @Test
    public void testPost() throws Exception {
        FileServiceClient.SERVER = "http://localhost:" + EmbededJetty.PORT;

        File postfile = new File("src/test/resources/test.txt");
        InputStream o = new FileInputStream(postfile);
        byte[] oba = IOUtil.toByteArray(o);
        FileDescriptor fd = FileServiceClient.createFileDescriptor(postfile);

        FileStorage fs = SpringData.getFileStorage();
        InputStream r = fs.readFile(fd);
        byte[] ba = IOUtil.toByteArray(r);

        assertArrayEquals(oba, ba);
    }

}
