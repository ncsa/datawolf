/**
 * 
 */
package edu.illinois.ncsa.datawolf.tool.creator.commandline;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class CommandLineValueDialog extends JDialog {
    private CommandLineOption option;
    private JTextField        txtFlag;
    private JTextField        txtValue;
    private JButton           btnOk;
    private boolean           result = false;

    public CommandLineValueDialog(JDialog parent, CommandLineOption option) {
        super(parent, "CommandLine Value", true);
        this.option = option;

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
        txtFlag.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        centerPane.add(txtFlag);

        centerPane.add(new JLabel("Value : "));
        txtValue = new JTextField();
        if (option.getValue() != null) {
            txtValue.setText(option.getValue());
        }
        txtValue.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        centerPane.add(txtValue);

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
        if ((txtValue.getText().trim().length() == 0) && (txtFlag.getText().trim().length() == 0)) {
            btnOk.setEnabled(false);
        } else {
            btnOk.setEnabled(true);
        }
    }

    protected void okPressed() {
        option.setType(edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption.Type.VALUE);
        option.setFlag(txtFlag.getText().trim());
        option.setValue(txtValue.getText().trim());

        setVisible(false);
        result = true;
    }
}
