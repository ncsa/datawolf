package edu.illinois.ncsa.datawolf.service;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.datawolf.service.client.DataWolfServiceClient;
import edu.illinois.ncsa.domain.Persistence;
import edu.illinois.ncsa.domain.Person;

public class ExecutionsResourceGetTest {
    private static Logger logger = LoggerFactory.getLogger(ExecutionsResourceGetTest.class);

    private static String id     = null;
    private static String stepId = null;

    @BeforeClass
    public static void setUp() throws Exception {
        logger.info("Setting up Test for Executions Resource");

        DataWolfServiceClient.SERVER = "http://localhost:" + EmbededJetty.PORT;

        EmbededJetty.jettyServer("src/test/resources", "testContext.xml");

        Persistence.setInjector(EmbededJetty.injector);

    }

    @Before
    public void createContext() throws Exception {
        // new GenericXmlApplicationContext("testContext.xml");

        // create person
        Person person = Person.createPerson("Jong", "Lee", "jonglee1@illinois.edu");

        // Engine engine = EmbededJetty.injector.getInstance(Engine.class);
        // CommandLineExecutor executor =
// Persistence.getBean(CommandLineExecutor.class);

        // create a workflow with a step
        Workflow workflow = TestCaseUtil.createWorkflow(person);
        WorkflowDao workflowDao = EmbededJetty.injector.getInstance(WorkflowDao.class);
        workflowDao.save(workflow);

        Execution execution = TestCaseUtil.createExecution(person, workflow);
        List<WorkflowStep> steps = workflow.getSteps();
        stepId = steps.get(0).getId();

        ExecutionDao executionDao = EmbededJetty.injector.getInstance(ExecutionDao.class);
        executionDao.save(execution);

        id = execution.getId();
    }

    @AfterClass
    public static void tearDown() {
        EmbededJetty.stop();
    }

    @Test
    public void testStartExecutionById() throws Exception {
        logger.info("Test get workflow by id");

        // Execution execution = executionDao.findOne(id);

        DataWolfServiceClient.startExecution(id);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            // Transaction transaction = SpringData.getTransaction();
            // try {
            // transaction.start();
            // Execution execution = executionDao.findOne(id);
            Execution execution = DataWolfServiceClient.getExecution(id);
            logger.info("Status of the job:" + stepId + " state = " + execution.getStepState(stepId));
            if (execution.getStepState(stepId) == Execution.State.FINISHED) {
                return;
            }
            // } finally {
            // transaction.commit();
            // }
        }
        throw new AssertionError("workflow never finished.");
//        assertEquals(workflowJson, wfJson);
    }
}
