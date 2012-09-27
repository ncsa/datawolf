package edu.illinois.ncsa.cyberintegrator.service;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.service.client.CyberintegratorServiceClient;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class ExecutionsResourceGetTest {
    private static Logger logger = LoggerFactory.getLogger(ExecutionsResourceGetTest.class);

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
        Execution execution = TestCaseUtil.createExecution(person, workflow);
        List<WorkflowStep> steps = workflow.getSteps();
        stepId = steps.get(0).getId();
        SpringData.getBean(ExecutionDAO.class).save(execution);
        id = execution.getId();

        CyberintegratorServiceClient.SERVER = "http://localhost:" + EmbededJetty.PORT;

        EmbededJetty.jettyServer("src/test/resources", "testContext.xml");
    }

    @AfterClass
    public static void tearDown() {
        EmbededJetty.stop();
    }

    @Test
    public void testStartExecutionById() throws Exception {
        logger.info("Test get workflow by id");
        CyberintegratorServiceClient.startExecution(id);
        ExecutionDAO executionDAO = SpringData.getBean(ExecutionDAO.class);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            Transaction transaction = SpringData.getTransaction();
            try {
                transaction.start();
                Execution execution = executionDAO.findOne(id);
                logger.info("Status of the job:" + execution.getStepState(stepId));
                if (execution.getStepState(stepId) == Execution.State.FINISHED) {
                    return;
                }
            } finally {
                transaction.commit();
            }
        }
        throw new AssertionError("workflow never finished.");
//        assertEquals(workflowJson, wfJson);
    }

}
