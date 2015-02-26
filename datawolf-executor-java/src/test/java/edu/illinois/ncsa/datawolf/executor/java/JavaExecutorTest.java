/**
 * 
 */
package edu.illinois.ncsa.datawolf.executor.java;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

import edu.illinois.ncsa.datawolf.AbortException;
import edu.illinois.ncsa.datawolf.FailedException;
import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.Execution.State;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter;
import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDao;
import edu.illinois.ncsa.datawolf.executor.java.tool.Dataset;
import edu.illinois.ncsa.datawolf.executor.java.tool.JavaTool;
import edu.illinois.ncsa.datawolf.executor.java.tool.Parameter;
import edu.illinois.ncsa.datawolf.executor.java.tool.Parameter.ParameterType;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Persistence;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.util.BeanUtil;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class JavaExecutorTest {
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
    public void testJavaToolImplementation() throws Exception {
        WorkflowToolDao dao = injector.getInstance(WorkflowToolDao.class);

        DummyJavaTool dummy = new DummyJavaTool();

        WorkflowTool tool = new WorkflowTool();
        tool.setTitle(dummy.getName());
        tool.setDescription(dummy.getDescription());
        tool.setExecutor("java");

        JavaToolImplementation impl = new JavaToolImplementation();
        impl.setToolClassName(dummy.getClass().getName());
        tool.setImplementation(BeanUtil.objectToJSON(impl));

        dao.save(tool);

        String id = tool.getId();

        dao = injector.getInstance(WorkflowToolDao.class);
        WorkflowTool tool2 = dao.findOne(id);

        JavaToolImplementation impl2 = BeanUtil.JSONToObject(tool2.getImplementation(), JavaToolImplementation.class);
        assertEquals(impl.getToolClassName(), impl2.getToolClassName());

    }

    @Test
    public void testExecutor() throws Exception {
        String datasetid = UUID.randomUUID().toString();

        edu.illinois.ncsa.domain.Dataset dataset = new edu.illinois.ncsa.domain.Dataset();
        FileDescriptor fd = injector.getInstance(FileStorage.class).storeFile(new ByteArrayInputStream("HELLO".getBytes("UTF-8")));
        dataset.addFileDescriptor(fd);
        injector.getInstance(DatasetDao.class).save(dataset);

        WorkflowStep step = new WorkflowStep();
        step.setTool(createTool());
        step.setInput("1", datasetid);
        injector.getInstance(WorkflowStepDao.class).save(step);

        Execution execution = new Execution();
        execution.setDataset(datasetid, dataset.getId());
        execution.setParameter(step.getParameters().values().iterator().next(), "WORLD");
        injector.getInstance(ExecutionDao.class).save(execution);

        // JavaExecutor exec = new JavaExecutor();
        JavaExecutor exec = injector.getInstance(JavaExecutor.class);
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

        UnitOfWork work = injector.getInstance(UnitOfWork.class);
        work.begin();
        execution = injector.getInstance(ExecutionDao.class).findOne(execution.getId());

        String outputid = step.getOutputs().values().iterator().next();
        assertTrue(execution.hasDataset(outputid));

        dataset = injector.getInstance(DatasetDao.class).findOne(execution.getDataset(outputid));
        assertEquals(1, dataset.getFileDescriptors().size());

        InputStream is = injector.getInstance(FileStorage.class).readFile(dataset.getFileDescriptors().get(0));
        byte[] buf = new byte[is.available()];
        is.read(buf);
        is.close();

        assertEquals("HELLO WORLD", new String(buf, "UTF-8"));
        work.end();

    }

    private WorkflowTool createTool() throws IOException {
        DummyJavaTool dummy = new DummyJavaTool();

        WorkflowTool tool = new WorkflowTool();
        tool.setTitle(dummy.getName());
        tool.setDescription(dummy.getDescription());
        tool.setExecutor("java");
        JavaToolImplementation impl = new JavaToolImplementation();
        impl.setToolClassName(dummy.getClass().getName());
        tool.setImplementation(BeanUtil.objectToJSON(impl));

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
        p.setType(edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter.ParameterType.valueOf(param.getType().toString()));
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
