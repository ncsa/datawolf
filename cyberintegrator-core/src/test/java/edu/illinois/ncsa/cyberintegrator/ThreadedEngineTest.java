/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ThreadedEngineTest {
    private Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");

        Executor.addExecutor("dummy", DummyExecutor.class);
    }

    @Before
    public void createEngine() {
        engine = new ThreadedEngine();
        engine.startEngine();
    }

    @After
    public void stopEngine() {
        engine.stopEngine();
    }

    @Test
    public void testSubmitSingleStep() {
        // check to see if engine is running
        assertTrue(engine.isStarted());

        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");

        // create a workflow with a step
        Workflow workflow = createWorkflow(person, 2);
        SpringData.getBean(WorkflowDAO.class).save(workflow);

        // create the execution
        Execution execution = createExecution(person, workflow);
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // submit a single step
        engine.execute(execution, workflow.getSteps().get(0).getId());

        // make sure the step is there and not running
        assertEquals(1, engine.getQueue().size());
        assertEquals(0, engine.getRunning().size());
        assertEquals(1, engine.getWaiting().size());
    }

    @Test
    public void testSubmitAllSteps() {
        // check to see if engine is running
        assertTrue(engine.isStarted());

        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");

        // create a workflow with a step
        Workflow workflow = createWorkflow(person, 2);
        SpringData.getBean(WorkflowDAO.class).save(workflow);

        // create the execution
        Execution execution = createExecution(person, workflow);
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // submit a single step
        engine.execute(execution);

        // make sure the step is there and not running
        assertEquals(2, engine.getQueue().size());
        assertEquals(0, engine.getRunning().size());
        assertEquals(2, engine.getWaiting().size());
    }

    @Test
    public void testRunAllSteps() {
        // check to see if engine is running
        assertTrue(engine.isStarted());

        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");

        // create a workflow with a step
        Workflow workflow = createWorkflow(person, 2);
        SpringData.getBean(WorkflowDAO.class).save(workflow);

        // create the execution
        Execution execution = createExecution(person, workflow);
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // submit a single step
        engine.execute(execution);

        // make sure the step is there and not running
        assertEquals(2, engine.getQueue().size());
        assertEquals(0, engine.getRunning().size());
        assertEquals(2, engine.getWaiting().size());

        // add a listener

        // add a dataset
        Dataset dataset = createDataset(person);
        SpringData.getBean(DatasetDAO.class).save(dataset);
        execution.setDataset(workflow.getSteps().get(0).getInputs().values().iterator().next(), dataset);

        //
    }

    protected Dataset createDataset(Person creator) {
        Dataset dataset = new Dataset();
        dataset.setCreator(creator);
        dataset.setTitle("TEST DATASET");

        return dataset;
    }

    protected Execution createExecution(Person creator, Workflow workflow) {
        Execution execution = new Execution();
        execution.setCreator(creator);
        execution.setWorkflow(workflow);

        return execution;
    }

    protected Workflow createWorkflow(Person creator, int steps) {
        Workflow workflow = new Workflow();
        workflow.setCreator(creator);
        workflow.setTitle("TEST WORKFLOW");

        for (int i = 0; i < steps; i++) {
            workflow.addStep(createWorkflowStep(creator));
        }

        return workflow;
    }

    protected WorkflowStep createWorkflowStep(Person creator) {
        WorkflowStep step = new WorkflowStep();
        step.setCreator(creator);
        step.setTool(createWorkflowTool(creator));

        return step;
    }

    protected WorkflowTool createWorkflowTool(Person creator) {
        WorkflowTool tool = new WorkflowTool();
        tool.setCreator(creator);

        WorkflowToolData input = new WorkflowToolData();
        input.setDataId("in-1");
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

    static class DummyExecutor extends Executor {
        /*
         * (non-Javadoc)
         * 
         * @see edu.illinois.ncsa.cyberintegrator.Executor#kill()
         */
        @Override
        public void kill() throws Exception {}

        /*
         * (non-Javadoc)
         * 
         * @see edu.illinois.ncsa.cyberintegrator.Executor#execute(java.io.File,
         * edu.illinois.ncsa.cyberintegrator.domain.Execution,
         * edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep)
         */
        @Override
        public void execute(File cwd, Execution execution, WorkflowStep step) throws AbortException, FailedException {
            WorkflowTool tool = step.getTool();

            if (tool.getInputs() == null) {
                return;
            }

            if (tool.getInputs().size() != tool.getOutputs().size()) {
                throw (new FailedException("Tool needs same number of inputs as outpus"));
            }

            for (Entry<WorkflowToolData, String> input : step.getInputs().entrySet()) {
                Dataset ds = execution.getDataset(input.getValue());
                if (ds == null) {
                    throw (new AbortException("Dataset is missing."));
                }

                boolean foundit = false;
                for (Entry<String, WorkflowToolData> output : step.getOutputs().entrySet()) {
                    if (output.getValue().getDataId().equals(input.getKey().getDataId())) {
                        execution.setDataset(output.getKey(), ds);
                        foundit = true;
                    }
                }
                if (!foundit) {
                    throw (new FailedException("Could not set output for :" + input.getKey().getDataId()));
                }
            }
        }
    }
}
