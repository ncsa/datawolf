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
package edu.illinois.ncsa.cyberintegrator.tool.creator.java;

import java.awt.GridBagConstraints;
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
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.executor.java.JavaToolImplementation;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Dataset;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.JavaTool;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Parameter;
import edu.illinois.ncsa.cyberintegrator.tool.creator.Wizard;
import edu.illinois.ncsa.cyberintegrator.tool.creator.WizardPage;
import edu.illinois.ncsa.domain.FileDescriptor;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ToolSelectWizardPage extends WizardPage {
    private static Logger                   logger = LoggerFactory.getLogger(ToolSelectWizardPage.class);

    private JList                           lstTools;
    private List<Class<? extends JavaTool>> tools;
    private final WorkflowTool              oldtool;
    private JavaToolImplementation          oldImplementation;
    private JTextField                      txtName;
    private JTextField                      txtDescription;
    private JTextField                      txtVersion;
    private final Set<FileDescriptor>       allfiles;
    private ToolModel                       model;

    protected ToolSelectWizardPage(Wizard wizard, WorkflowTool oldtool) {
        super(wizard);

        if ((oldtool != null) && (oldtool.getImplementation() != null)) {
            try {
                oldImplementation = new ObjectMapper().readValue(oldtool.getImplementation(), JavaToolImplementation.class);
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

        add(new JLabel("Available tools"), 0, 0);
        model = new ToolModel();
        int selection = (oldtool == null) ? ListSelectionModel.SINGLE_SELECTION : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
        lstTools = new JList(model);
        lstTools.getSelectionModel().setSelectionMode(selection);
        lstTools.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                checkUI();
            }
        });
        add(lstTools, 1, 0, GridBagConstraints.BOTH);

        add(new JLabel("Tool Name"), 0, 1);
        txtName = new JTextField();
        txtName.setEditable(false);
        add(txtName, 1, 1, GridBagConstraints.HORIZONTAL);

        add(new JLabel("Tool Version"), 0, 2);
        txtVersion = new JTextField();
        txtVersion.setEditable(false);
        add(txtVersion, 1, 2, GridBagConstraints.HORIZONTAL);

        add(new JLabel("Tool Description"), 0, 3);
        txtDescription = new JTextField();
        txtDescription.setEditable(false);
        add(txtDescription, 1, 3, GridBagConstraints.HORIZONTAL);

        checkUI();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setToolPath(List<FileDescriptor> files) {
        allfiles.clear();
        allfiles.addAll(files);

        // remember the last selected tool
        Object[] selection = lstTools.getSelectedValues();

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
        for (Class<? extends JavaTool> tool : tools) {
            model.add(tool);
        }

        // reselect previously selected tool
//        if (!selection.isEmpty()) {
//            lvTools.setSelection(selection, true);
//        }
        checkUI();
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

    private void checkUI() {
        setStepComplete(false);
        txtName.setText(""); //$NON-NLS-1$
        txtDescription.setText(""); //$NON-NLS-1$
        txtVersion.setText(""); //$NON-NLS-1$

        int idx = lstTools.getSelectedIndex();
        if (idx != -1) {
            try {
                JavaTool tool = model.getToolAt(idx).newInstance();
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
                setStepComplete(true);
            } catch (Throwable thr) {
                logger.warn("Could not retrieve tool information.", thr);
                model.remove(idx);
                checkUI();
            }
        }
    }

    public Set<WorkflowTool> getTools() throws Exception {
        Set<WorkflowTool> result = new HashSet<WorkflowTool>();

        // editing a single tool, only tool creted.
        if (oldtool != null) {
            WorkflowTool tool = getTool(model.getToolAt(lstTools.getSelectedIndex()));
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
            for (int idx : lstTools.getSelectedIndices()) {
                result.add(getTool(model.getToolAt(idx)));
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
        tool.setImplementation(new ObjectMapper().writeValueAsString(jti));

        return tool;
    }

    class ToolModel extends AbstractListModel {
        private List<Class<? extends JavaTool>> tools = new ArrayList<Class<? extends JavaTool>>();

        public List<Class<? extends JavaTool>> getList() {
            return tools;
        }

        public void remove(Class<? extends JavaTool> tool) {
            int index = tools.indexOf(tool);
            tools.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public void remove(int index) {
            tools.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public void add(Class<? extends JavaTool> tool) {
            int size = tools.size();
            tools.add(tool);
            fireIntervalAdded(this, size + 1, size + 1);
        }

        public Class<? extends JavaTool> getToolAt(int index) {
            return tools.get(index);
        }

        @Override
        public Object getElementAt(int arg0) {
            return tools.get(arg0).getName();
        }

        @Override
        public int getSize() {
            return tools.size();
        }

    }
}
