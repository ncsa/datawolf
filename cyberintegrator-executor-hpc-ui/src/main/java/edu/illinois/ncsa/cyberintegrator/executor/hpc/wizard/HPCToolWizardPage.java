package edu.illinois.ncsa.cyberintegrator.executor.hpc.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter.ParameterType;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.Type;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.wizard.CommandLineDataDialog;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.wizard.CommandLineParameterDialog;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.wizard.CommandLineValueDialog;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.HPCToolImplementation;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;

public class HPCToolWizardPage extends WizardPage {
    private static final String                                 UNSAVED_DATA    = "0";
    private final List<CommandLineOption>                       options;
    private final Map<CommandLineOption, WorkflowToolParameter> parameters;
    private final Map<CommandLineOption, WorkflowToolData>      data;
    private Text                                                exec;
    private Text                                                hpcTemplate;
    private List<FileDescriptor>                                fileDescriptors = new ArrayList<FileDescriptor>();
    private ListViewer                                          viewer;
    private Text                                                commandline;
    private Button                                              del;
    private Button                                              up;
    private Button                                              down;
    private Button                                              addData;
    private Button                                              edit;

    private Button                                              addValue;

    private Button                                              addParam;

    protected HPCToolWizardPage(String pageName, WorkflowTool oldTool) {
        super(pageName);

        setTitle(pageName);
        setPageComplete(true);
        setMessage("Remote tool command line with options");

        options = new ArrayList<CommandLineOption>();
        parameters = new HashMap<CommandLineOption, WorkflowToolParameter>();
        data = new HashMap<CommandLineOption, WorkflowToolData>();
    }

    public void createControl(Composite parent) {
        final Composite top = new Composite(parent, SWT.NONE);
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        top.setLayout(new GridLayout(3, false));

        // Browse for the shell script
        Label lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("Executable:");

        exec = new Text(top, SWT.SINGLE | SWT.BORDER);
        exec.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
        exec.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // validate();
            }
        });

        /*
        Button btn = new Button(top, SWT.NONE);
        btn.setText("...");
        btn.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
                fd.setFileName(exec.getText());
                if (fd.open() != null) {
                    String filename = fd.getFileName();
                    exec.setText(filename);
                    boolean found = false;
                    for (FileDescriptor desc : fileDescriptors) {
                        if (desc.getId().equals(UNSAVED_DATA) && desc.getFilename().equals(filename)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        FileDescriptor desc = new FileDescriptor();
                        desc.setId(UNSAVED_DATA);
                        desc.setFilename(filename);
                        desc.setDataURL("file:" + fd.getFilterPath() + File.separator + filename);
                        fileDescriptors.add(desc);
                    }
                    // validate();
                }
            }
        });
		*/
        // Browse for the gondola template
        lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("HPC Template:");

        hpcTemplate = new Text(top, SWT.SINGLE | SWT.BORDER);
        hpcTemplate.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        hpcTemplate.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // validate();
            }
        });

        Button btn = new Button(top, SWT.NONE);
        btn.setText("...");
        btn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
                fd.setFileName(hpcTemplate.getText());
                if (fd.open() != null) {
                    String filename = fd.getFileName();
                    hpcTemplate.setText(filename);
                    boolean found = false;
                    for (FileDescriptor desc : fileDescriptors) {
                        if (desc.getId().equals(UNSAVED_DATA) && desc.getFilename().equals(filename)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        FileDescriptor desc = new FileDescriptor();
                        desc.setId(UNSAVED_DATA);
                        desc.setFilename(filename);
                        desc.setDataURL("file:" + fd.getFilterPath() + File.separator + filename);
                        fileDescriptors.add(desc);
                    }
                    // validate();
                }
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
                WorkflowToolData tooldata = new WorkflowToolData();
                CommandLineDataDialog dlg = new CommandLineDataDialog(top.getShell(), option, tooldata);
                if (dlg.open() == Dialog.OK) {
                    options.add(option);
                    data.put(option, tooldata);
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
        viewer.setInput(options);
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

    /**
     * Add the filedescriptors of the blobs to the file.
     */
    public void updateTool(WorkflowTool tool) throws IOException {
        if (fileDescriptors.size() == 0) {
            return;
        }

        // store the blobs
        FileStorage fs = SpringData.getBean(FileStorage.class);
        for (FileDescriptor fd : fileDescriptors) {
            if (fd.getId().equals(UNSAVED_DATA)) {
                tool.addBlob(fs.storeFile(fd.getFilename(), new URL(fd.getDataURL()).openStream()));
            } else {
                tool.addBlob(fd);
            }
        }

        HPCToolImplementation impl = new HPCToolImplementation();
        impl.setExecutable(exec.getText());
        impl.setTemplate(hpcTemplate.getText());

        // Make this a UI option
        // This is the Gondola log, not the actual standard out from the execution
        WorkflowToolData data = new WorkflowToolData();
        data.setTitle("gondola-log");
        data.setDescription("gondola log file.");
        data.setMimeType("text/plain"); //$NON-NLS-1$
        data.setDataId("gondola-log");
        tool.addOutput(data);

        impl.setLog(data.getDataId());
        
        // Capture Standard Error and Standard Out from the execution
        WorkflowToolData standardOut = new WorkflowToolData();
        standardOut.setTitle("standard-out");
        standardOut.setDescription("standard out of remote tool.");
        standardOut.setMimeType("text/plain");
        
        impl.setCaptureStdOut(standardOut.getDataId());
        tool.addOutput(standardOut);
        
        WorkflowToolData standardErr = new WorkflowToolData();
        standardErr.setTitle("standard-err");
        standardErr.setDescription("standard error of remote tool.");
        standardErr.setMimeType("text/plain");
        
        impl.setCaptureStdErr(standardErr.getDataId());
        tool.addOutput(standardErr);

        // Add Username
        WorkflowToolParameter param = new WorkflowToolParameter();
        param.setAllowNull(false);
        param.setTitle("Target Username");
        param.setType(ParameterType.STRING);
        param.setDescription("Username on remote host");

        CommandLineOption specialOption = new CommandLineOption();
        specialOption.setType(Type.PARAMETER);
        specialOption.setOptionId(param.getParameterId());
        specialOption.setCommandline(false);
        this.parameters.put(specialOption, param);
        this.options.add(specialOption);

        // tool.addParameter(param);

        // Add User home on host
        param = new WorkflowToolParameter();
        param.setAllowNull(false);
        param.setTitle("Target Userhome");
        param.setType(ParameterType.STRING);
        param.setDescription("Username on remote host");

        specialOption = new CommandLineOption();
        specialOption.setType(Type.PARAMETER);
        specialOption.setOptionId(param.getParameterId());
        specialOption.setCommandline(false);
        this.parameters.put(specialOption, param);
        this.options.add(specialOption);

        // tool.addParameter(param);

        // Add HPC Target
        param = new WorkflowToolParameter();
        param.setAllowNull(false);
        param.setTitle("Target SSH");
        param.setType(ParameterType.STRING);
        param.setDescription("Remote host ssh");
        specialOption = new CommandLineOption();
        specialOption.setType(Type.PARAMETER);
        specialOption.setOptionId(param.getParameterId());
        specialOption.setCommandline(false);
        this.parameters.put(specialOption, param);
        this.options.add(specialOption);

        // tool.addParameter(param);

        // Other options
        for (CommandLineOption option : options) {
            switch (option.getType()) {
            case VALUE:
            	break;
            	
            case PARAMETER:
                WorkflowToolParameter toolParameter = parameters.get(option);
                // option.setId(toolParameter.getId());
                tool.addParameter(toolParameter);

                break;
            case DATA:
                WorkflowToolData toolData = this.data.get(option);
                if (option.getInputOutput().equals(CommandLineOption.InputOutput.INPUT)) {
                    option.setOptionId(toolData.getDataId());
                    tool.addInput(toolData);
                }

            }
        }

        impl.setCommandLineOptions(this.options);
        
        tool.setImplementation(SpringData.objectToJSON(impl));
    }

    private void validate() {
        int index = viewer.getList().getSelectionIndex();
        int size = viewer.getList().getItemCount();

        /*
         * StringBuffer sb = new StringBuffer();
         * sb.append(exec.getText());
         * sb.append(" "); //$NON-NLS-1$
         * for (CommandLineOption option : options) {
         * sb.append(getOptionString(option));
         * }
         * 
         * if (joinStdoutStderrCheck.getSelection()) {
         * sb.append(" 2>&1"); //$NON-NLS-1$
         * } else if (stderrCheck.getSelection()) {
         * sb.append(" 2>"); //$NON-NLS-1$
         * sb.append(stderr.getText());
         * }
         * 
         * if (stdoutCheck.getSelection()) {
         * sb.append(" >"); //$NON-NLS-1$
         * sb.append(stdout.getText());
         * }
         * 
         * commandline.setText(sb.toString());
         */

        del.setEnabled(index >= 0);
        up.setEnabled(size > 1 && index > 0);
        down.setEnabled(size > 1 && index >= 0 && index < size - 1);

        // stdout.setEnabled(stdoutCheck.getSelection());
        // stderrCheck.setEnabled(!joinStdoutStderrCheck.getSelection());
        // stderr.setEnabled(!joinStdoutStderrCheck.getSelection() &&
// stderrCheck.getSelection());

        if (exec.getText().trim().length() == 0) {
            setErrorMessage("Tool needs an executable.");
            setPageComplete(false);
            return;
        }

        // if (stdoutCheck.getSelection() && (stdout.getText().trim().length()
// == 0)) {
        // setErrorMessage("If capturing stdout, an output is required.");
        // setPageComplete(false);
        // return;
        // }

        // if (stderrCheck.getSelection() && (stderr.getText().trim().length()
// == 0)) {
        // setErrorMessage("If capturing stderr, an output is required.");
        // setPageComplete(false);
        // return;
        // }

        setErrorMessage(null);
        setPageComplete(true);
    }

}
