package edu.illinois.ncsa.cyberintegrator.springdata;

import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class WorkflowStepDAOTest {

    @BeforeClass
    public static void setUp() throws Exception {
        SpringData.loadXMLContext("testContext.xml");
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowStepDAO dao = SpringData.getBean(WorkflowStepDAO.class);

        WorkflowStep step = new WorkflowStep();
        dao.save(step);

        WorkflowStep step1 = dao.findOne(step.getId());

        assert (step.getId().equals(step1.getId()));
    }

    @Test
    public void testConnectWorkflowSteps() throws Exception {
        Transaction t = SpringData.getTransaction();
        t.start();
        WorkflowStepDAO stepDAO = SpringData.getBean(WorkflowStepDAO.class);

        WorkflowTool tool1 = getTool1();
        WorkflowTool tool2 = getTool2();

        WorkflowStep step1 = new WorkflowStep();
        step1.setTool(tool1);

        stepDAO.save(step1);

        WorkflowToolData tool1Output = step1.getTool().getOutputs().get(0);
        String outputId = "";
        for (Entry<String, WorkflowToolData> entry : step1.getOutputs().entrySet()) {
            if (entry.getValue().equals(tool1Output)) {
                outputId = entry.getKey();
                break;
            }
        }

        WorkflowStep step2 = new WorkflowStep();
        step2.setTool(tool2);

        WorkflowToolData tool2Input = tool2.getInputs().get(0);
        step2.setInput(tool2Input, outputId);
        stepDAO.save(step2);
        t.commit();
    }

    private WorkflowTool getTool1() {
        WorkflowToolDAO toolDAO = SpringData.getBean(WorkflowToolDAO.class);
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
        WorkflowToolDAO toolDAO = SpringData.getBean(WorkflowToolDAO.class);
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
