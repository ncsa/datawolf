/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.tool.creator.commandline;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter.ParameterType;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.Type;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class CommandLineParameterDialog extends JDialog {
    private CommandLineOption     option;
    private WorkflowToolParameter param;
    private JTextField            txtFlag;
    private JTextField            txtName;
    private JTextField            txtDescription;
    private JComboBox             cmbType;
    private JTextField            txtValue;
    private JCheckBox             chkHidden;
    private JCheckBox             chkAllowNull;
    private JButton               btnOk;
    private boolean               result = false;

    public CommandLineParameterDialog(JDialog parent, CommandLineOption option, WorkflowToolParameter param) {
        super(parent, "CommandLine Parameter", true);
        this.option = option;
        this.param = param;

        JPanel panel = new JPanel(new BorderLayout());
        add(panel);
        JPanel centerPane = new JPanel(new GridLayout(0, 2));
        panel.add(centerPane, BorderLayout.CENTER);

        // flag
        centerPane.add(new JLabel("Flag : "));
        txtFlag = new JTextField();
        if (option.getFlag() != null) {
            txtFlag.setText(option.getFlag());
        }
        centerPane.add(txtFlag);

        // parameter
        centerPane.add(new JLabel("Name:"));
        txtName = new JTextField();
        txtName.setText(param.getTitle());
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        centerPane.add(txtName);

        centerPane.add(new JLabel("Description:"));
        txtDescription = new JTextField();
        txtDescription.setText(param.getDescription());
        txtDescription.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        centerPane.add(txtDescription);

        centerPane.add(new JLabel("Type : "));
        cmbType = new JComboBox(ParameterType.values());
        cmbType.setSelectedItem(param.getType());
        cmbType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkUI();
            }
        });
        centerPane.add(cmbType);

        centerPane.add(new JLabel("Default value : "));
        txtValue = new JTextField();
        txtValue.setText(param.getValue());
        txtValue.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        centerPane.add(txtValue);

        centerPane.add(new JLabel("Hidden parameter?"));
        chkHidden = new JCheckBox();
        chkHidden.setSelected(param.isHidden());
        chkHidden.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkUI();
            }
        });
        centerPane.add(chkHidden);

        centerPane.add(new JLabel("Can be empty?"));
        chkAllowNull = new JCheckBox();
        chkAllowNull.setSelected(param.isAllowNull());
        chkAllowNull.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkUI();
            }
        });
        centerPane.add(chkAllowNull);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(buttons, BorderLayout.SOUTH);
        btnOk = new JButton("OK");
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                okPressed();
            }
        });
        buttons.add(btnOk);
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });
        buttons.add(btnCancel);

        checkUI();
    }

    public boolean open() {
        pack();
        setVisible(true);
        return result;
    }

    private void checkUI() {
        if (btnOk == null) {
            return;
        }
        if ((txtName.getText().trim().length() == 0)) {
            btnOk.setEnabled(false);
        } else {
            btnOk.setEnabled(true);
        }
    }

    protected void okPressed() {
        option.setType(Type.PARAMETER);
        option.setFlag(txtFlag.getText().trim());
        option.setOptionId(param.getParameterId());

        param.setTitle(txtName.getText().trim());
        param.setDescription(txtDescription.getText().trim());
        param.setType((ParameterType) cmbType.getSelectedItem());
        param.setValue(txtValue.getText().trim());
        param.setHidden(chkHidden.isSelected());
        param.setAllowNull(chkAllowNull.isSelected());

        setVisible(false);
        result = true;
    }
}
