/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.executor.commandline.wizard;

import org.eclipse.ui.IWorkbenchWizard;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface ToolEditor extends IWorkbenchWizard {
    static public String EXT_PT = "edu.uiuc.ncsa.cyberintegrator.ui.toolEditor"; //$NON-NLS-1$

    public void setTool(WorkflowTool tool);
}