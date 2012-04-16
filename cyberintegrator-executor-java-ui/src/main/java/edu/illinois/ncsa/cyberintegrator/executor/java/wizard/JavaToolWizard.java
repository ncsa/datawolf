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
package edu.illinois.ncsa.cyberintegrator.executor.java.wizard;

import java.io.FileDescriptor;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class JavaToolWizard extends Wizard implements INewWizard, ToolEditor {
    static private Logger          logger = LoggerFactory.getLogger(JavaToolWizard.class);

    private WorkflowTool           oldtool;
    private ToolResourceWizardPage resourcepage;
    private ToolSelectWizardPage   toolselectpage;
    private Set<FileDescriptor>    descriptors;

    public JavaToolWizard() {}

    public void setTool(WorkflowTool oldtool) {
        this.oldtool = oldtool;
    }

    @Override
    public void addPages() {
        resourcepage = new ToolResourceWizardPage("Select resources for tool", oldtool);
        resourcepage.setMinimumFiles(1);
        addPage(resourcepage);

        toolselectpage = new ToolSelectWizardPage("Select tool", oldtool);
        addPage(toolselectpage);
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page.equals(resourcepage)) {
            toolselectpage.setToolPath(resourcepage.getFileDescriptors());
        }
        return super.getNextPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            // create the tool(s)
            Set<WorkflowTool> tools = toolselectpage.getTools();

            // add the resources.
            for (WorkflowTool tool : tools) {
                resourcepage.updateTool(tool);
            }

            // save the newly created tools
            WorkflowToolDAO dao = SpringData.getBean(WorkflowToolDAO.class);
            dao.save(tools);

            // fire the events.
            if (oldtool != null) {
                // TODO RK : add events
//                Workflow.fireChangeTool(oldtool);
            }
            for (WorkflowTool tool : tools) {
                // TODO RK : add events
//                Workflow.fireAddTool(tool);
            }

        } catch (Exception exc) {
            logger.warn("Could not create tool(s)", exc);
            return false;
        }

        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("Cyberintegrator tool wizard");
        setNeedsProgressMonitor(true);
    }
}