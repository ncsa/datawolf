package edu.illinois.ncsa.cyberintegrator.tool.creator;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

/**
 * All steps in the wizard should extend this class. This class is
 * the framework for each step of the wizard.
 * 
 * @author Rob Kooper
 * 
 */
public abstract class WizardPage extends JPanel {
    private static final long serialVersionUID = 1L;

    private boolean           stepComplete     = false;

    private Wizard            wizard;

    public WizardPage(Wizard wizard) {
        this.wizard = wizard;
        setLayout(new GridBagLayout());
    }

    public void checkWizardUI() {
        wizard.checkUI();
    }

    public Wizard getWizard() {
        return wizard;
    }

    public void setStepComplete(boolean stepComplete) {
        this.stepComplete = stepComplete;
        checkWizardUI();
    }

    public boolean isStepComplete() {
        return stepComplete;
    }

    public void add(Component component, int x, int y) {
        add(component, x, y, 1, 1, GridBagConstraints.NONE);
    }

    public void add(Component component, int x, int y, int fill) {
        add(component, x, y, 1, 1, fill);
    }

    public void add(Component component, int x, int y, int wx, int wy, int fill) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;

        switch (fill) {
        case GridBagConstraints.NONE:
            gbc.weightx = 0;
            gbc.weighty = 0;
            break;
        case GridBagConstraints.HORIZONTAL:
            gbc.weightx = 1;
            gbc.weighty = 0;
            break;
        case GridBagConstraints.VERTICAL:
            gbc.weightx = 0;
            gbc.weighty = 1;
            break;
        case GridBagConstraints.BOTH:
            gbc.weightx = 1;
            gbc.weighty = 1;
            break;
        }
        gbc.gridwidth = wx;
        gbc.gridheight = wy;
        gbc.fill = fill;
        add(component, gbc);
    }
}
