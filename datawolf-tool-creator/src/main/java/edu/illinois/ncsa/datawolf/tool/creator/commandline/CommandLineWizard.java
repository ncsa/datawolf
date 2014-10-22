package edu.illinois.ncsa.datawolf.tool.creator.commandline;

import java.io.File;
import java.util.Date;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.ImportExport;
import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineExecutor;
import edu.illinois.ncsa.datawolf.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.datawolf.tool.creator.ToolCreator;
import edu.illinois.ncsa.datawolf.tool.creator.Wizard;
import edu.illinois.ncsa.datawolf.tool.creator.common.InfoWizardPage;
import edu.illinois.ncsa.datawolf.tool.creator.common.ToolResourceWizardPage;
import edu.illinois.ncsa.datawolf.tool.creator.java.JavaWizard;
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
            // create the tool
            WorkflowTool tool = new WorkflowTool();
            tool.setDate(new Date());
            tool.setCreator(person);
            tool.setExecutor(CommandLineExecutor.EXECUTOR_NAME);

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
