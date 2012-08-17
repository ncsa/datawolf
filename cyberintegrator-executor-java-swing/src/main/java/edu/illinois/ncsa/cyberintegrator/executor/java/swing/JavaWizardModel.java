package edu.illinois.ncsa.cyberintegrator.executor.java.swing;

import java.util.List;

import org.pietschy.wizard.models.StaticModel;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.domain.FileDescriptor;

public class JavaWizardModel extends StaticModel {
    private WorkflowTool           oldtool;
    private ToolResourceWizardPage resourcepage;
    private ToolSelectWizardPage   toolselectpage;

    public JavaWizardModel(WorkflowTool oldtool) {
        resourcepage = new ToolResourceWizardPage();
        toolselectpage = new ToolSelectWizardPage();

        add(resourcepage);
        add(toolselectpage);
        setLastVisible(true);
    }

    public WorkflowTool getOldTool() {
        return oldtool;
    }

    public ToolResourceWizardPage getResourcePage() {
        return resourcepage;
    }

    public ToolSelectWizardPage getToolSelectPage() {
        return toolselectpage;
    }

    public List<FileDescriptor> getFileDescriptors() {
        return resourcepage.getFileDescriptors();
    }
}
