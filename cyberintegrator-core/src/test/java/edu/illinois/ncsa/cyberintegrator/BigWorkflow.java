package edu.illinois.ncsa.cyberintegrator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.EngineTest.DummyExecutor;
import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class BigWorkflow implements Runnable {
    private static Logger   logger  = LoggerFactory.getLogger(BigWorkflow.class);

    private static Person   person;
    private static Engine   engine;
    private static Workflow workflow;

    private static int      CLIENTS = 10;
    private static int      JOBS    = 20;

    public static void main(String[] args) throws Exception {
        // setup spring data
        new GenericXmlApplicationContext("bigTestContext.xml");

        // create the engine with many local threads
        System.out.println(Runtime.getRuntime().availableProcessors() - 1);
        LocalExecutor.setWorkers(Runtime.getRuntime().availableProcessors() - 1);
        engine = new Engine();
        engine.addExecutor(new DummyExecutor());

        person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");

        // create a workflow with a step
        workflow = EngineTest.createWorkflow(person, 4, false);
        workflow = SpringData.getBean(WorkflowDAO.class).save(workflow);

        for (int i = 0; i < CLIENTS; i++) {
            new Thread(new BigWorkflow()).start();
        }
    }

    public void run() {
        // add a dataset
        Dataset dataset = EngineTest.createDataset(person);
        SpringData.getBean(DatasetDAO.class).save(dataset);

        // create the executions
        Map<String, Long> executionids = new HashMap<String, Long>();
        long l = System.currentTimeMillis();
        for (int i = 0; i < JOBS; i++) {
            long k = System.currentTimeMillis();
            Execution execution = EngineTest.createExecution(person, workflow);
            execution.setDataset("dataset", dataset.getId());
            SpringData.getBean(ExecutionDAO.class).save(execution);
            engine.execute(execution);
            executionids.put(execution.getId(), System.currentTimeMillis());

            logger.info(String.format("Took %d ms for submission", System.currentTimeMillis() - k));
        }
        logger.info(String.format("Took %d ms for all submissions", System.currentTimeMillis() - l));

        // check to see if all workflows are done
        int loop = 0;
        boolean alldone = false;
        while ((loop < 100000) && !alldone) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.info("interrupted", e);
            }
            loop++;

            // check for done
            alldone = true;
            Iterator<String> keys = executionids.keySet().iterator();
            while (keys.hasNext()) {
                String id = keys.next();
                if (engine.getSteps(id).size() > 0) {
                    alldone = false;
                } else {
                    logger.info(String.format("Took %d ms for execution.", System.currentTimeMillis() - executionids.get(id)));
                    keys.remove();
                }
            }
        }
        logger.info(String.format("Took %d ms for all submissions and executions.", System.currentTimeMillis() - l));

        // make sure everything is done
        for (String id : executionids.keySet()) {
            Transaction t = SpringData.getTransaction();
            try {
                t.start();
                Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(id);
                for (WorkflowStep step : workflow.getSteps()) {
                    if (execution.getStepState(step.getId()) != State.FINISHED) {
                        logger.warn(String.format("[%s:%s] is not FINISHED.", id, step.getId()));
                    }
                }
                t.commit();
            } catch (Exception e) {
                logger.error("Could not get results.", e);
            }
        }

    }
}