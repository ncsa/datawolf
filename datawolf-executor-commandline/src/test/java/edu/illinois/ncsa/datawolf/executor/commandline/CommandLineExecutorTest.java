package edu.illinois.ncsa.datawolf.executor.commandline;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.Execution.State;
import edu.illinois.ncsa.datawolf.domain.LogFile;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.datawolf.domain.dao.LogFileDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption.Type;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Persistence;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.util.BeanUtil;

public class CommandLineExecutorTest {
    Logger                  logger = LoggerFactory.getLogger(CommandLineExecutorTest.class);

    private static Injector injector;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new TestModule());

        Persistence.setInjector(injector);
        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();
    }

    @Test
    public void testExecutor() throws Exception {
        // create workflow step
        WorkflowStep step = new WorkflowStep();
        step.setTool(createTool());
        step.setTitle("Jong");
        injector.getInstance(WorkflowStepDao.class).save(step);

        Execution execution = new Execution();
        injector.getInstance(ExecutionDao.class).save(execution);

        CommandLineExecutor exec = injector.getInstance(CommandLineExecutor.class);
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

        // Transaction t = SpringData.getTransaction();
        // t.start(true);

        execution = injector.getInstance(ExecutionDao.class).findOne(execution.getId());

        String outputid = step.getOutputs().values().iterator().next();
        logger.info(outputid);
        assertTrue(execution.hasDataset(outputid));

        Dataset dataset = injector.getInstance(DatasetDao.class).findOne(execution.getDataset(outputid));

        InputStream is = injector.getInstance(FileStorage.class).readFile(dataset.getFileDescriptors().get(0));
        byte[] buf = new byte[is.available()];
        is.read(buf);
        is.close();

        assertTrue(new String(buf, "UTF-8").trim().startsWith("Active "));

        for (LogFile lf : injector.getInstance(LogFileDao.class).findAll()) {
            System.out.println("ID            = " + lf.getId());
            System.out.println("Execution ID  = " + lf.getExecutionId());
            System.out.println("Step ID       = " + lf.getStepId());
            System.out.println("Date          = " + lf.getDate());
            System.out.println("LOG           = " + lf.getLog().getDataURL());
        }

        // t.commit();
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

        tool.setImplementation(BeanUtil.objectToJSON(impl));

        return tool;
    }

}
