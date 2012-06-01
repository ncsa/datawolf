package edu.illinois.ncsa.cyberintegrator.executor.commandline;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.Type;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.springdata.SpringData;

public class CommandLineExecutorTest {
    Logger logger = LoggerFactory.getLogger(CommandLineExecutorTest.class);

    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Test
    public void testExecutor() throws Exception {
        CommandLineExecutor exec = new CommandLineExecutor();
        assertTrue(exec.canRun());

//        String datasetid = UUID.randomUUID().toString();

        // create dataset
//        Dataset dataset = new Dataset();
//        FileDescriptor fd = SpringData.getFileStorage().storeFile(new ByteArrayInputStream("HELLO".getBytes("UTF-8")));
//        dataset.addFileDescriptor(fd);

        // create workflow step
        WorkflowStep step = new WorkflowStep();
        step.setTool(createTool());
        step.setTitle("Jong");

        Execution execution = new Execution();
//        execution.setDataset(datasetid, dataset);

        File tmp = File.createTempFile("ciexec", "tmp");
        tmp.delete();
        tmp.mkdirs();

        exec.execute(tmp, execution, step);

        String outputid = step.getOutputs().values().iterator().next();
        logger.info(outputid);
        assertTrue(execution.hasDataset(outputid));

        Dataset dataset = execution.getDataset(outputid);

        InputStream is = SpringData.getFileStorage().readFile(dataset.getFileDescriptors().get(0));
        byte[] buf = new byte[is.available()];
        is.read(buf);
        is.close();

        System.out.println(new String(buf, "UTF-8"));
//
//        assertEquals("HELLO WORLD", new String(buf, "UTF-8"));
    }

    private WorkflowTool createTool() {
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

        tool.setImplementation(impl);

//        Dataset outputds = dummy.getInputs().iterator().next();
//        WorkflowToolData output = new WorkflowToolData();
//        output.setDataId(outputds.getID());
//        output.setDescription(outputds.getDescription());
//        output.setTitle(outputds.getName());
//        output.setMimeType(outputds.getType());
//        tool.addOutput(output);

        return tool;
    }

}
