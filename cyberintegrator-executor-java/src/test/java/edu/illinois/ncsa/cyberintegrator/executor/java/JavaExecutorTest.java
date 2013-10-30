/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.executor.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import edu.illinois.ncsa.cyberintegrator.AbortException;
import edu.illinois.ncsa.cyberintegrator.FailedException;
import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Dataset;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.JavaTool;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Parameter;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Parameter.ParameterType;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class JavaExecutorTest {
    @BeforeClass
    public static void setUp() throws Exception {
        new GenericXmlApplicationContext("testContext.xml");
    }

    @Test
    public void testJavaToolImplementation() throws Exception {
        Transaction t = SpringData.getTransaction();
        t.start();
        WorkflowToolDAO dao = SpringData.getBean(WorkflowToolDAO.class);

        DummyJavaTool dummy = new DummyJavaTool();

        WorkflowTool tool = new WorkflowTool();
        tool.setTitle(dummy.getName());
        tool.setDescription(dummy.getDescription());
        tool.setExecutor("java");

        JavaToolImplementation impl = new JavaToolImplementation();
        impl.setToolClassName(dummy.getClass().getName());
        tool.setImplementation(SpringData.objectToJSON(impl));

        dao.save(tool);

        String id = tool.getId();

        t.commit();

        Transaction t1 = SpringData.getTransaction();
        t1.start();

        dao = SpringData.getBean(WorkflowToolDAO.class);
        WorkflowTool tool2 = dao.findOne(id);

        JavaToolImplementation impl2 = SpringData.JSONToObject(tool2.getImplementation(), JavaToolImplementation.class);
        assertEquals(impl.getToolClassName(), impl2.getToolClassName());

        t1.commit();
    }

    @Test
    public void testExecutor() throws Exception {
        String datasetid = UUID.randomUUID().toString();

        edu.illinois.ncsa.domain.Dataset dataset = new edu.illinois.ncsa.domain.Dataset();
        FileDescriptor fd = SpringData.getFileStorage().storeFile(new ByteArrayInputStream("HELLO".getBytes("UTF-8")));
        dataset.addFileDescriptor(fd);
        SpringData.getBean(DatasetDAO.class).save(dataset);

        WorkflowStep step = new WorkflowStep();
        step.setTool(createTool());
        step.setInput("1", datasetid);
        SpringData.getBean(WorkflowStepDAO.class).save(step);

        Execution execution = new Execution();
        execution.setDataset(datasetid, dataset.getId());
        execution.setParameter(step.getParameters().values().iterator().next(), "WORLD");
        SpringData.getBean(ExecutionDAO.class).save(execution);

        JavaExecutor exec = new JavaExecutor();
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
        assertTrue(execution.hasDataset(outputid));

        dataset = SpringData.getBean(DatasetDAO.class).findOne(execution.getDataset(outputid));
        assertEquals(1, dataset.getFileDescriptors().size());

        InputStream is = SpringData.getFileStorage().readFile(dataset.getFileDescriptors().get(0));
        byte[] buf = new byte[is.available()];
        is.read(buf);
        is.close();

        assertEquals("HELLO WORLD", new String(buf, "UTF-8"));

        t.commit();
    }

    private WorkflowTool createTool() throws IOException {
        DummyJavaTool dummy = new DummyJavaTool();

        WorkflowTool tool = new WorkflowTool();
        tool.setTitle(dummy.getName());
        tool.setDescription(dummy.getDescription());
        tool.setExecutor("java");
        JavaToolImplementation impl = new JavaToolImplementation();
        impl.setToolClassName(dummy.getClass().getName());
        tool.setImplementation(SpringData.objectToJSON(impl));

        Dataset inputds = dummy.getInputs().iterator().next();
        WorkflowToolData input = new WorkflowToolData();
        input.setDataId(inputds.getID());
        input.setDescription(inputds.getDescription());
        input.setTitle(inputds.getName());
        input.setMimeType(inputds.getType());
        tool.addInput(input);

        Dataset outputds = dummy.getInputs().iterator().next();
        WorkflowToolData output = new WorkflowToolData();
        output.setDataId(outputds.getID());
        output.setDescription(outputds.getDescription());
        output.setTitle(outputds.getName());
        output.setMimeType(outputds.getType());
        tool.addOutput(output);

        Parameter param = dummy.getParameters().iterator().next();
        WorkflowToolParameter p = new WorkflowToolParameter();
        p.setParameterId(param.getID());
        p.setTitle(param.getName());
        p.setDescription(param.getDescription());
        p.setType(edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter.ParameterType.valueOf(param.getType().toString()));
        tool.addParameter(p);

        return tool;
    }

    public static class DummyJavaTool implements JavaTool {
        private String param  = null;
        private byte[] input  = null;
        private byte[] output = null;

        @Override
        public String getName() {
            return "DUMMY";
        }

        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public String getDescription() {
            return "DUMMY";
        }

        @Override
        public Collection<Dataset> getInputs() {
            Collection<Dataset> inputs = new HashSet<Dataset>();
            inputs.add(new Dataset("1", "input", "input", "text/plain"));
            return inputs;
        }

        @Override
        public void setInput(String id, InputStream input) {
            if ("1".equals(id)) {
                try {
                    this.input = new byte[input.available()];
                    input.read(this.input);
                } catch (IOException e) {
                    e.printStackTrace();
                    this.input = null;
                }
            }
        }

        @Override
        public Collection<Dataset> getOutputs() {
            Collection<Dataset> outputs = new HashSet<Dataset>();
            outputs.add(new Dataset("1", "output", "output", "text/plain"));
            return outputs;
        }

        @Override
        public InputStream getOutput(String id) {
            if ("1".equals(id)) {
                return new ByteArrayInputStream(output);
            }
            return null;
        }

        @Override
        public Collection<Parameter> getParameters() {
            Collection<Parameter> params = new HashSet<Parameter>();
            params.add(new Parameter("1", "param", "param", ParameterType.STRING, ""));
            return params;
        }

        @Override
        public void setParameter(String id, String value) {
            if ("1".equals(id)) {
                param = value;
            }
        }

        @Override
        public void setTempFolder(File tempfolder) {}

        @Override
        public void execute() throws AbortException, FailedException {
            if ((param == null) || (input == null)) {
                throw (new FailedException("param or input is null."));
            }
            output = new byte[input.length + 1 + param.length()];

            System.arraycopy(input, 0, output, 0, input.length);
            output[input.length] = ' ';
            try {
                System.arraycopy(param.getBytes("UTF-8"), 0, output, input.length + 1, param.length());
            } catch (UnsupportedEncodingException exc) {
                throw (new FailedException("UTF-8", exc));
            }

            System.out.println(new String(output));
        }
    }
}
