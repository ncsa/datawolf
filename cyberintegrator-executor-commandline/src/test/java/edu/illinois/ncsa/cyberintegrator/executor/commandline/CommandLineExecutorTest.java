package edu.illinois.ncsa.cyberintegrator.executor.commandline;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.LogFile;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.Type;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.LogFileDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class CommandLineExecutorTest {
    Logger logger = LoggerFactory.getLogger(CommandLineExecutorTest.class);

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Test
    public void testExecutor() throws Exception {
        // create workflow step
        WorkflowStep step = new WorkflowStep();
        step.setTool(createTool());
        step.setTitle("Jong");
        SpringData.getBean(WorkflowStepDAO.class).save(step);

        Execution execution = new Execution();
        SpringData.getBean(ExecutionDAO.class).save(execution);

        CommandLineExecutor exec = new CommandLineExecutor();
        assertTrue(exec.isExecutorReady());
        exec.setJobInformation(execution, step);
        exec.startJob();

        int loop = 0;
        while ((exec.getState() != State.FINISHED) && (loop < 100)) {
            if (exec.getState() == State.FAILED) {
                System.out.println(exec.getLog());
                fail("Execution FAILED");
            }
            if (exec.getState() == State.ABORTED) {
                System.out.println(exec.getLog());
                fail("Execution ABORTED");
            }
            Thread.sleep(100);
            loop++;
        }
        if (exec.getState() != State.FINISHED) {
            System.out.println(exec.getLog());
            fail("Execution NEVER FINISHED");
        }

        Transaction t = SpringData.getTransaction();
        t.start(true);

        execution = SpringData.getBean(ExecutionDAO.class).findOne(execution.getId());

        String outputid = step.getOutputs().values().iterator().next();
        logger.info(outputid);
        assertTrue(execution.hasDataset(outputid));

        Dataset dataset = SpringData.getBean(DatasetDAO.class).findOne(execution.getDataset(outputid));

        InputStream is = SpringData.getFileStorage().readFile(dataset.getFileDescriptors().get(0));
        byte[] buf = new byte[is.available()];
        is.read(buf);
        is.close();

        assertTrue(new String(buf, "UTF-8").trim().startsWith("Active "));

        for (LogFile lf : SpringData.getBean(LogFileDAO.class).findAll()) {
            System.out.println("ID            = " + lf.getId());
            System.out.println("Execution ID  = " + lf.getExecutionId());
            System.out.println("Step ID       = " + lf.getStepId());
            System.out.println("Date          = " + lf.getDate());
            System.out.println("LOG           = " + lf.getLog().split("\n").length);
        }

        t.commit();
    }

    private WorkflowTool createTool() throws IOException {
        WorkflowTool tool = new WorkflowTool();
        tool.setTitle("Netstat");
        tool.setDescription("Command Line Executor");
        tool.setExecutor(CommandLineExecutor.EXECUTOR_NAME);

        String STDOUT_ID = "output";

        WorkflowToolData stdoutData = new WorkflowToolData();
        stdoutData.setMimeType("text/plain");
        stdoutData.setDataId(STDOUT_ID);
        stdoutData.setTitle("stdout");
        tool.addOutput(stdoutData);

        CommandLineOption option = new CommandLineOption();
        option.setFlag("-n");
        option.setType(Type.VALUE);

        CommandLineImplementation impl = new CommandLineImplementation();
        impl.setExecutable("netstat");
        impl.setCaptureStdOut(STDOUT_ID);
        impl.getCommandLineOptions().add(option);

        tool.setImplementation(new ObjectMapper().writeValueAsString(impl));

        return tool;
    }

}
