package edu.illinois.ncsa.datawolf.executor.hpc.wizard;

import java.util.Date;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.event.WorkflowToolChangedEvent;
import edu.illinois.ncsa.datawolf.domain.event.WorkflowToolCreatedEvent;
import edu.illinois.ncsa.datawolf.executor.hpc.HPCExecutor;
import edu.illinois.ncsa.datawolf.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class HPCToolWizard extends Wizard implements INewWizard, ToolEditor {
    private static Logger      logger = LoggerFactory.getLogger(HPCToolWizard.class);
    private WorkflowTool       oldTool;
    private ToolInfoWizardPage infopage;
    private HPCToolWizardPage  hpcToolWizardPage;

    @Override
    public void addPages() {
        infopage = new ToolInfoWizardPage("Basic information about a tool", oldTool);
        addPage(infopage);

        hpcToolWizardPage = new HPCToolWizardPage("Select resources for tool", oldTool);
        addPage(hpcToolWizardPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("HPC Tool Wizard");
        setNeedsProgressMonitor(true);
    }

    @Override
    public boolean performFinish() {
        Transaction transaction = null;
        try {
            transaction = SpringData.getTransaction();
            transaction.start();

            Person creator = null;

            // create the tool
            WorkflowTool tool = new WorkflowTool();
            tool.setDate(new Date());
            tool.setCreator(creator);
            tool.setExecutor(HPCExecutor.EXECUTOR_NAME);

            infopage.updateTool(tool);
            hpcToolWizardPage.updateTool(tool);

            // save the tool
            SpringData.getBean(WorkflowToolDAO.class).save(tool);
            // fire the events.
            if (oldTool != null) {
                SpringData.getEventBus().fireEvent(new WorkflowToolChangedEvent(oldTool));
            } else {
                SpringData.getEventBus().fireEvent(new WorkflowToolCreatedEvent(tool));
            }

        } catch (Exception exc) {
            logger.warn("Could not save tool.", exc);
            return false;
        } finally {
            try {
                transaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public void setTool(WorkflowTool tool) {
        this.oldTool = tool;
    }

}
