package edu.illinois.ncsa.cyberintegrator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

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

    private static int      CLIENTS = 1;
    private static int      JOBS    = 50;

    public static void main(String[] args) throws Exception {
        // setup spring data
        new GenericXmlApplicationContext("bigTestContext.xml");

        // create the engine with many local threads
        engine = SpringData.getBean(Engine.class);
        engine.addExecutor(new DummyExecutor());

        person = Person.createPerson("Rob", "Kooper", "kooper@illinois.edu");

        // create a workflow with a step
        workflow = EngineTest.createWorkflow(person, 4, false, true);
        workflow.getSteps().get(2).setTitle(EngineTest.SLOW_STEP);
        workflow = SpringData.getBean(WorkflowDAO.class).save(workflow);

        for (int i = 0; i < CLIENTS; i++) {
            new Thread(new BigWorkflow(i)).start();
        }
    }

    private int client;

    public BigWorkflow(int client) {
        this.client = client;
    }

    public void run() {
        // add a dataset
        Dataset dataset = EngineTest.createDataset(person);
        SpringData.getBean(DatasetDAO.class).save(dataset);

        // create the executions
        List<String> ids = new ArrayList<String>();
        Map<String, Long> executionids = new HashMap<String, Long>();
        long l = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        for (int i = 0; i < JOBS; i++) {
            long k = System.currentTimeMillis();
            Execution execution = EngineTest.createExecution(person, workflow);
            execution.setProperty("TITLE", String.format("%02d-%02d", client, i));
            execution.setDataset("dataset", dataset.getId());
            try {
                execution = SpringData.getBean(ExecutionDAO.class).save(execution);
                execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());
                if (execution != null) {
                    engine.execute(execution);
                    executionids.put(execution.getId(), System.currentTimeMillis());
                    ids.add(execution.getId());
                } else {
                    logger.error("ERROR CREATING EXECUTION.");
                }

                logger.info(String.format("Took %d ms for submission", System.currentTimeMillis() - k));
            } catch (Throwable t) {
                logger.error("ERROR", t);
            }
        }
        logger.info(String.format("Took %d ms for all submissions", System.currentTimeMillis() - l));

        // check to see if all workflows are done
        int loop = 0;
        boolean alldone = false;
        Set<String> keys = new HashSet<String>(executionids.keySet());
        while ((loop < 100000) && !alldone) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.info("interrupted", e);
            }
            loop++;

            // check for done
            alldone = true;
            Iterator<String> iter = keys.iterator();
            while (iter.hasNext()) {
                String id = iter.next();
                if (engine.getSteps(id).size() > 0) {
                    alldone = false;
                } else {
                    logger.info(String.format("Took %d ms for execution.", System.currentTimeMillis() - executionids.get(id)));
                    iter.remove();
                }
            }
        }
        logger.info(String.format("Took %d ms for all submissions and executions.", System.currentTimeMillis() - l));

        long end = System.currentTimeMillis();

        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        double scale_x = (double) (img.getWidth()) / (end - start);
        double scale_y = (double) (img.getHeight()) / (executionids.size());
        Graphics2D g2d = img.createGraphics();

        // make sure everything is done
        int y = 0;
        Color[] colors = { Color.RED, Color.GREEN, Color.BLUE, Color.WHITE };
        for (String id : ids) {
            Transaction t = SpringData.getTransaction();
            try {
                t.start();
                Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(id);
                String row = execution.getProperty("TITLE") + "\t" + execution.getId() + "\t" + (execution.getDate().getTime() - start) + "\t";
                int col = 0;
                for (WorkflowStep step : workflow.getSteps()) {
                    if (execution.getStepState(step.getId()) != State.FINISHED) {
                        logger.warn(String.format("[%s:%s] is not FINISHED.", id, step.getId()));
                    } else {
                        row += execution.getStepState(step.getId()) + "\t";
                        row += (execution.getStepQueued(step.getId()).getTime() - start) + "\t";
                        row += (execution.getStepStart(step.getId()).getTime() - start) + "\t";
                        row += (execution.getStepEnd(step.getId()).getTime() - start) + "\t";

                        g2d.setColor(colors[col]);
                        int s = (int) (execution.getStepQueued(step.getId()).getTime() - start);
                        int e = (int) (execution.getStepStart(step.getId()).getTime() - start);
                        g2d.fillRect((int) (s * scale_x), (int) (y + scale_y * 0.1), (int) ((e - s) * scale_x), (int) (scale_y * 0.2));
                        s = (int) (execution.getStepStart(step.getId()).getTime() - start);
                        e = (int) (execution.getStepEnd(step.getId()).getTime() - start);
                        g2d.fillRect((int) (s * scale_x), (int) (y + scale_y * 0.3), (int) ((e - s) * scale_x), (int) (scale_y * 0.6));
                    }
                    col++;
                }

                y += scale_y;
                System.out.println(row);
                t.commit();
            } catch (Exception e) {
                try {
                    t.commit();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                logger.error("Could not get results.", e);
            }
        }

        JFrame frm = new JFrame("Image");
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.add(new JLabel(new ImageIcon(img)));
        frm.pack();
        frm.setVisible(true);

        try {
            ImageIO.write(img, "png", new File("/Users/kooper/Desktop/time.png"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}