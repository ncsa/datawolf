/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.PersonDAO;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 * @TODO add test to test loadQueue - create engine, add workflow, stop engine
 *       and start new engine
 * 
 */
public class BugCBI399Test {
    @Before
    public void setup() {
        new GenericXmlApplicationContext("cbi399.xml");
    }

    @Test
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
        for (Person pb : SpringData.getBean(PersonDAO.class).findAll()) {
            mapper.writeValue(jsonGenerator, pb);

        }

        // make sure people are reused
        assertEquals(1, SpringData.getBean(PersonDAO.class).count());
        assertEquals(2, SpringData.getBean(WorkflowDAO.class).count());
        assertEquals(4, SpringData.getBean(WorkflowStepDAO.class).count());
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
