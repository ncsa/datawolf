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

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.InputOutput;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.Type;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class CommandLineDataDialog extends JDialog {
    private CommandLineOption option;
    private WorkflowToolData  data;
    private JTextField        txtFlag;
    private JTextField        txtName;
    private JTextField        txtDescription;
    private JTextField        txtFilename;
    private JComboBox         cmbInputOutput;
    private JTextField        txtMimeType;
    private JCheckBox         chkCommandline;
    private JButton           btnOk;
    private boolean           result = false;

    public CommandLineDataDialog(JDialog parent, CommandLineOption option, WorkflowToolData data) {
        super(parent, "CommandLine Input/Output", true);
        this.option = option;
        this.data = data;

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

        // data
        centerPane.add(new JLabel("Input/Output : "));
        cmbInputOutput = new JComboBox(InputOutput.values());
        cmbInputOutput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkUI();
            }
        });
        if (option.getInputOutput() != null) {
            cmbInputOutput.setSelectedItem(option.getInputOutput());
        } else {
            cmbInputOutput.setSelectedItem(InputOutput.INPUT);
        }
        centerPane.add(cmbInputOutput);

        centerPane.add(new JLabel("Name:"));
        txtName = new JTextField();
        txtName.setText(data.getTitle());
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        centerPane.add(txtName);

        centerPane.add(new JLabel("Description:"));
        txtDescription = new JTextField();
        txtDescription.setText(data.getDescription());
        txtDescription.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        centerPane.add(txtDescription);

        centerPane.add(new JLabel("Content Type:"));
        txtMimeType = new JTextField();
        txtMimeType.setText(data.getMimeType());
        txtMimeType.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        centerPane.add(txtMimeType);

        centerPane.add(new JLabel("Commandline :"));
        chkCommandline = new JCheckBox();
        chkCommandline.setSelected(option.isCommandline());
        chkCommandline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkUI();
            }
        });
        centerPane.add(chkCommandline);

        centerPane.add(new JLabel("Filename:"));
        txtFilename = new JTextField();
        if (option.getFilename() != null) {
            txtFilename.setText(option.getFilename());
        }
        txtFilename.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        centerPane.add(txtFilename);

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
        if ((txtName.getText().trim().length() == 0) || (txtMimeType.getText().trim().length() == 0)) {
            btnOk.setEnabled(false);
        }
        if (chkCommandline.isSelected() && (txtFilename.getText().trim().length() == 0)) {
            btnOk.setEnabled(false);
        }
        btnOk.setEnabled(true);
    }

    protected void okPressed() {
        option.setType(Type.DATA);
        option.setFlag(txtFlag.getText().trim());
        option.setInputOutput(InputOutput.valueOf(cmbInputOutput.getSelectedItem().toString()));
        option.setFilename(txtFilename.getText().trim());
        option.setCommandline(chkCommandline.isSelected());
        option.setOptionId(data.getDataId());

        data.setTitle(txtName.getText().trim());
        data.setDescription(txtDescription.getText().trim());
        data.setMimeType(txtMimeType.getText().trim());

        setVisible(false);
        result = true;
    }
}
