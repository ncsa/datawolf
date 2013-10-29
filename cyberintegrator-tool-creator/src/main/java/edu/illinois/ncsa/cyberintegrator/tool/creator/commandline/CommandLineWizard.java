package edu.illinois.ncsa.cyberintegrator.tool.creator.commandline;

import java.io.File;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.ImportExport;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.cyberintegrator.tool.creator.ToolCreator;
import edu.illinois.ncsa.cyberintegrator.tool.creator.Wizard;
import edu.illinois.ncsa.cyberintegrator.tool.creator.common.InfoWizardPage;
import edu.illinois.ncsa.cyberintegrator.tool.creator.common.ToolResourceWizardPage;
import edu.illinois.ncsa.cyberintegrator.tool.creator.java.JavaWizard;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.SpringData;

public class CommandLineWizard extends Wizard {
    private static final long      serialVersionUID = 1L;
    private static Logger          logger           = LoggerFactory.getLogger(JavaWizard.class);

    private InfoWizardPage         infoPage;
    private CommandLineWizardPage  cmdPage;
    private ToolResourceWizardPage resourcePage;
    private EnvWizardPage          envPage;

    public CommandLineWizard(JFrame parent, String name) {
        super(parent, name);

        WorkflowTool oldtool = null;

        infoPage = new InfoWizardPage(this, oldtool);
        addPage(infoPage);

        cmdPage = new CommandLineWizardPage(this, oldtool);
        addPage(cmdPage);

        resourcePage = new ToolResourceWizardPage(this, oldtool);
        addPage(resourcePage);

        envPage = new EnvWizardPage(this, oldtool);
        addPage(envPage);
    }

    public void createTool(String endpoint, Person person) {
        System.out.println("Creating Tool");

        try {
            WorkflowTool tool = new WorkflowTool();
            tool.setCreator(person);
            infoPage.updateTool(tool);
            cmdPage.updateTool(tool);
            resourcePage.updateTool(tool);
            envPage.updateTool(tool);

            // save the tool
            tool = SpringData.getBean(WorkflowToolDAO.class).save(tool);

            // upload tool
            File zipfile = File.createTempFile("tool", ".zip");
            ImportExport.exportTool(zipfile, tool.getId());
            ToolCreator.postTool(zipfile, endpoint);
            zipfile.delete();
        } catch (Throwable thr) {
            logger.error("Could not save tool.", thr);
        }
    }
}
