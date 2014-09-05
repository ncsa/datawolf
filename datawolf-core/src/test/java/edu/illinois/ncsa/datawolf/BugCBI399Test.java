/**
 * 
 */
package edu.illinois.ncsa.datawolf;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.BeforeClass;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.PersonDao;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 * @TODO add test to test loadQueue - create engine, add workflow, stop engine
 *       and start new engine
 * 
 */
public class BugCBI399Test {
    private static Injector injector;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new TestModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();
    }

    // @Test
    public void importWorkflow() throws Exception {
        // create a workflow with a step
        Person person1 = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");
        Workflow workflow1 = EngineTest.createWorkflow(person1, 2, true);
        File file1 = saveWorkflow(workflow1);
        ImportExport.importWorkflow(file1);

        Person person2 = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");
        Workflow workflow2 = EngineTest.createWorkflow(person2, 2, true);
        File file2 = saveWorkflow(workflow2);
        ImportExport.importWorkflow(file2);

        ObjectMapper mapper = new ObjectMapper();
        final JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(System.out);
        jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
        PersonDao personDao = injector.getInstance(PersonDao.class);
        for (Person pb : personDao.findAll()) {
            mapper.writeValue(jsonGenerator, pb);

        }
        WorkflowDao workflowDao = injector.getInstance(WorkflowDao.class);
        WorkflowStepDao workflowStepDao = injector.getInstance(WorkflowStepDao.class);
        // make sure people are reused
        assertEquals(1, personDao.count());
        assertEquals(2, workflowDao.count());
        assertEquals(4, workflowStepDao.count());
    }

    private File saveWorkflow(Workflow workflow) throws IOException {
        // create zipfile
        File file = File.createTempFile("cbi3999", ".zip");
        file.deleteOnExit();
        ZipOutputStream zipfile = new ZipOutputStream(new FileOutputStream(file));

        // export workflow
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // this will close the outputstream, hence the byte array
        ObjectMapper mapper = new ObjectMapper();
        final JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(baos);
        jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
        mapper.writeValue(jsonGenerator, workflow);

        zipfile.putNextEntry(new ZipEntry("workflow.json"));
        zipfile.write(baos.toByteArray());
        zipfile.closeEntry();

        zipfile.close();
        return file;
    }
}
