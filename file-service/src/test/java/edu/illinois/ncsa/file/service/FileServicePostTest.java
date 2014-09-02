/**
 * 
 */
package edu.illinois.ncsa.file.service;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.file.service.client.FileServiceClient;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class FileServicePostTest {

    @Inject
    private FileStorage fs;

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

        fs = EmbededJetty.injector.getInstance(FileStorage.class);

        InputStream r = fs.readFile(fd);
        byte[] ba = IOUtil.toByteArray(r);

        assertArrayEquals(oba, ba);
    }

}
