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
package edu.illinois.ncsa.datawolf.executor.java.wizard;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter;
import edu.illinois.ncsa.datawolf.executor.java.JavaToolImplementation;
import edu.illinois.ncsa.datawolf.executor.java.tool.Dataset;
import edu.illinois.ncsa.datawolf.executor.java.tool.JavaTool;
import edu.illinois.ncsa.datawolf.executor.java.tool.Parameter;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ToolSelectWizardPage extends WizardPage {
    private static Logger                   logger = LoggerFactory.getLogger(ToolSelectWizardPage.class);

    private ListViewer                      lvTools;
    private List<Class<? extends JavaTool>> tools;
    private final WorkflowTool              oldtool;
    private JavaToolImplementation          oldImplementation;
    private Text                            txtName;
    private Text                            txtDescription;
    private Text                            txtVersion;
    private final Set<FileDescriptor>       allfiles;

    protected ToolSelectWizardPage(String pageName, WorkflowTool oldtool) {
        super(pageName);
        setTitle(pageName);
        setPageComplete(false);
        setMessage("Select tool from available tools.");

        if ((oldtool != null) && (oldtool.getImplementation() != null)) {
            try {
                oldImplementation = SpringData.JSONToObject(oldtool.getImplementation(), JavaToolImplementation.class);
            } catch (IOException e) {
                logger.error("Could not get old java tool implementation.", e);
                oldImplementation = null;
            }
        } else {
            oldImplementation = null;
        }

        this.oldtool = oldtool;
        this.tools = new ArrayList<Class<? extends JavaTool>>();
        this.allfiles = new HashSet<FileDescriptor>();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setToolPath(List<FileDescriptor> files) {
        allfiles.clear();
        allfiles.addAll(files);

        // remember the last selected tool
        ISelection selection = lvTools.getSelection();
        if (selection.isEmpty() && (oldImplementation != null) && (oldImplementation.getToolClassName() != null)) {
            selection = new StructuredSelection(oldImplementation.getToolClassName());
        }

        // Fill the reflections classloader
        URL[] urls = new URL[files.size()];
        for (int i = 0; i < urls.length; i++) {
            try {
                urls[i] = new URL(files.get(i).getDataURL());
            } catch (MalformedURLException e) {
                logger.error("file is not a valid url " + files.get(i).getDataURL(), e);
            }
        }

        // find all the tools
        tools.clear();
        tools.addAll(getTools(urls));
        Collections.sort(tools, new Comparator() {
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        lvTools.setInput(tools);

        // reselect previously selected tool
        if (!selection.isEmpty()) {
            lvTools.setSelection(selection, true);
        }
        validate();
    }

    private Set<Class<? extends JavaTool>> getTools(URL[] urls) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.addUrls(urls);
        Reflections reflections = new Reflections(cb);

        // work around issue where reflections does not configration urls
        // return reflections.getSubTypesOf(JavaTool.class);
        Set<String> classnames = reflections.getStore().getSubTypesOf(JavaTool.class.getName());
        return ImmutableSet.copyOf(ReflectionUtils.<JavaTool> forNames(classnames, new URLClassLoader(urls, Thread.currentThread().getContextClassLoader())));
    }

    public void createControl(Composite parent) {
        final Composite top = new Composite(parent, SWT.NONE);
        top.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        top.setLayout(new GridLayout(2, false));

        Label lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        lbl.setText("Available tools");

        int selection = (oldtool == null) ? SWT.MULTI : SWT.SINGLE;
        lvTools = new ListViewer(top, selection | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        lvTools.getList().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
        lvTools.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                return tools.toArray();
            }

            public void dispose() {}

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
        });
        lvTools.setLabelProvider(new LabelProvider());
        lvTools.setInput(tools);
        lvTools.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                validate();
            }
        });

        lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("Tool Name");

        txtName = new Text(top, SWT.NONE | SWT.BORDER | SWT.SINGLE);
        txtName.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtName.setEditable(false);

        lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("Tool Version");

        txtVersion = new Text(top, SWT.NONE | SWT.BORDER | SWT.SINGLE);
        txtVersion.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
        txtVersion.setEditable(false);

        lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        lbl.setText("Tool Description");

        txtDescription = new Text(top, SWT.NONE | SWT.BORDER | SWT.MULTI);
        txtDescription.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
        txtDescription.setEditable(false);

        validate();
        setControl(top);
    }

    private void validate() {
        IStructuredSelection ssel = (IStructuredSelection) lvTools.getSelection();
        setPageComplete(false);
        txtName.setText(""); //$NON-NLS-1$
        txtDescription.setText(""); //$NON-NLS-1$
        txtVersion.setText(""); //$NON-NLS-1$
        if (!ssel.isEmpty()) {
            for (Iterator<Class<? extends JavaTool>> iter = ssel.iterator(); iter.hasNext();) {
                Class<? extends JavaTool> item = iter.next();
                try {
                    JavaTool tool = item.newInstance();
                    if (tool.getName() != null) {
                        txtName.setText(tool.getName());
                    } else {
                        txtName.setText("Unknown tool"); //$NON-NLS-1$
                    }
                    txtVersion.setText(Integer.toString(tool.getVersion()));
                    if (tool.getDescription() != null) {
                        txtDescription.setText(tool.getDescription());
                    } else {
                        txtDescription.setText(""); //$NON-NLS-1$
                    }
                    setPageComplete(true);
                } catch (Throwable thr) {
                    logger.warn("Could not retrieve tool information.", thr);
                    tools.remove(item);
                    lvTools.remove(item);
                }
            }
        }
    }

    public Set<WorkflowTool> getTools() throws Exception {
        IStructuredSelection ssel = (IStructuredSelection) lvTools.getSelection();
        Set<WorkflowTool> result = new HashSet<WorkflowTool>();

        // editing a single tool, only tool creted.
        if (oldtool != null) {
            WorkflowTool tool = getTool((Class<? extends JavaTool>) ssel.getFirstElement());
            tool.setPreviousVersion(oldtool);

            // add list of all people working on this tool
            // TODO RK : add people
//            if (!oldtool.getCreator().equals(Workflow.getUserBean())) {
//                tool.addContributor(oldtool.getCreator());
//            }
//            for (Person user : oldtool.getContributors()) {
//                if (!user.equals(Workflow.getUserBean())) {
//                    tool.addContributor(user);
//                }
//            }

            // only tool created
            result.add(tool);
        } else {
            for (Iterator<Class<? extends JavaTool>> iter = ssel.iterator(); iter.hasNext();) {
                result.add(getTool(iter.next()));
            }
        }

        return result;
    }

    private WorkflowTool getTool(Class<? extends JavaTool> item) throws InstantiationException, IllegalAccessException, IOException {
        JavaTool jt = item.newInstance();

        // create the tool
        WorkflowTool tool = new WorkflowTool();
        tool.setDate(new Date());
        // TODO RK : add people
//        tool.setCreator(Workflow.getUserBean());
        tool.setExecutor("java"); //$NON-NLS-1$

        // add tool info
        if (jt.getName() != null) {
            tool.setTitle(jt.getName().trim());
        } else {
            tool.setTitle("Unknown tool");
        }
        if (jt.getDescription() != null) {
            tool.setDescription(jt.getDescription().trim());
        }
        tool.setVersion(Integer.toString(jt.getVersion()));

        // add inputs
        if (jt.getInputs() != null) {
            for (Dataset datadef : jt.getInputs()) {
                WorkflowToolData wtd = new WorkflowToolData();
                wtd.setDataId(datadef.getID());
                wtd.setTitle(datadef.getName());
                wtd.setDescription(datadef.getDescription());
                wtd.setMimeType(datadef.getType());
                tool.addInput(wtd);
            }
        }

        // add outputs
        if (jt.getOutputs() != null) {
            for (Dataset datadef : jt.getOutputs()) {
                WorkflowToolData wtd = new WorkflowToolData();
                wtd.setDataId(datadef.getID());
                wtd.setTitle(datadef.getName());
                wtd.setDescription(datadef.getDescription());
                wtd.setMimeType(datadef.getType());
                tool.addOutput(wtd);
            }
        }

        // add parameters
        if (jt.getParameters() != null) {
            for (Parameter paramdef : jt.getParameters()) {
                WorkflowToolParameter wp = new WorkflowToolParameter();
                wp.setParameterId(paramdef.getID());
                wp.setTitle(paramdef.getName());
                wp.setDescription(paramdef.getDescription());
                wp.setType(WorkflowToolParameter.ParameterType.valueOf(paramdef.getType().toString().toUpperCase()));
                wp.setValue(paramdef.getValue());
                wp.setAllowNull(paramdef.isAllowEmpty());
                wp.setHidden(paramdef.isHidden());
                wp.setOptions(Arrays.asList(paramdef.getOptions()));
                tool.addParameter(wp);
            }
        }

        // add tool implementation
        JavaToolImplementation jti = new JavaToolImplementation();
        jti.setToolClassName(item.getName());
        tool.setImplementation(SpringData.objectToJSON(jti));

        return tool;
    }
}
