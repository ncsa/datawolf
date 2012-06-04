package edu.illinois.ncsa.cyberintegrator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.SpringData;

public class WorkflowsResourceGetTest {
    private static Logger   logger   = LoggerFactory.getLogger(WorkflowsResourceGetTest.class);

    private static String   id       = null;
    private static Workflow workflow = null;

    @BeforeClass
    public static void setUp() throws Exception {
        logger.debug("Setting up Test for Workflow Resource");

        new GenericXmlApplicationContext("testContext.xml");

        // create person
        Person person = Person.createPerson("Jong", "Lee", "jonglee1@illinois.edu");

        // create a workflow with a step
        workflow = createWorkflow(person, 2);
        SpringData.getBean(WorkflowDAO.class).save(workflow);
        id = workflow.getId();

        CyberintegratorServiceClient.SERVER = "http://localhost:8088";

        RestServer.jettyServer("src/test/resources", "testContext.xml");
    }

    @Test
    public void testGetIdForFileDescriptor() throws Exception {
        Workflow wf = CyberintegratorServiceClient.getWorkflowById(id);

    }

    public static Workflow createWorkflow(Person creator, int steps) {
        Workflow workflow = new Workflow();
        workflow.setCreator(creator);
        workflow.setTitle("TEST WORKFLOW");

        List<String> ids = new ArrayList<String>();
        ids.add("input");
        for (int i = 0; i < steps; i++) {
            WorkflowStep step = createWorkflowStep(creator);
            step.setInput(step.getTool().getInputs().get(0), ids.get(new Random().nextInt(ids.size())));
            workflow.addStep(step);
            ids.add(step.getOutputs().keySet().iterator().next());
        }

        return workflow;
    }

    public static WorkflowStep createWorkflowStep(Person creator) {
        WorkflowStep step = new WorkflowStep();
        step.setCreator(creator);
        step.setTool(createWorkflowTool(creator));

        return step;
    }

    public static WorkflowTool createWorkflowTool(Person creator) {
        WorkflowTool tool = new WorkflowTool();
        tool.setCreator(creator);

        WorkflowToolData input = new WorkflowToolData();
        input.setDataId("inp-1");
        input.setMimeType("text/plain");
        input.setTitle("input");
        tool.addInput(input);

        WorkflowToolData output = new WorkflowToolData();
        output.setDataId("out-1");
        output.setMimeType("text/plain");
        output.setTitle("output");
        tool.addOutput(output);

        tool.setExecutor("dummy");

        return tool;
    }

}
