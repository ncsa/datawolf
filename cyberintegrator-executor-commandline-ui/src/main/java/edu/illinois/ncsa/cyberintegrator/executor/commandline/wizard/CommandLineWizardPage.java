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
package edu.illinois.ncsa.cyberintegrator.executor.commandline.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineImplementation;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class CommandLineWizardPage extends WizardPage {
    private static Logger                                       logger = LoggerFactory.getLogger(CommandLineWizardPage.class);

    private final List<CommandLineOption>                       options;
    private final Map<CommandLineOption, WorkflowToolParameter> parameters;
    private final Map<CommandLineOption, WorkflowToolData>      data;
    private Button                                              del;
    private Button                                              up;
    private Button                                              down;
    private Text                                                exec;
    private Button                                              stdoutCheck;
    private Text                                                stdout;
    private Button                                              joinStdoutStderrCheck;
    private Button                                              stderrCheck;
    private Text                                                stderr;
    private Text                                                commandline;
    private ListViewer                                          viewer;
    private final WorkflowTool                                  oldtool;
    private Button                                              addData;
    private Button                                              edit;

    private Button                                              addValue;

    private Button                                              addParam;

    protected CommandLineWizardPage(String pageName, WorkflowTool oldtool) {
        super(pageName);
        setTitle(pageName);
        setPageComplete(true);
        setMessage("External tool command line with options.");

        options = new ArrayList<CommandLineOption>();
        parameters = new HashMap<CommandLineOption, WorkflowToolParameter>();
        data = new HashMap<CommandLineOption, WorkflowToolData>();
        this.oldtool = oldtool;
    }

    public void createControl(Composite parent) {
        final Composite top = new Composite(parent, SWT.NONE);
        top.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        top.setLayout(new GridLayout(3, false));

        Label lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("Executable:");

        exec = new Text(top, SWT.SINGLE | SWT.BORDER);
        exec.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        exec.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });

        Button btn = new Button(top, SWT.NONE);
        btn.setText("...");
        btn.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
                fd.setFileName(exec.getText());
                String filename = fd.open();
                if (filename != null) {
                    exec.setText(filename);
                    validate();
                }
            }
        });

        stdoutCheck = new Button(top, SWT.CHECK);
        stdoutCheck.setText("Capture stdout?");
        stdoutCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                validate();
            }
        });

        stdout = new Text(top, SWT.SINGLE | SWT.BORDER);
        stdout.setText("stdout");
        stdout.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
        stdout.setEnabled(stdoutCheck.getSelection());
        stdout.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });

        joinStdoutStderrCheck = new Button(top, SWT.CHECK);
        joinStdoutStderrCheck.setText("also stderr?");
        joinStdoutStderrCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                validate();
            }
        });

        stderrCheck = new Button(top, SWT.CHECK);
        stderrCheck.setText("Capture stderr?");
        stderrCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                validate();
            }
        });

        stderr = new Text(top, SWT.SINGLE | SWT.BORDER);
        stderr.setText("stderr");
        stderr.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        stderr.setEnabled(stdoutCheck.getSelection());
        stderr.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validate();
            }
        });

        lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("Options:");

        viewer = new ListViewer(top, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        viewer.setUseHashlookup(true);
        viewer.getList().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                validate();
            }
        });

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                CommandLineOption option = (CommandLineOption) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                switch (option.getType()) {
                case VALUE:
                    if (new CommandLineValueDialog(top.getShell(), option).open() == Dialog.OK) {
                        viewer.refresh();
                        validate();
                    }
                    break;
                case PARAMETER:
                    if (new CommandLineParameterDialog(top.getShell(), option, parameters.get(option)).open() == Dialog.OK) {
                        viewer.refresh();
                        validate();
                    }
                    break;
                case DATA:
                    if (new CommandLineDataDialog(top.getShell(), option, data.get(option)).open() == Dialog.OK) {
                        viewer.refresh();
                        validate();
                    }
                    break;
                }
            }
        });

        // content
        viewer.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                return options.toArray();
            }

            public void dispose() {}

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
        });
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof CommandLineOption) {
                    return getOptionString((CommandLineOption) element);
                }
                return element.toString();
            }
        });

        Composite buttons = new Composite(top, SWT.NULL);
        buttons.setLayout(new GridLayout());

        addValue = new Button(buttons, SWT.PUSH);
        addValue.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        addValue.setText("Add Value");
        addValue.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CommandLineOption option = new CommandLineOption();
                CommandLineValueDialog dlg = new CommandLineValueDialog(top.getShell(), option);
                if (dlg.open() == Dialog.OK) {
                    options.add(option);
                    viewer.refresh();
                    validate();
                }
            }
        });

        addParam = new Button(buttons, SWT.PUSH);
        addParam.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        addParam.setText("Add Parameter");
        addParam.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CommandLineOption option = new CommandLineOption();
                WorkflowToolParameter param = new WorkflowToolParameter();
                CommandLineParameterDialog dlg = new CommandLineParameterDialog(top.getShell(), option, param);
                if (dlg.open() == Dialog.OK) {
                    options.add(option);
                    parameters.put(option, param);
                    viewer.refresh();
                    validate();
                }
            }
        });

        addData = new Button(buttons, SWT.PUSH);
        addData.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        addData.setText("Add Data");
        addData.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CommandLineOption option = new CommandLineOption();
                WorkflowToolData data = new WorkflowToolData();
                CommandLineDataDialog dlg = new CommandLineDataDialog(top.getShell(), option, data);
                if (dlg.open() == Dialog.OK) {
                    options.add(option);
                    CommandLineWizardPage.this.data.put(option, data);
                    viewer.refresh();
                    validate();
                }
            }
        });

        edit = new Button(buttons, SWT.PUSH);
        edit.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        edit.setText("Edit");
        edit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CommandLineOption option = (CommandLineOption) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                switch (option.getType()) {
                case VALUE:
                    if (new CommandLineValueDialog(top.getShell(), option).open() == Dialog.OK) {
                        viewer.refresh();
                        validate();
                    }
                    break;
                case PARAMETER:
                    if (new CommandLineParameterDialog(top.getShell(), option, parameters.get(option)).open() == Dialog.OK) {
                        viewer.refresh();
                        validate();
                    }
                    break;
                case DATA:
                    if (new CommandLineDataDialog(top.getShell(), option, data.get(option)).open() == Dialog.OK) {
                        viewer.refresh();
                        validate();
                    }
                    break;
                }
            }
        });

        del = new Button(buttons, SWT.PUSH);
        del.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        del.setText("Delete");
        del.setEnabled(false);
        del.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Object obj = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                options.remove(obj);
                viewer.remove(obj);
                validate();
            }
        });

        up = new Button(buttons, SWT.PUSH);
        up.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        up.setText("Up");
        up.setEnabled(false);
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = viewer.getList().getSelectionIndex();
                CommandLineOption o = options.get(idx);
                options.set(idx, options.get(idx - 1));
                options.set(idx - 1, o);
                viewer.refresh();
                validate();
            }
        });

        down = new Button(buttons, SWT.PUSH);
        down.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        down.setText("Down");
        down.setEnabled(false);
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = viewer.getList().getSelectionIndex();
                CommandLineOption o = options.get(idx);
                options.set(idx, options.get(idx + 1));
                options.set(idx + 1, o);
                viewer.refresh();
                validate();
            }
        });

        commandline = new Text(top, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        commandline.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1));
        commandline.setEditable(false);

        // set values from old tool
        if (oldtool != null) {
            Transaction transaction = null;
            try {
                transaction = SpringData.getTransaction();
                transaction.start();

                CommandLineImplementation impl = new ObjectMapper().readValue(oldtool.getImplementation(), CommandLineImplementation.class);
                exec.setText(impl.getExecutable());
//          waitGUICheck.setSelection(impl.isGuiwait());
                if (impl.getCaptureStdOut() != null) {
                    stdoutCheck.setSelection(true);
//          oldtool.get
//          WorkflowToolDataBean wtdb = oldtool.getOutput(oldexternal.getCaptureStdout().getRefid());
//          stdout.setText(wtdb.getTitle());
                }

                joinStdoutStderrCheck.setSelection(impl.isJoinStdOutStdErr());

                if (impl.getCaptureStdErr() != null) {
                    stderrCheck.setSelection(true);
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

        viewer.setInput(options);
        validate();
        setControl(top);
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

    private void validate() {
        int index = viewer.getList().getSelectionIndex();
        int size = viewer.getList().getItemCount();

        StringBuffer sb = new StringBuffer();
        sb.append(exec.getText());
        sb.append(" "); //$NON-NLS-1$
        for (CommandLineOption option : options) {
            sb.append(getOptionString(option));
        }

        if (joinStdoutStderrCheck.getSelection()) {
            sb.append(" 2>&1"); //$NON-NLS-1$
        } else if (stderrCheck.getSelection()) {
            sb.append(" 2>"); //$NON-NLS-1$
            sb.append(stderr.getText());
        }

        if (stdoutCheck.getSelection()) {
            sb.append(" >"); //$NON-NLS-1$
            sb.append(stdout.getText());
        }

        commandline.setText(sb.toString());

        del.setEnabled(index >= 0);
        up.setEnabled(size > 1 && index > 0);
        down.setEnabled(size > 1 && index >= 0 && index < size - 1);

        stdout.setEnabled(stdoutCheck.getSelection());
        stderrCheck.setEnabled(!joinStdoutStderrCheck.getSelection());
        stderr.setEnabled(!joinStdoutStderrCheck.getSelection() && stderrCheck.getSelection());

        if (exec.getText().trim().length() == 0) {
            setErrorMessage("Tool needs an executable.");
            setPageComplete(false);
            return;
        }

        if (stdoutCheck.getSelection() && (stdout.getText().trim().length() == 0)) {
            setErrorMessage("If capturing stdout, an output is required.");
            setPageComplete(false);
            return;
        }

        if (stderrCheck.getSelection() && (stderr.getText().trim().length() == 0)) {
            setErrorMessage("If capturing stderr, an output is required.");
            setPageComplete(false);
            return;
        }

        setErrorMessage(null);
        setPageComplete(true);
    }

    public void updateTool(WorkflowTool tool) throws IOException {
        CommandLineImplementation impl = new CommandLineImplementation();

        impl.setExecutable(exec.getText());

        if (stdoutCheck.getSelection()) {
            WorkflowToolData data = new WorkflowToolData();
            data.setTitle(stdout.getText());
            data.setDescription("stdout of external tool.");
            data.setMimeType("text/plain"); //$NON-NLS-1$
            data.setDataId("stdout");
            tool.addOutput(data);

            impl.setCaptureStdOut(data.getDataId());
            if (joinStdoutStderrCheck.getSelection()) {
                impl.setJoinStdOutStdErr(true);
            }
        }

        if (!joinStdoutStderrCheck.getSelection() && stderrCheck.getSelection()) {
            WorkflowToolData data = new WorkflowToolData();
            data.setTitle(stderr.getText());
            data.setDescription("stderr of external tool.");
            data.setMimeType("text/plain"); //$NON-NLS-1$
            data.setDataId("stderr");
            tool.addOutput(data);

            impl.setCaptureStdErr(data.getDataId());
        }

        for (CommandLineOption option : options) {
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

        tool.setImplementation(new ObjectMapper().writeValueAsString(impl));
    }
}