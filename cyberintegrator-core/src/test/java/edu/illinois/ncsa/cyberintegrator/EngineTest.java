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
 * @TODO add test to test loadQueue - create engine, add workflow, stop engine
 *       and start new engine
 * 
 */
public class EngineTest {
    public static final String STEP_WITH_ERROR = "STEP WITH ERROR";
    public static final String SLOW_STEP       = "SLOW STEP";
    protected Engine           engine;

    @BeforeClass
    public static void setUp() throws Exception {
        // new GenericXmlApplicationContext("testContext.xml");
    }

    @Before
    public void createEngine() {
        engine = new Engine();
        engine.addExecutor(new DummyExecutor());
        new GenericXmlApplicationContext("testContext.xml");
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
    public void testStepWithError() throws Exception {
        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");

        // create a workflow with a step
        Workflow workflow = createWorkflow(person, 2, true);
        SpringData.getBean(WorkflowDAO.class).save(workflow);

        // create the execution
        Execution execution = createExecution(person, workflow);
        execution.setProperty("HELLO", "WORLD");
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // add a dataset
        Dataset dataset = createDataset(person);
        SpringData.getBean(DatasetDAO.class).save(dataset);
        execution.setDataset("dataset", dataset.getId());
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // submit a single step
        engine.execute(execution);

        // check to see if all workflows are done
        int loop = 0;
        while ((loop < 100) && engine.getSteps(execution.getId()).size() > 0) {
            Thread.sleep(100);
            loop++;
        }

        // make sure everything is done
        assertEquals(0, engine.getSteps(execution.getId()).size());

        Transaction t = SpringData.getTransaction();
        t.start();
        try {
            execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());
            assertEquals("WORLD", execution.getProperty("HELLO"));
            assertEquals(State.FAILED, execution.getStepState(workflow.getSteps().get(0).getId()));
            assertEquals(State.ABORTED, execution.getStepState(workflow.getSteps().get(1).getId()));
        } finally {
            t.commit();
        }
    }

//    @Test
//    public void testTimeOut1() throws Exception {
//        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");
//
//        // create a workflow with a step
//        Workflow workflow = createWorkflow(person, 2, false);
//        SpringData.getBean(WorkflowDAO.class).save(workflow);
//
//        // create the execution
//        Execution execution = createExecution(person, workflow);
//        SpringData.getBean(ExecutionDAO.class).save(execution);
//
//        // submit a single step
//        engine.setTimeout(1);
//        engine.execute(execution);
//
//        // check to see if all workflows are done
//        int loop = 0;
//        while ((loop < 100) && engine.getSteps(execution.getId()).size() > 0) {
//            Thread.sleep(100);
//            loop++;
//        }
//
//        // make sure everything is done
//        assertEquals(0, engine.getSteps(execution.getId()).size());
//
//        Transaction t = SpringData.getTransaction();
//        try {
//            t.start();
//            execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());
//            assertEquals(State.ABORTED, execution.getStepState(workflow.getSteps().get(0).getId()));
//            assertEquals(State.ABORTED, execution.getStepState(workflow.getSteps().get(1).getId()));
//        } finally {
//            t.commit();
//        }
//    }
//
//    @Test
//    public void testTimeOut2() throws Exception {
//        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");
//
//        // create a workflow with a step
//        Workflow workflow = createWorkflow(person, 2, false);
//        // break input of second step
//        WorkflowStep step = workflow.getSteps().get(1);
//        step.setInput(step.getTool().getInputs().get(0), "broken");
//        SpringData.getBean(WorkflowDAO.class).save(workflow);
//
//        // create the execution
//        Execution execution = createExecution(person, workflow);
//        Dataset dataset = createDataset(person);
//        SpringData.getBean(DatasetDAO.class).save(dataset);
//        execution.setDataset("dataset", dataset.getId());
//        SpringData.getBean(ExecutionDAO.class).save(execution);
//
//        // submit a single step
//        engine.setTimeout(1);
//        engine.execute(execution);
//
//        // check to see if all workflows are done
//        int loop = 0;
//        while ((loop < 100) && engine.getSteps(execution.getId()).size() > 0) {
//            Thread.sleep(100);
//            loop++;
//        }
//
//        // make sure everything is done
//        assertEquals(0, engine.getSteps(execution.getId()).size());
//
//        Transaction t = SpringData.getTransaction();
//        t.start();
//        try {
//            execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());
//            assertEquals(State.FINISHED, execution.getStepState(workflow.getSteps().get(0).getId()));
//            assertEquals(State.ABORTED, execution.getStepState(workflow.getSteps().get(1).getId()));
//        } finally {
//            t.commit();
//        }
//    }
//
//    @Test
//    public void testTimeOut3() throws Exception {
//        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");
//
//        // create a workflow with a step
//        Workflow workflow = createWorkflow(person, 2, false);
//        // mark first step as slooooow step
//        WorkflowStep step = workflow.getSteps().get(0);
//        step.setTitle(SLOW_STEP);
//        SpringData.getBean(WorkflowDAO.class).save(workflow);
//
//        // create the execution
//        Execution execution = createExecution(person, workflow);
//        Dataset dataset = createDataset(person);
//        SpringData.getBean(DatasetDAO.class).save(dataset);
//        execution.setDataset("dataset", dataset.getId());
//        SpringData.getBean(ExecutionDAO.class).save(execution);
//
//        // submit a single step
//        engine.setTimeout(1);
//        engine.execute(execution);
//
//        // check to see if all workflows are done
//        int loop = 0;
//        while ((loop < 100) && engine.getSteps(execution.getId()).size() > 0) {
//            Thread.sleep(100);
//            loop++;
//        }
//
//        // make sure everything is done
//        assertEquals(0, engine.getSteps(execution.getId()).size());
//
//        Transaction t = SpringData.getTransaction();
//        t.start();
//        try {
//            execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());
//            assertEquals(State.FINISHED, execution.getStepState(workflow.getSteps().get(0).getId()));
//            assertEquals(State.FINISHED, execution.getStepState(workflow.getSteps().get(1).getId()));
//        } finally {
//            t.commit();
//        }
//    }
//
//    @Test
//    public void testTimeOut4() throws Exception {
//        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");
//
//        // create a workflow with a step
//        Workflow workflow = createWorkflow(person, 2, false);
//        SpringData.getBean(WorkflowDAO.class).save(workflow);
//
//        // create the execution
//        Execution execution = createExecution(person, workflow);
//        SpringData.getBean(ExecutionDAO.class).save(execution);
//
//        // submit a single step
//        engine.setTimeout(2);
//        engine.execute(execution);
//
//        // Wait a second
//        Thread.sleep(1000);
//        Dataset dataset = createDataset(person);
//        SpringData.getBean(DatasetDAO.class).save(dataset);
//        execution.setDataset("dataset", dataset.getId());
//        SpringData.getBean(ExecutionDAO.class).save(execution);
//
//        // check to see if all workflows are done
//        int loop = 0;
//        while ((loop < 100) && engine.getSteps(execution.getId()).size() > 0) {
//            Thread.sleep(100);
//            loop++;
//        }
//
//        // make sure everything is done
//        assertEquals(0, engine.getSteps(execution.getId()).size());
//
//        Transaction t = SpringData.getTransaction();
//        t.start();
//        try {
//            execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());
//            assertEquals(State.FINISHED, execution.getStepState(workflow.getSteps().get(0).getId()));
//            assertEquals(State.FINISHED, execution.getStepState(workflow.getSteps().get(1).getId()));
//        } finally {
//            t.commit();
//        }
//    }

    @Test
    public void testWorkflowCancel() throws Exception {
        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");

        // create a workflow with a step
        Workflow workflow = createWorkflow(person, 2, false);
        SpringData.getBean(WorkflowDAO.class).save(workflow);

        // create the execution
        Execution execution = createExecution(person, workflow);
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // submit a single step
        engine.execute(execution);

        // cancel execution
        engine.stop(execution.getId());

        // check to see if all workflows are done
        int loop = 0;
        while ((loop < 100) && engine.getSteps(execution.getId()).size() > 0) {
            Thread.sleep(100);
            loop++;
        }

        // make sure everything is done
        assertEquals(0, engine.getSteps(execution.getId()).size());

        Transaction t = SpringData.getTransaction();
        t.start();
        try {
            execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());
            assertEquals(State.ABORTED, execution.getStepState(workflow.getSteps().get(0).getId()));
            assertEquals(State.ABORTED, execution.getStepState(workflow.getSteps().get(1).getId()));
        } finally {
            t.commit();
        }
    }

    @Test
    public void testRunAllSteps() throws Exception {
        Person person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");

        // create a workflow with a step
        Workflow workflow = createWorkflow(person, 20, false);
        SpringData.getBean(WorkflowDAO.class).save(workflow);

        // create the execution
        Execution execution = createExecution(person, workflow);
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // submit a single step
        engine.execute(execution);

        // make sure the step is there and not running
        assertEquals(workflow.getSteps().size(), engine.getSteps(execution.getId()).size());
        // TODO RK : check state of all steps
        // assertEquals(workflow.getSteps().size(),
// engine.getSteps(execution.getId(), State.WAITING).size());

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
        execution.setDataset("dataset", dataset.getId());
        SpringData.getBean(ExecutionDAO.class).save(execution);

        // check to see if all workflows are done
        int loop = 0;
        while ((loop < 1000) && engine.getSteps(execution.getId()).size() > 0) {
            Thread.sleep(100);
            loop++;
        }

        // make sure everything is done
        assertEquals(0, engine.getSteps(execution.getId()).size());

        Transaction t = SpringData.getTransaction();
        t.start(false);
        try {
            execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());
            for (WorkflowStep step : workflow.getSteps()) {
                assertEquals(State.FINISHED, execution.getStepState(step.getId()));
            }
        } finally {
            t.commit();
        }

        // assert all steps send notification
        assertEquals(workflow.getSteps().size(), finished.size());
        assertEquals(workflow.getSteps().size(), running.size());
        for (WorkflowStep step : workflow.getSteps()) {
            assertTrue("finished did not contain workflow step", finished.contains(step));
            assertTrue("running did not contain workflow step", running.contains(step));
        }
    }

    protected static Dataset createDataset(Person creator) {
        Dataset dataset = new Dataset();
        dataset.setCreator(creator);
        dataset.setTitle("TEST DATASET");

        return dataset;
    }

    protected static Execution createExecution(Person creator, Workflow workflow) {
        Execution execution = new Execution();
        execution.setCreator(creator);
        execution.setWorkflow(workflow);

        return execution;
    }

    protected static Workflow createWorkflow(Person creator, int steps, boolean errorStep) {
        return createWorkflow(creator, steps, errorStep, false);
    }

    protected static Workflow createWorkflow(Person creator, int steps, boolean errorStep, boolean linear) {
        Workflow workflow = new Workflow();
        workflow.setCreator(creator);
        workflow.setTitle("TEST WORKFLOW");

        String last = "dataset";
        List<String> ids = new ArrayList<String>();
        ids.add(last);
        for (int i = 0; i < steps; i++) {
            WorkflowStep step = createWorkflowStep(creator);
            if (errorStep && (i == 0)) {
                step.setTitle(STEP_WITH_ERROR);
                step.setInput(step.getTool().getInputs().get(0), "dataset");
            } else if (errorStep && (i == 1)) {
                step.setInput(step.getTool().getInputs().get(0), ids.get(1));
            } else if (linear) {
                step.setInput(step.getTool().getInputs().get(0), last);
            } else {
                step.setInput(step.getTool().getInputs().get(0), ids.get(new Random().nextInt(ids.size())));
            }
            workflow.addStep(step);
            last = step.getOutputs().values().iterator().next();
            ids.add(last);
        }

        return workflow;
    }

    protected static WorkflowStep createWorkflowStep(Person creator) {
        WorkflowStep step = new WorkflowStep();
        step.setCreator(creator);
        step.setTool(createWorkflowTool(creator));

        return step;
    }

    protected static WorkflowTool createWorkflowTool(Person creator) {
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

    protected static class DummyExecutor extends LocalExecutor {
        @Override
        public String getExecutorName() {
            return "dummy";
        }

        @Override
        public void execute(File cwd) throws AbortException, FailedException {
            Transaction t = SpringData.getTransaction();
            try {
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

                // throw failed exception in case of error
                if (STEP_WITH_ERROR.equals(step.getTitle())) {
                    throw (new FailedException("Some Error"));
                }

                if (SLOW_STEP.equals(step.getTitle())) {
                    Thread.sleep(3000);
                }

                // Do some computation
                Thread.sleep(200);

                String firstinput = step.getInputs().values().iterator().next();
                Dataset dataset = SpringData.getBean(DatasetDAO.class).findOne(execution.getDataset(firstinput));
                if (dataset == null) {
                    throw (new FailedException("Dataset is not found."));
                }

                // why do we need a new dataset?
                // dataset = new Dataset();
                String firstoutput = step.getOutputs().values().iterator().next();
                execution.setDataset(firstoutput, dataset.getId());

                t.commit();
            } catch (Exception e) {
                try {
                    t.rollback();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                throw (new FailedException("job failed.", e));
            }
        }
    }
}
