package edu.illinois.ncsa.cyberintegrator.gui;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;

public interface ExecutorWizard {
    String getTitle();

    void showWizard(WorkflowTool oldtool);
}
