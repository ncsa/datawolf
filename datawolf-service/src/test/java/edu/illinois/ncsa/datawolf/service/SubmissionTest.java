package edu.illinois.ncsa.datawolf.service;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.domain.Execution.State;
import edu.illinois.ncsa.datawolf.domain.Submission;
import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.datawolf.service.client.DataWolfServiceClient;
import edu.illinois.ncsa.domain.Persistence;
import edu.illinois.ncsa.domain.Person;

public class SubmissionTest {
    private static Logger logger = LoggerFactory.getLogger(SubmissionTest.class);

    private static String id     = null;
    private static String stepId = null;

    @BeforeClass
    public static void setUp() throws Exception {
        logger.info("Setting up Test for Executions Resource");

        // new GenericXmlApplicationContext("testContext.xml");

        // create person
        Person person = Person.createPerson("Jong", "Lee", "jonglee1@illinois.edu");

        DataWolfServiceClient.SERVER = "http://localhost:" + EmbededJetty.PORT;

        EmbededJetty.jettyServer("src/test/resources", "testContext.xml");

        Persistence.setInjector(EmbededJetty.injector);

        // create a workflow with a step
        Workflow workflow = TestCaseUtil.createWorkflow(person);
        WorkflowDao workflowDao = EmbededJetty.injector.getInstance(WorkflowDao.class);
        workflowDao.save(workflow);

        id = workflow.getId();
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

        String executionId = DataWolfServiceClient.submit(submission);

        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            Map<String, State> state = DataWolfServiceClient.getState(executionId);
            for (String stepId : state.keySet()) {
                logger.info("Status of the step " + stepId + ": " + state.get(stepId));
            }
        }

//        assertEquals(workflowJson, wfJson);
    }
}
