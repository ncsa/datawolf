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
package edu.illinois.ncsa.cyberintegrator.executor.java.swing;

import java.util.Set;

import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.event.WorkflowToolChangedEvent;
import edu.illinois.ncsa.cyberintegrator.domain.event.WorkflowToolCreatedEvent;
import edu.illinois.ncsa.cyberintegrator.gui.ExecutorWizard;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowToolDAO;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class JavaToolWizard implements ExecutorWizard {
    static private Logger logger = LoggerFactory.getLogger(JavaToolWizard.class);

    public JavaToolWizard() {}

    @Override
    public String getTitle() {
        return "JAVA Tool";
    }

    @Override
    public void showWizard(WorkflowTool oldtool) {
        final JavaWizardModel model = new JavaWizardModel(oldtool);
        Wizard wizard = new Wizard(model);
        wizard.addWizardListener(new WizardListener() {
            @Override
            public void wizardClosed(WizardEvent e) {
                performFinish(model);
            }

            @Override
            public void wizardCancelled(WizardEvent e) {}
        });
        wizard.showInFrame(getTitle());
    }

    public boolean performFinish(JavaWizardModel model) {
        try {
            // create the tool(s)
            Set<WorkflowTool> tools = model.getToolSelectPage().getTools();

            // add the resources.
            for (WorkflowTool tool : tools) {
                model.getResourcePage().updateTool(tool);
            }

            // save the newly created tools
            WorkflowToolDAO dao = SpringData.getBean(WorkflowToolDAO.class);
            dao.save(tools);

            // fire the events.
            if (model.getOldTool() != null) {
                SpringData.getEventBus().fireEvent(new WorkflowToolChangedEvent(model.getOldTool()));
            }
            for (WorkflowTool tool : tools) {
                SpringData.getEventBus().fireEvent(new WorkflowToolCreatedEvent(tool));
            }

        } catch (Exception exc) {
            logger.warn("Could not create tool(s)", exc);
            return false;
        }

        return true;
    }
}