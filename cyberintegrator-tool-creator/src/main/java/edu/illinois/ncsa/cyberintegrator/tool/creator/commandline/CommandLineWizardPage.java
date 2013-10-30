/*******************************************************************************
 * University of Illinois/NCSA 
 * Open Source License
 * 
 * Copyright (c) 2006, 2008 NCSA/UIUC.  All rights reserved.
 * 
 *  Developed by:
 *   Image Spatial Data Analysis Group (ISDA Group)
 *   http://isda.ncsa.uiuc.edu/
 *
 *   Cyber-Environments and Technologies
 *   http://cet.ncsa.uiuc.edu/
 *
 *   National Center for Supercomputing Applications (NCSA)
 *   http://www.ncsa.uiuc.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of University of Illinois, NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.cyberintegrator.tool.creator.commandline;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineImplementation;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.cyberintegrator.tool.creator.Wizard;
import edu.illinois.ncsa.cyberintegrator.tool.creator.WizardPage;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class CommandLineWizardPage extends WizardPage {
    private static Logger                                       logger = LoggerFactory.getLogger(CommandLineWizardPage.class);

    private final Map<CommandLineOption, WorkflowToolParameter> parameters;
    private final Map<CommandLineOption, WorkflowToolData>      data;
    private JButton                                             del;
    private JButton                                             up;
    private JButton                                             down;
    private JTextField                                          exec;
    private JCheckBox                                           stdoutCheck;
    private JTextField                                          stdout;
    private JCheckBox                                           joinStdoutStderrCheck;
    private JCheckBox                                           stderrCheck;
    private JTextField                                          stderr;
    private JTextField                                          commandline;
    private OptionModel                                         model;
    private JList                                               lstCommandsLine;
    private final WorkflowTool                                  oldtool;
    private JButton                                             addData;
    private JButton                                             edit;
    private JButton                                             addValue;
    private JButton                                             addParam;

    protected CommandLineWizardPage(Wizard wizard, WorkflowTool oldtool) {
        super(wizard);
        setStepComplete(true);

        parameters = new HashMap<CommandLineOption, WorkflowToolParameter>();
        data = new HashMap<CommandLineOption, WorkflowToolData>();
        this.oldtool = oldtool;

        add(new JLabel("Executable:"), 0, 0);
        exec = new JTextField();
        exec.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        add(exec, 1, 0, GridBagConstraints.HORIZONTAL);

        JButton btn = new JButton("...");
        btn.setText("...");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(exec.getText()));
                if (chooser.showOpenDialog(CommandLineWizardPage.this) == JFileChooser.APPROVE_OPTION) {
                    exec.setText(chooser.getSelectedFile().getAbsolutePath());
                    checkUI();
                }
            }
        });
        add(btn, 2, 0);

        stdoutCheck = new JCheckBox("Capture stdout?");
        stdoutCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkUI();
            }
        });
        add(stdoutCheck, 0, 1);

        stdout = new JTextField("stdout");
        stdout.setEnabled(stdoutCheck.isSelected());
        stdout.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        add(stdout, 1, 1, GridBagConstraints.HORIZONTAL);

        joinStdoutStderrCheck = new JCheckBox("also stderr?");
        joinStdoutStderrCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkUI();
            }
        });
        add(joinStdoutStderrCheck, 2, 1);

        stderrCheck = new JCheckBox("Capture stderr?");
        stderrCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkUI();
            }
        });
        add(stderrCheck, 0, 2);

        stderr = new JTextField("stderr");
        stderr.setEnabled(stdoutCheck.isSelected());
        stderr.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                checkUI();
            }
        });
        add(stderr, 1, 2, GridBagConstraints.HORIZONTAL);
        add(new JLabel(), 2, 2);

        add(new JLabel("Options:"), 0, 3);

        model = new OptionModel();
        lstCommandsLine = new JList(model);
        lstCommandsLine.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                checkUI();
            }
        });
        lstCommandsLine.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    CommandLineOption option = (CommandLineOption) model.getOptionAt(index);
                    switch (option.getType()) {
                    case VALUE:
                        if (new CommandLineValueDialog(getWizard(), option).open()) {
                            model.update(option);
                            checkUI();
                        }
                        break;
                    case PARAMETER:
                        if (new CommandLineParameterDialog(getWizard(), option, parameters.get(option)).open()) {
                            model.update(option);
                            checkUI();
                        }
                        break;
                    case DATA:
                        if (new CommandLineDataDialog(getWizard(), option, data.get(option)).open()) {
                            model.update(option);
                            checkUI();
                        }
                        break;
                    }
                }
            }
        });
        add(lstCommandsLine, 1, 3, GridBagConstraints.BOTH);

        JPanel buttons = new JPanel(new GridLayout(0, 1));
        add(buttons, 2, 3);

        addValue = new JButton("Add Value");
        addValue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                CommandLineOption option = new CommandLineOption();
                CommandLineValueDialog dlg = new CommandLineValueDialog(getWizard(), option);
                if (dlg.open()) {
                    model.add(option);
                    checkUI();
                }
            }
        });
        buttons.add(addValue);

        addParam = new JButton("Add Parameter");
        addParam.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                CommandLineOption option = new CommandLineOption();
                WorkflowToolParameter param = new WorkflowToolParameter();
                CommandLineParameterDialog dlg = new CommandLineParameterDialog(getWizard(), option, param);
                if (dlg.open()) {
                    model.add(option);
                    parameters.put(option, param);
                    checkUI();
                }
            }
        });
        buttons.add(addParam);

        addData = new JButton("Add Data");
        addData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                CommandLineOption option = new CommandLineOption();
                WorkflowToolData data = new WorkflowToolData();
                CommandLineDataDialog dlg = new CommandLineDataDialog(getWizard(), option, data);
                if (dlg.open()) {
                    model.add(option);
                    CommandLineWizardPage.this.data.put(option, data);
                    checkUI();
                }
            }
        });
        buttons.add(addData);

        edit = new JButton("Edit");
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                CommandLineOption option = model.getOptionAt(lstCommandsLine.getSelectedIndex());
                switch (option.getType()) {
                case VALUE:
                    if (new CommandLineValueDialog(getWizard(), option).open()) {
                        model.update(option);
                        checkUI();
                    }
                    break;
                case PARAMETER:
                    if (new CommandLineParameterDialog(getWizard(), option, parameters.get(option)).open()) {
                        model.update(option);
                        checkUI();
                    }
                    break;
                case DATA:
                    if (new CommandLineDataDialog(getWizard(), option, data.get(option)).open()) {
                        model.update(option);
                        checkUI();
                    }
                    break;
                }
            }
        });
        buttons.add(edit);

        del = new JButton("Delete");
        del.setEnabled(false);
        del.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                model.remove(lstCommandsLine.getSelectedIndex());
                checkUI();
            }
        });
        buttons.add(del);

        up = new JButton("Up");
        up.setEnabled(false);
        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                model.up(lstCommandsLine.getSelectedIndex());
                checkUI();
            }
        });
        buttons.add(up);

        down = new JButton("Down");
        down.setEnabled(false);
        down.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                model.down(lstCommandsLine.getSelectedIndex());
                checkUI();
            }
        });
        buttons.add(down);

        commandline = new JTextField();
        commandline.setEditable(false);
        add(commandline, 0, 4, 3, 1, GridBagConstraints.HORIZONTAL);

        // set values from old tool
        if (oldtool != null) {
            Transaction transaction = null;
            try {
                transaction = SpringData.getTransaction();
                transaction.start();

                CommandLineImplementation impl = SpringData.JSONToObject(oldtool.getImplementation(), CommandLineImplementation.class);
                exec.setText(impl.getExecutable());
//          waitGUICheck.setSelection(impl.isGuiwait());
                if (impl.getCaptureStdOut() != null) {
                    stdoutCheck.setSelected(true);
//          oldtool.get
//          WorkflowToolDataBean wtdb = oldtool.getOutput(oldexternal.getCaptureStdout().getRefid());
//          stdout.setText(wtdb.getTitle());
                }

                joinStdoutStderrCheck.setSelected(impl.isJoinStdOutStdErr());

                if (impl.getCaptureStdErr() != null) {
                    stderrCheck.setSelected(true);
                }

//          ArrayList<OptionOrder> options = new ArrayList<OptionOrder>();
//          for (Cmdoption cmdopt : oldexternal.getOption()) {
//              for (Optionvalue opt : cmdopt.getValue()) {
//                  ToolOption to = new ToolOption();
//                  to.setType(ToolOptionType.VALUE);
//                  if (opt.getFlag() != null) {
//                      to.setFlag(opt.getFlag());
//                  }
//                  if (opt.getValue() != null) {
//                      to.setValue(opt.getValue());
//                  }
//                  options.add(new OptionOrder(opt.getOrder(), to));
//              }
//              for (Optionparameter opt : cmdopt.getParameter()) {
//                  ToolOption to = new ToolOption();
//                  to.setType(ToolOptionType.PARAMETER);
//                  if (opt.getFlag() != null) {
//                      to.setFlag(opt.getFlag());
//                  }
//                  to.setParameter(oldtool.getParameter(opt.getRefid()));
//                  options.add(new OptionOrder(opt.getOrder(), to));
//              }
//              for (Optiondata opt : cmdopt.getData()) {
//                  if (opt.getInput() != null) {
//                      ToolOption to = new ToolOption();
//                      to.setType(ToolOptionType.INPUT);
//                      if (opt.getFlag() != null) {
//                          to.setFlag(opt.getFlag());
//                      }
//                      ToolInput input = new ToolInput();
//                      input.setCommandLine(opt.isCommandline());
//                      input.setInput(oldtool.getInput(opt.getInput()));
//                      to.setInput(input);
//                      if (opt.getOutput() != null) {
//                          input.setAlsoOutput(true);
//                          input.setOutput(oldtool.getOutput(opt.getOutput()));
//                      }
//                      options.add(new OptionOrder(opt.getOrder(), to));
//                  } else {
//                      ToolOption to = new ToolOption();
//                      to.setType(ToolOptionType.OUTPUT);
//                      if (opt.getFlag() != null) {
//                          to.setFlag(opt.getFlag());
//                      }
//                      ToolOutput out = new ToolOutput();
//                      out.setCommandLine(opt.isCommandline());
//                      out.setOutput(oldtool.getOutput(opt.getOutput()));
//                      to.setOutput(out);
//                      options.add(new OptionOrder(opt.getOrder(), to));
//                  }
//              }

                transaction.commit();
            } catch (Exception exc) {
                logger.warn("Could nto get information from old tool.", exc);
            }
        }

        checkUI();
    }

    private String getOptionString(CommandLineOption option) {
        StringBuilder result = new StringBuilder();

        if ((option.getFlag() != null) && (option.getFlag().trim().length() > 0)) {
            result.append(option.getFlag());
            result.append(" ");
        }

        switch (option.getType()) {
        case VALUE:
            if ((option.getValue() != null) && (option.getValue().trim().length() > 0)) {
                result.append(option.getValue());
            }
            break;

        case PARAMETER:
            WorkflowToolParameter param = parameters.get(option);
            result.append(param.getTitle());
            result.append("[");
            result.append(param.getType().toString());
            result.append("] = ");
            result.append(param.getValue());
            break;

        case DATA:
            result.append("file(");
            switch (option.getInputOutput()) {
            case INPUT:
                result.append("in:");
                break;
            case OUTPUT:
                result.append("out:");
                break;
            case BOTH:
                result.append("in/out:");
                break;
            }
            if ((option.getFilename() != null) && (option.getFilename().trim().length() > 0)) {
                result.append(option.getFilename());
            } else {
                result.append("AUTO");
            }
            if (option.isCommandline()) {
                result.append("[not passed]");
            }
            result.append(")");
            break;
        }

        return result.toString();
    }

    private void checkUI() {
        int index = lstCommandsLine.getSelectedIndex();
        int size = lstCommandsLine.getModel().getSize();

        StringBuffer sb = new StringBuffer();
        sb.append(exec.getText());
        sb.append(" "); //$NON-NLS-1$
        for (CommandLineOption option : model.getList()) {
            sb.append(getOptionString(option));
        }

        if (joinStdoutStderrCheck.isSelected()) {
            sb.append(" 2>&1"); //$NON-NLS-1$
        } else if (stderrCheck.isSelected()) {
            sb.append(" 2>"); //$NON-NLS-1$
            sb.append(stderr.getText());
        }

        if (stdoutCheck.isSelected()) {
            sb.append(" >"); //$NON-NLS-1$
            sb.append(stdout.getText());
        }

        commandline.setText(sb.toString());

        del.setEnabled(index >= 0);
        up.setEnabled(size > 1 && index > 0);
        down.setEnabled(size > 1 && index >= 0 && index < size - 1);

        stdout.setEnabled(stdoutCheck.isSelected());
        stderrCheck.setEnabled(!joinStdoutStderrCheck.isSelected());
        stderr.setEnabled(!joinStdoutStderrCheck.isSelected() && stderrCheck.isSelected());

        if (exec.getText().trim().length() == 0) {
            setStepComplete(false);
            return;
        }

        if (stdoutCheck.isSelected() && (stdout.getText().trim().length() == 0)) {
            setStepComplete(false);
            return;
        }

        if (stderrCheck.isSelected() && (stderr.getText().trim().length() == 0)) {
            setStepComplete(false);
            return;
        }

        setStepComplete(true);
    }

    public void updateTool(WorkflowTool tool) throws IOException {
        CommandLineImplementation impl = new CommandLineImplementation();

        impl.setExecutable(exec.getText());

        if (stdoutCheck.isSelected()) {
            WorkflowToolData data = new WorkflowToolData();
            data.setTitle(stdout.getText());
            data.setDescription("stdout of external tool.");
            data.setMimeType("text/plain"); //$NON-NLS-1$
            data.setDataId("stdout");
            tool.addOutput(data);

            impl.setCaptureStdOut(data.getDataId());
            if (joinStdoutStderrCheck.isSelected()) {
                impl.setJoinStdOutStdErr(true);
            }
        }

        if (!joinStdoutStderrCheck.isSelected() && stderrCheck.isSelected()) {
            WorkflowToolData data = new WorkflowToolData();
            data.setTitle(stderr.getText());
            data.setDescription("stderr of external tool.");
            data.setMimeType("text/plain"); //$NON-NLS-1$
            data.setDataId("stderr");
            tool.addOutput(data);

            impl.setCaptureStdErr(data.getDataId());
        }

        for (CommandLineOption option : model.getList()) {
            impl.getCommandLineOptions().add(option);
            switch (option.getType()) {
            case VALUE:
                break;

            case PARAMETER:
                tool.addParameter(parameters.get(option));
                break;

            case DATA:
                switch (option.getInputOutput()) {
                case INPUT:
                    tool.addInput(data.get(option));
                    break;
                case OUTPUT:
                    tool.addOutput(data.get(option));
                    break;
                case BOTH:
                    tool.addInput(data.get(option));
                    tool.addOutput(data.get(option));
                    break;
                }
                break;
            }
        }

        tool.setImplementation(SpringData.objectToJSON(impl));
    }

    class OptionModel extends AbstractListModel {
        private final List<CommandLineOption> options = new ArrayList<CommandLineOption>();

        public List<CommandLineOption> getList() {
            return options;
        }

        public void remove(CommandLineOption option) {
            int index = options.indexOf(option);
            options.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public void remove(int index) {
            options.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public void add(CommandLineOption option) {
            int size = options.size();
            options.add(option);
            fireIntervalAdded(this, size + 1, size + 1);
        }

        public void update(CommandLineOption option) {
            int index = options.indexOf(option);
            fireContentsChanged(this, index, index);
        }

        public void up(int idx) {
            if (idx > 0) {
                CommandLineOption a = options.get(idx);
                options.set(idx, options.get(idx - 1));
                options.set(idx - 1, a);
                fireContentsChanged(this, idx - 1, idx);
            }
        }

        public void down(int idx) {
            if (idx < options.size() - 1) {
                CommandLineOption a = options.get(idx);
                options.set(idx, options.get(idx + 1));
                options.set(idx + 1, a);
                fireContentsChanged(this, idx, idx + 1);
            }
        }

        public CommandLineOption getOptionAt(int index) {
            return options.get(index);
        }

        @Override
        public Object getElementAt(int arg0) {
            return getOptionString(options.get(arg0));
        }

        @Override
        public int getSize() {
            return options.size();
        }
    }
}