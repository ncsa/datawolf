package edu.illinois.ncsa.cyberintegrator.service;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
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

        CyberintegratorServiceClient.SERVER = "http://localhost:8088";

        RestServer.jettyServer("src/test/resources", "testContext.xml");
    }

    @Test
    public void testStartExecutionById() throws Exception {
        logger.info("Test get workflow by id");
        CyberintegratorServiceClient.startExecutionById(id);
        Transaction transaction = SpringData.getTransaction();
        transaction.start();
        ExecutionDAO executionDAO = SpringData.getBean(ExecutionDAO.class);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            Execution execution = executionDAO.findOne(id);
            logger.info("Status of the job:" + execution.getStepState(stepId));
        }
        transaction.commit();
//        assertEquals(workflowJson, wfJson);
    }

}
