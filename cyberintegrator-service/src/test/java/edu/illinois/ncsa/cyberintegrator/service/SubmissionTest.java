package edu.illinois.ncsa.cyberintegrator.service;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.Submission;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.service.client.CyberintegratorServiceClient;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.SpringData;

public class SubmissionTest {
    private static Logger logger = LoggerFactory.getLogger(SubmissionTest.class);

    private static String id     = null;
    private static String stepId = null;

    @BeforeClass
    public static void setUp() throws Exception {
        logger.info("Setting up Test for Executions Resource");

        new GenericXmlApplicationContext("testContext.xml");

        // create person
        Person person = Person.createPerson("Jong", "Lee", "jonglee1@illinois.edu");

        // create a workflow with a step
        Workflow workflow = TestCaseUtil.createWorkflow(person);
        SpringData.getBean(WorkflowDAO.class).save(workflow);

        id = workflow.getId();

        CyberintegratorServiceClient.SERVER = "http://localhost:" + EmbededJetty.PORT;

        EmbededJetty.jettyServer("src/test/resources", "testContext.xml");
    }

    @AfterClass
    public static void tearDown() {
        EmbededJetty.stop();
    }

    @Test
    public void testSubmission() throws Exception {
        logger.info("Test get workflow by id");
        Submission submission = new Submission();
        // create person
        Person person = Person.createPerson("Jong", "Lee", "jonglee1@illinois.edu");
        submission.setCreatorId(person.getId());
        submission.setWorkflowId(id);

        String executionId = CyberintegratorServiceClient.submit(submission);

        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            Map<String, State> state = CyberintegratorServiceClient.getState(executionId);
            for (String stepId : state.keySet()) {
                logger.info("Status of the step " + stepId + ": " + state.get(stepId));
            }
        }

//        assertEquals(workflowJson, wfJson);
    }
}
