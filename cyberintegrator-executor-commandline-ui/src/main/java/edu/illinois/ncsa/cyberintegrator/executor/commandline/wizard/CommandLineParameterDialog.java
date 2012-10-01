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

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter.ParameterType;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.Type;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class CommandLineParameterDialog extends Dialog {
    private CommandLineOption     option;
    private WorkflowToolParameter param;
    private Text                  txtFlag;
    private Text                  txtName;
    private Text                  txtDescription;
    private Combo                 cmbType;
    private Text                  txtValue;
    private Button                btnHidden;
    private Button                btnAllowNull;

    public CommandLineParameterDialog(Shell shell, CommandLineOption option, WorkflowToolParameter param) {
        super(shell);
        this.option = option;
        this.param = param;
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

        // parameter
        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Name : ");
        txtName = new Text(composite, SWT.BORDER | SWT.SINGLE);
        txtName.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtName.setText(param.getTitle());
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });

        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Description : ");
        txtDescription = new Text(composite, SWT.BORDER | SWT.SINGLE);
        txtDescription.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtDescription.setText(param.getDescription());

        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Type : ");
        cmbType = new Combo(composite, SWT.READ_ONLY);
        for (ParameterType pt : ParameterType.values()) {
            cmbType.add(pt.toString());
        }
        cmbType.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        cmbType.setText(param.getType().toString());
        cmbType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                validate();
            }
        });

        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Default value : ");
        txtValue = new Text(composite, SWT.BORDER | SWT.SINGLE);
        txtValue.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtValue.setText(param.getValue());

        btnHidden = new Button(composite, SWT.CHECK);
        btnHidden.setText("Hidden parameter?");
        btnHidden.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        btnHidden.setSelection(param.isHidden());

        btnAllowNull = new Button(composite, SWT.CHECK);
        btnAllowNull.setText("Can be empty?");
        btnAllowNull.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        btnAllowNull.setSelection(param.isAllowNull());

        applyDialogFont(composite);
        return composite;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("CommandLine Parameter");
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control c = super.createButtonBar(parent);
        validate();
        return c;
    }

    private void validate() {
        Button btn = getButton(IDialogConstants.OK_ID);
        if ((txtName.getText().trim().length() == 0) || (cmbType.getText().trim().length() == 0)) {
            btn.setEnabled(false);
        } else {
            btn.setEnabled(true);
        }
    }

    @Override
    protected void okPressed() {
        option.setType(Type.PARAMETER);
        option.setFlag(txtFlag.getText().trim());
        option.setOptionId(param.getParameterId());

        param.setTitle(txtName.getText().trim());
        param.setDescription(txtDescription.getText().trim());
        param.setType(ParameterType.valueOf(cmbType.getText().trim()));
        param.setValue(txtValue.getText().trim());
        param.setHidden(btnHidden.getSelection());
        param.setAllowNull(btnAllowNull.getSelection());
        super.okPressed();
    }
}
