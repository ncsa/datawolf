/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.event.StepStateChangedEvent;
import edu.illinois.ncsa.cyberintegrator.event.StepStateChangedHandler;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class EngineTest {
    protected Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Before
    public void createEngine() {
        engine = new Engine();
        engine.addExecutor(new DummyExecutor());
    }

    @After
    public void stopEngine() {
        engine.stopAll();
    }

//    @Test
//    public void testWorkers() {
//        ThreadedEngine engine = (ThreadedEngine) this.engine;
//        engine.setWorkers(1);
//
//        assertFalse(engine.isStarted());
//        assertEquals(1, engine.getWorkers());
//
//        engine.setWorkers(2);
//        assertEquals(2, engine.getWorkers());
//
//        engine.startEngine();
//        assertTrue(engine.isStarted());
//
//        assertEquals(2, engine.getWorkers());
//
//        engine.setWorkers(1);
//        assertEquals(1, engine.getWorkers());
//
//        engine.setWorkers(3);
//        assertEquals(3, engine.getWorkers());
//    }

    @Test
    public void testRunAllSteps() throws Exception {
        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");

        // create a workflow with a step
        Workflow workflow = createWorkflow(person, 20);
        SpringData.getBean(WorkflowDAO.class).save(workflow);

        // create the execution
        Execution execution = createExecution(person, workflow);
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // submit a single step
        engine.execute(execution);

        // make sure the step is there and not running
        assertEquals(workflow.getSteps().size(), engine.getSteps().size());
        assertEquals(workflow.getSteps().size(), engine.getSteps(State.WAITING).size());

        // add a listener
        final List<WorkflowStep> running = new ArrayList<WorkflowStep>();
        final List<WorkflowStep> finished = new ArrayList<WorkflowStep>();
        SpringData.getEventBus().addHandler(StepStateChangedEvent.TYPE, new StepStateChangedHandler() {
            @Override
            public void stepStateChange(StepStateChangedEvent event) {
                if (event.getNewState() == Execution.State.FINISHED) {
                    System.out.println("F : " + event.getStep());
                    finished.add(event.getStep());
                }
                if (event.getNewState() == Execution.State.RUNNING) {
                    System.out.println("R : " + event.getStep());
                    running.add(event.getStep());
                }
            }
        });

        // add a dataset
        Dataset dataset = createDataset(person);
        SpringData.getBean(DatasetDAO.class).save(dataset);
        execution.setDataset("input", dataset);
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // check to see if all workflows are done
        int loop = 0;
        while ((loop < 1000) && engine.getSteps().size() > 0) {
            Thread.sleep(100);
            loop++;
        }

        // make sure everything is done
        assertEquals(0, engine.getSteps().size());

        Transaction t = SpringData.getTransaction();
        t.start(false);
        execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());
        for (WorkflowStep step : workflow.getSteps()) {
            assertEquals(State.FINISHED, execution.getStepState(step.getId()));
        }
        t.commit();

        // assert all steps send notification
        assertEquals(workflow.getSteps().size(), finished.size());
        assertEquals(workflow.getSteps().size(), running.size());
        for (WorkflowStep step : workflow.getSteps()) {
            assertTrue("finished did not contain workflow step", finished.contains(step));
            assertTrue("running did not contain workflow step", running.contains(step));
        }
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

    static class DummyExecutor extends LocalExecutor {
        @Override
        public String getExecutorName() {
            return "dummy";
        }

        @Override
        public void execute(File cwd) throws AbortException, FailedException {
            try {
                Transaction t = SpringData.getTransaction();
                t.start();
                WorkflowStep step = SpringData.getBean(WorkflowStepDAO.class).findOne(getStepId());
                Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(getExecutionId());
                WorkflowTool tool = step.getTool();

                if ((tool.getInputs() == null) || (tool.getOutputs() == null)) {
                    throw (new FailedException("Tool needs inputs and outpus"));
                }

                if ((tool.getInputs().size() != 1) || (tool.getOutputs().size() != 1)) {
                    throw (new FailedException("Tool needs 1 inputs or outpus"));
                }

                // Do some computation
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}

                Dataset dataset = execution.getDataset(step.getInputs().values().iterator().next());
                if (dataset == null) {
                    throw (new FailedException("Dataset is not found."));
                }

                // why do we need a new dataset?
                // dataset = new Dataset();
                execution.setDataset(step.getOutputs().keySet().iterator().next(), dataset);

                t.commit();
            } catch (Exception e) {
                throw (new FailedException("job failed.", e));
            }
        }

    }
}
