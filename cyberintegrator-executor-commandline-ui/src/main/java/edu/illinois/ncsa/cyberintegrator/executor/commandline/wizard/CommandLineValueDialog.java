/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.executor.commandline.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.Type;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class CommandLineValueDialog extends Dialog {
    private CommandLineOption option;
    private Text              txtFlag;
    private Text              txtValue;

    public CommandLineValueDialog(Shell shell, CommandLineOption option) {
        super(shell);
        this.option = option;
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
        txtFlag.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });

        lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lbl.setText("Value : ");
        txtValue = new Text(composite, SWT.BORDER | SWT.SINGLE);
        if (option.getValue() != null) {
            txtValue.setText(option.getValue());
        }
        txtValue.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtValue.addKeyListener(new KeyAdapter() {
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
        newShell.setText("CommandLine Value");
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control c = super.createButtonBar(parent);
        validate();
        return c;
    }

    private void validate() {
        Button btn = getButton(IDialogConstants.OK_ID);

        if ((txtValue.getText().trim().length() == 0) && (txtFlag.getText().trim().length() == 0)) {
            btn.setEnabled(false);
        } else {
            btn.setEnabled(true);
        }
    }

    @Override
    protected void okPressed() {
        option.setType(Type.VALUE);
        option.setFlag(txtFlag.getText().trim());
        option.setValue(txtValue.getText().trim());

        super.okPressed();
    }
}
