package edu.illinois.ncsa.cyberintegrator.tool.creator.common;

import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.tool.creator.Wizard;
import edu.illinois.ncsa.cyberintegrator.tool.creator.WizardPage;

public class InfoWizardPage extends WizardPage implements KeyListener {
    private JTextField txtName;
    private JTextArea  txtDescription;
    private JTextField txtVersion;

    public InfoWizardPage(Wizard wizard, WorkflowTool oldtool) {
        super(wizard);

        add(new JLabel("Name :"), 0, 0);
        txtName = new JTextField();
        txtName.addKeyListener(this);
        add(txtName, 1, 0, GridBagConstraints.HORIZONTAL);

        add(new JLabel("Version :"), 0, 1);
        txtVersion = new JTextField();
        txtVersion.addKeyListener(this);
        add(txtVersion, 1, 1, GridBagConstraints.HORIZONTAL);

        add(new JLabel("Description :"), 0, 2);
        txtDescription = new JTextArea();
        txtDescription.addKeyListener(this);
        add(txtDescription, 1, 2, GridBagConstraints.BOTH);

        checkUI();
    }

    private void checkUI() {
        setStepComplete(!txtName.getText().trim().equals(""));
    }

    public void updateTool(WorkflowTool tool) {
        tool.setTitle(txtName.getText().trim());
        tool.setVersion(txtVersion.getText().trim());
        tool.setDescription(txtDescription.getText().trim());
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        checkUI();
    }
}
