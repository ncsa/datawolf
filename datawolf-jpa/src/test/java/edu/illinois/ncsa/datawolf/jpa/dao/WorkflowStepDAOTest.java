package edu.illinois.ncsa.datawolf.jpa.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;

public class WorkflowStepDAOTest {

    private static Injector injector;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new TestModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowStepDao dao = injector.getInstance(WorkflowStepDao.class);

        WorkflowStep step = new WorkflowStep();
        dao.save(step);

        WorkflowStep step1 = dao.findOne(step.getId());

        assert (step.getId().equals(step1.getId()));
    }

    @Test
    public void testConnectWorkflowSteps() throws Exception {
        WorkflowStepDao stepDAO = injector.getInstance(WorkflowStepDao.class);

        WorkflowTool tool1 = getTool1();
        WorkflowTool tool2 = getTool2();

        WorkflowStep step1 = new WorkflowStep();
        step1.setTool(tool1);

        stepDAO.save(step1);

        // get first output
        String outputId = step1.getOutputs().values().iterator().next();

        WorkflowStep step2 = new WorkflowStep();
        step2.setTool(tool2);

        WorkflowToolData tool2Input = tool2.getInputs().get(0);
        step2.setInput(tool2Input, outputId);
        stepDAO.save(step2);

    }

    private WorkflowTool getTool1() {
        WorkflowTool tool = new WorkflowTool();
        tool.setTitle("TEST Tool");
        tool.setDescription("This is just a test");

        WorkflowToolData toolData = new WorkflowToolData();
        toolData.setTitle("input image");
        toolData.setMimeType("image/*");
        toolData.setDataId("1");
        tool.addInput(toolData);

        toolData = new WorkflowToolData();
        toolData.setTitle("output image");
        toolData.setMimeType("image/png");
        toolData.setDataId("1");
        tool.addOutput(toolData);

        tool.setExecutor("java");

        return tool;
    }

    private WorkflowTool getTool2() {
        WorkflowTool tool = new WorkflowTool();
        tool.setTitle("TEST Tool");
        tool.setDescription("This is just a test");

        WorkflowToolData toolData = new WorkflowToolData();
        toolData.setTitle("input image");
        toolData.setMimeType("image/png");
        toolData.setDataId("1");
        tool.addInput(toolData);

        toolData = new WorkflowToolData();
        toolData.setTitle("output image");
        toolData.setMimeType("image/jpg");
        toolData.setDataId("1");
        tool.addOutput(toolData);

        tool.setExecutor("java");

        return tool;
    }
}
