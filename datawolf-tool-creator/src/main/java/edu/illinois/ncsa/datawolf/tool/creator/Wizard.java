package edu.illinois.ncsa.datawolf.tool.creator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Wizard extends JDialog {
    private static final long serialVersionUID = 1L;

    private JButton           btnNext;
    private JButton           btnPrev;
    private JButton           btnFinish;
    private JPanel            stepPane;
    private List<WizardPage>  pages            = new ArrayList<WizardPage>();
    private int               currentStep      = 0;
    private boolean           result           = false;

    public Wizard(JFrame parent, String name) {
        super(parent, name, true);

        // create and layout the interface
        createUI();
        pack();
    }

    public void addPage(WizardPage page) {
        pages.add(page);
    }

    public boolean open() {
        showCurrent();
        setVisible(true);
        return result;
    }

    protected void createUI() {
        JPanel panel = new JPanel(new BorderLayout());
        add(panel);

        // create pane that holds the steps of the wizard
        stepPane = new JPanel();
        stepPane.setPreferredSize(new Dimension(640, 480));
        stepPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        // panel.add(new JScrollPane(stepPane), BorderLayout.CENTER);
        panel.add(stepPane, BorderLayout.CENTER);

        // buttons that allow the user to jump to next and prev tab
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(buttons, BorderLayout.SOUTH);

        btnPrev = new JButton(new AbstractAction("Prev") {
            public void actionPerformed(ActionEvent e) {
                showPrevious();
            }
        });
        btnPrev.setEnabled(false);
        buttons.add(btnPrev);

        btnNext = new JButton(new AbstractAction("Next") {
            public void actionPerformed(ActionEvent e) {
                showNext();
            }
        });
        btnNext.setEnabled(false);
        buttons.add(btnNext);

        btnFinish = new JButton(new AbstractAction("Finish") {
            public void actionPerformed(ActionEvent e) {
                result = true;
                setVisible(false);
            }
        });
        btnFinish.setEnabled(false);
        buttons.add(btnFinish);
    }

    public List<WizardPage> getPages() {
        return pages;
    }

    private void showPrevious() {
        currentStep--;
        showCurrent();
    }

    private void showNext() {
        currentStep++;
        showCurrent();
    }

    public void showingPage(WizardPage page) {}

    private void showCurrent() {
        if (currentStep < 0) {
            currentStep = 0;
        }
        if (currentStep >= pages.size()) {
            currentStep = pages.size() - 1;
        }
        checkUI();
        WizardPage step = pages.get(currentStep);
        showingPage(step);
        stepPane.removeAll();
        stepPane.add(step);
        step.setPreferredSize(stepPane.getSize());
        stepPane.validate();
        stepPane.repaint();
    }

    public void checkUI() {
        if (pages.size() == 0) {
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            btnFinish.setEnabled(false);
            return;
        }
        if (currentStep == 0) {
            btnPrev.setEnabled(false);
        } else {
            btnPrev.setEnabled(true);
        }
        if (currentStep == (pages.size() - 1)) {
            btnNext.setEnabled(false);
            btnFinish.setEnabled(pages.get(currentStep).isStepComplete());
        } else {
            btnNext.setEnabled(pages.get(currentStep).isStepComplete());
            btnFinish.setEnabled(false);
        }
    }
}
