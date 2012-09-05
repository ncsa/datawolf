package edu.illinois.ncsa.cyberintegrator.service;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.SpringData;

public class WorkflowsResourceGetTest {
    private static Logger   logger       = LoggerFactory.getLogger(WorkflowsResourceGetTest.class);

    private static String   id           = null;
    private static Workflow workflow     = null;
    private static String   workflowJson = null;

    @BeforeClass
    public static void setUp() throws Exception {
        logger.info("Setting up Test for Workflow Resource");

        new GenericXmlApplicationContext("testContext.xml");

        // create person
        Person person = Person.createPerson("Jong", "Lee", "jonglee1@illinois.edu");

        // create a workflow with a step
        workflow = TestCaseUtil.createWorkflow(person);
        SpringData.getBean(WorkflowDAO.class).save(workflow);
        id = workflow.getId();

        StringWriter sw = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(sw, workflow);
        workflowJson = sw.toString();

        CyberintegratorServiceClient.SERVER = "http://localhost:" + EmbededJetty.PORT;

        EmbededJetty.jettyServer("src/test/resources", "testContext.xml");
    }

    @AfterClass
    public static void tearDown() {
        EmbededJetty.stop();
    }

    @Test
    public void testGetWorkflowById() throws Exception {
        logger.info("Test get workflow by id");
        String wfJson = CyberintegratorServiceClient.getWorkflowJSONById(id);
        assertEquals(workflowJson, wfJson);
    }

}
