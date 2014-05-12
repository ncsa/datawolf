package edu.illinois.ncsa.datawolf.executor.hpc.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;

public class ToolInfoWizardPage extends WizardPage {
    private static final String VERSION_1 = "1.0"; //$NON-NLS-1$

    private Text                name;
    private Text                description;
    private final WorkflowTool  oldtool;
    private Text                version;

    public ToolInfoWizardPage(String pageName, WorkflowTool oldtool) {
        super(pageName);
        this.oldtool = oldtool;
        setTitle(pageName);
        if (oldtool == null) {
            setMessage("Creates a new tool, all fields are required.");
        } else {
            setMessage("Edit an existing tool, all fields are required.");
        }
    }

    public void createControl(Composite parent) {
        final Composite top = new Composite(parent, SWT.NONE);
        top.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        top.setLayout(new GridLayout(3, false));

        Label lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("Name:");

        name = new Text(top, SWT.SINGLE | SWT.BORDER);
        name.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        name.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });
        if (oldtool != null) {
            name.setText(oldtool.getTitle());
        }

        lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("Version:");

        version = new Text(top, SWT.SINGLE | SWT.BORDER);
        version.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        if (oldtool != null) {
            version.setText(getNextVersion(oldtool, true));
        } else {
            version.setText(VERSION_1);
        }
        lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("Description:");

        description = new Text(top, SWT.MULTI | SWT.BORDER | SWT.WRAP);
        description.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
        description.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });
        if (oldtool != null) {
            description.setText(oldtool.getDescription());
        }

        validate();
        setControl(top);
    }

    private void validate() {
        if (name.getText().trim().length() == 0) {
            setPageComplete(false);
            return;
        }

        if (version.getText().trim().length() == 0) {
            setPageComplete(false);
            return;
        }

        setPageComplete(true);
    }

    public void updateTool(WorkflowTool tool) {
        tool.setTitle(name.getText().trim());
        tool.setDescription(description.getText().trim());
        tool.setVersion(version.getText().trim());
    }

    /**
     * Given a tool return the next version for that tool. This will find the
     * latest tool (if findLatest is true) in the chain, and retrieve the
     * version number for this tool. Next it will add 1 to the version.
     * 
     * @param tool
     *            the tool of which to find the tool version
     * @param findLatest
     *            search for the latest tool in chain.
     * @return the next version to be used for a new tool
     */
    public String getNextVersion(WorkflowTool tool, boolean findLatest) {
        if (tool == null) {
            return VERSION_1;
        }

        // find the latest tool
        WorkflowTool latest = tool;
//        if (findLatest) {
//            latest = getLatestTool(tool);
//        }

        // find the version
        String version = latest.getVersion();
        if ((version == null) || version.equals("")) { //$NON-NLS-1$
            return VERSION_1;
        }

        // update the number
        int idx = version.lastIndexOf("."); //$NON-NLS-1$
        if (idx == -1) {
            try {
                int x = Integer.parseInt(version);
                x++;
                version = Integer.toString(x);
            } catch (NumberFormatException e) {
                version += ".1"; //$NON-NLS-1$
            }
        } else {
            try {
                int x = Integer.parseInt(version.substring(idx + 1));
                x++;
                version = version.substring(0, idx + 1) + Integer.toString(x);
            } catch (NumberFormatException e) {
                version += ".1"; //$NON-NLS-1$
            }
        }

        return version;
    }

}
