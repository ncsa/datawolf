/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.executor.commandline.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.InputOutput;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.Type;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class CommandLineDataDialog extends Dialog {
    private CommandLineOption option;
    private WorkflowToolData  data;
    private Text              txtFlag;
    private Text              txtName;
    private Text              txtDescription;
    private Text              txtFilename;
    private Combo             cmbInputOutput;
    private Text              txtMimeType;
    private Button            chkCommandline;

    public CommandLineDataDialog(Shell shell, CommandLineOption option, WorkflowToolData data) {
        super(shell);
        this.option = option;
        this.data = data;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        ((GridLayout) composite.getLayout()).numColumns = 2;
        GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
        gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        composite.setLayoutData(gd);

        // flag
        Label lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Flag : ");
        txtFlag = new Text(composite, SWT.BORDER | SWT.SINGLE);
        if (option.getFlag() != null) {
            txtFlag.setText(option.getFlag());
        }
        txtFlag.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

        // data
        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Input/Output : ");
        cmbInputOutput = new Combo(composite, SWT.READ_ONLY);
        for (InputOutput io : InputOutput.values()) {
            cmbInputOutput.add(io.toString());
        }
        cmbInputOutput.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        if (option.getInputOutput() != null) {
            cmbInputOutput.setText(option.getInputOutput().toString());
        } else {
            cmbInputOutput.setText(InputOutput.INPUT.toString());
        }

        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Name:");
        txtName = new Text(composite, SWT.BORDER | SWT.SINGLE);
        txtName.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtName.setText(data.getTitle());
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });

        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Description:");
        txtDescription = new Text(composite, SWT.BORDER | SWT.SINGLE);
        txtDescription.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtDescription.setText(data.getDescription());

        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Content Type:");
        txtMimeType = new Text(composite, SWT.BORDER | SWT.SINGLE);
        txtMimeType.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtMimeType.setText(data.getMimeType());
        txtMimeType.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });

        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Commandline :");
        chkCommandline = new Button(composite, SWT.BORDER | SWT.CHECK);
        chkCommandline.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        chkCommandline.setSelection(option.isCommandline());
        chkCommandline.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                validate();
            }
        });

        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Filename:");
        txtFilename = new Text(composite, SWT.BORDER | SWT.SINGLE);
        txtFilename.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtFilename.setText(option.getFilename());
        txtFilename.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });

        applyDialogFont(composite);
        return composite;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("CommandLine Input/Output");
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control c = super.createButtonBar(parent);
        validate();
        return c;
    }

    private void validate() {
        Button btn = getButton(IDialogConstants.OK_ID);
        if ((txtName.getText().trim().length() == 0) || (txtMimeType.getText().trim().length() == 0)) {
            btn.setEnabled(false);
        }
        if (chkCommandline.getSelection() && (txtFilename.getText().trim().length() == 0)) {
            btn.setEnabled(false);
        }
        btn.setEnabled(true);
    }

    @Override
    protected void okPressed() {
        option.setType(Type.DATA);
        option.setFlag(txtFlag.getText().trim());
        option.setInputOutput(InputOutput.valueOf(cmbInputOutput.getText()));
        option.setFilename(txtFilename.getText().trim());
        option.setCommandline(chkCommandline.getSelection());

        data.setTitle(txtName.getText().trim());
        data.setDescription(txtDescription.getText().trim());
        data.setMimeType(txtMimeType.getText().trim());

        super.okPressed();
    }
}
