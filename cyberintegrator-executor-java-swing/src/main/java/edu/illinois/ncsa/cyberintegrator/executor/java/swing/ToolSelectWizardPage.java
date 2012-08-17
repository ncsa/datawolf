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
package edu.illinois.ncsa.cyberintegrator.executor.java.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.executor.java.JavaToolImplementation;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Dataset;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.JavaTool;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Parameter;
import edu.illinois.ncsa.domain.FileDescriptor;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ToolSelectWizardPage extends PanelWizardStep {
    private static Logger             logger = LoggerFactory.getLogger(ToolSelectWizardPage.class);

    private JList                     lstTools;
    private ToolListModel             listModel;
    private WorkflowTool              oldtool;
    private JavaToolImplementation    oldImplementation;
    private JTextField                txtName;
    private JTextField                txtDescription;
    private JTextField                txtVersion;
    private final Set<FileDescriptor> allfiles;
    private JavaWizardModel           model;

    protected ToolSelectWizardPage() {
        super("Select tool", "Select tool from available tools.");
        this.allfiles = new HashSet<FileDescriptor>();

        createGUI();
    }

    @Override
    public void init(WizardModel model) {
        this.model = (JavaWizardModel) model;
        this.oldtool = this.model.getOldTool();

        if ((oldtool != null) && (oldtool.getImplementation() != null) && (oldtool.getImplementation() instanceof JavaToolImplementation)) {
            oldImplementation = (JavaToolImplementation) oldtool.getImplementation();
        } else {
            oldImplementation = null;
        }
    }

    @Override
    public void prepare() {
        allfiles.clear();
        allfiles.addAll(model.getFileDescriptors());

        // remember the last selected tool
        // TODO RK : remember selection
//        ISelection selection = lvTools.getSelection();
//        if (selection.isEmpty() && (oldImplementation != null) && (oldImplementation.getToolClassName() != null)) {
//            selection = new StructuredSelection(oldImplementation.getToolClassName());
//        }

        // Fill the reflections classloader
        URL[] urls = new URL[allfiles.size()];
        int i = 0;
        for (FileDescriptor fd : allfiles) {
            try {
                urls[i++] = new URL(fd.getDataURL());
            } catch (MalformedURLException e) {
                logger.error("file is not a valid url " + fd.getDataURL(), e);
            }
        }

        // find all the tools
        listModel.removeAll();
        listModel.addAll(getTools(urls));
//        Collections.sort(tools, new Comparator() {
//            public int compare(Object o1, Object o2) {
//                return o1.toString().compareTo(o2.toString());
//            }
//        });

        // reselect previously selected tool
        // TODO RK : remember selection
//        if (!selection.isEmpty()) {
//            lvTools.setSelection(selection, true);
//        }
        checkPage();
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

    private void createGUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel lbl = new JLabel("Available tools");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(lbl, c);

        lstTools = new JList(listModel);
        lstTools.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                checkPage();
            }
        });
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        add(lstTools, c);

        lbl = new JLabel("Tool Name");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        add(lbl, c);

        txtName = new JTextField();
        txtName.setEditable(false);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(txtName, c);

        lbl = new JLabel("Tool Version");
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        add(lbl, c);

        txtVersion = new JTextField();
        txtName.setEditable(false);
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(txtVersion, c);

        lbl = new JLabel("Tool Description");
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        add(lbl, c);

        txtDescription = new JTextField();
        txtName.setEditable(false);
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(txtDescription, c);

        checkPage();
    }

    private void checkPage() {
        txtName.setText(""); //$NON-NLS-1$
        txtDescription.setText(""); //$NON-NLS-1$
        txtVersion.setText(""); //$NON-NLS-1$

        if (lstTools.getSelectedIndex() >= 0) {
            Class<? extends JavaTool> item = (Class<? extends JavaTool>) listModel.getElementAt(lstTools.getSelectedIndex());
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
                setComplete(true);
            } catch (Throwable thr) {
                logger.warn("Could not retrieve tool information.", thr);
                listModel.remove(lstTools.getSelectedIndex());
                checkPage();
            }
        } else {
            setComplete(false);
        }
    }

    @SuppressWarnings("unchecked")
    public Set<WorkflowTool> getTools() throws Exception {
        Set<WorkflowTool> result = new HashSet<WorkflowTool>();

        // editing a single tool, only tool creted.
        if (oldtool != null) {
            @SuppressWarnings("unchecked")
            WorkflowTool tool = getTool((Class<? extends JavaTool>) listModel.getElementAt(lstTools.getSelectedIndex()));
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
            for (int i : lstTools.getSelectedIndices()) {
                result.add(getTool((Class<? extends JavaTool>) listModel.getElementAt(i)));
            }
        }

        return result;
    }

    private WorkflowTool getTool(Class<? extends JavaTool> item) throws InstantiationException, IllegalAccessException {
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
        tool.setImplementation(jti);

        return tool;
    }

    public static class ToolListModel extends AbstractListModel {
        private List<Class<? extends JavaTool>> tools;

        public ToolListModel() {
            tools = new ArrayList<Class<? extends JavaTool>>();
        }

        public void add(Class<? extends JavaTool> tool) {
            tools.add(tool);
            fireIntervalAdded(this, tools.size() - 1, tools.size() - 1);
        }

        public void addAll(Collection<Class<? extends JavaTool>> tools) {
            int size = this.tools.size();
            this.tools.addAll(tools);
            fireIntervalAdded(this, size, this.tools.size() - 1);
        }

        public void remove(int index) {
            tools.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public void removeAll() {
            int last = tools.size() - 1;
            tools.clear();
            fireIntervalRemoved(this, 0, last);
        }

        @Override
        public int getSize() {
            return tools.size();
        }

        @Override
        public Object getElementAt(int index) {
            return tools.get(index);
        }
    }
}
