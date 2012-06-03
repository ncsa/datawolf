/*******************************************************************************
 * University of Illinois/NCSA 
 * Open Source License
 * 
 * Copyright (c) 2006, 2008 NCSA/UIUC.  All rights reserved.
 * 
 *  Developed by:
 *   Image Spatial Data Analysis Group (ISDA Group)
 *   http://isda.ncsa.uiuc.edu/
 *
 *   Cyber-Environments and Technologies
 *   http://cet.ncsa.uiuc.edu/
 *
 *   National Center for Supercomputing Applications (NCSA)
 *   http://www.ncsa.uiuc.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of University of Illinois, NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.cyberintegrator.executor.commandline.wizard;

import java.util.Date;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.event.WorkflowToolChangedEvent;
import edu.illinois.ncsa.cyberintegrator.domain.event.WorkflowToolCreatedEvent;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineExecutor;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class CommandLineWizard extends Wizard implements INewWizard, ToolEditor {
    private static Logger          logger = LoggerFactory.getLogger(CommandLineWizard.class);

    private WorkflowTool           oldtool;

    private ToolResourceWizardPage resourcepage;

    private ToolInfoWizardPage     infopage;
    private CommandLineWizardPage  commandlinepage;
    private EnvWizardPage          envpage;

    public CommandLineWizard() {}

    public void setTool(WorkflowTool oldtool) {
        this.oldtool = oldtool;
    }

    @Override
    public void addPages() {
        infopage = new ToolInfoWizardPage("Basic information about a tool", oldtool);
        addPage(infopage);

        commandlinepage = new CommandLineWizardPage("Setup command line tool", oldtool);
        addPage(commandlinepage);

        resourcepage = new ToolResourceWizardPage("Select resources for tool", oldtool);
        addPage(resourcepage);

        envpage = new EnvWizardPage("Add environment variables", oldtool);
        addPage(envpage);
    }

    @Override
    public boolean performFinish() {
        Transaction transaction = null;
        try {
            transaction = SpringData.getTransaction();
            transaction.start();

            // get the user
            Person creator = null;// SpringData.getBean(PersonDAO.class).findOne(CyberintegratorActivator.currentUserId);

            // create the tool
            WorkflowTool tool = new WorkflowTool();
            tool.setDate(new Date());
            tool.setCreator(creator);
            tool.setExecutor(CommandLineExecutor.EXECUTOR_NAME);

            // add list of all people working on this tool
            if (oldtool != null) {
                tool.setPreviousVersion(oldtool);

                if (!oldtool.getCreator().equals(creator)) {
                    tool.addContributor(oldtool.getCreator());
                }

                for (Person person : oldtool.getContributors()) {
                    if (!person.equals(creator)) {
                        tool.addContributor(person);
                    }
                }
            }

            // add basic information
            infopage.updateTool(tool);

            // add command line page
            commandlinepage.updateTool(tool);

            // add environment variables
            envpage.updateTool(tool);

            // add resources
            resourcepage.updateTool(tool);

            // save the tool
            SpringData.getBean(WorkflowToolDAO.class).save(tool);

            // fire the events.
            if (oldtool != null) {
                SpringData.getEventBus().fireEvent(new WorkflowToolChangedEvent(oldtool));
            } else {
                SpringData.getEventBus().fireEvent(new WorkflowToolCreatedEvent(tool));
            }

            transaction.commit();

        } catch (Exception exc) {
            logger.warn("Could not save tool.", exc);
            return false;
        }

        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("Command Line Wizard");
        setNeedsProgressMonitor(true);
    }
}