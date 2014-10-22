/**
 * 
 */
package edu.illinois.ncsa.datawolf.executor.java.wizard;

import org.eclipse.ui.IWorkbenchWizard;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public interface ToolEditor extends IWorkbenchWizard {
    static public String EXT_PT = "edu.uiuc.ncsa.datawolf.ui.toolEditor"; //$NON-NLS-1$

    public void setTool(WorkflowTool tool);
}
