package edu.illinois.ncsa.cyberintegrator.tool.creator.java;

import java.io.File;
import java.util.Set;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.ImportExport;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.cyberintegrator.tool.creator.ToolCreator;
import edu.illinois.ncsa.cyberintegrator.tool.creator.Wizard;
import edu.illinois.ncsa.cyberintegrator.tool.creator.WizardPage;
import edu.illinois.ncsa.cyberintegrator.tool.creator.common.ToolResourceWizardPage;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.SpringData;

public class JavaWizard extends Wizard {
    private static final long      serialVersionUID = 1L;
    private static Logger          logger           = LoggerFactory.getLogger(JavaWizard.class);

    private ToolResourceWizardPage resourcepage;

    private ToolSelectWizardPage   toolselectpage;

    public JavaWizard(JFrame parent, String name) {
        super(parent, name);

        WorkflowTool oldtool = null;

        resourcepage = new ToolResourceWizardPage(this, oldtool);
        resourcepage.setMinimumFiles(1);
        resourcepage.addFilter(".jar", "JAR Files");
        addPage(resourcepage);

        toolselectpage = new ToolSelectWizardPage(this, oldtool);
        addPage(toolselectpage);
    }

    @Override
    public void showingPage(WizardPage page) {
        if (page == toolselectpage) {
            toolselectpage.setToolPath(resourcepage.getFileDescriptors());
        }
        super.showingPage(page);
    }

    public void createTool(String endpoint, Person person) {
        System.out.println("Creating Tool");

        try {
            // create the tool(s)
            Set<WorkflowTool> tools = toolselectpage.getTools();

            // add the resources.
            for (WorkflowTool tool : tools) {
                tool.setCreator(person);
                resourcepage.updateTool(tool);

                // save the tool
                tool = SpringData.getBean(WorkflowToolDAO.class).save(tool);

                // upload tool
                File zipfile = File.createTempFile("tool", ".zip");
                ImportExport.exportTool(zipfile, tool.getId());
                ToolCreator.postTool(zipfile, endpoint);
                zipfile.delete();
            }
        } catch (Exception exc) {
            logger.error("Could not save tool.", exc);
        }
    }
}
