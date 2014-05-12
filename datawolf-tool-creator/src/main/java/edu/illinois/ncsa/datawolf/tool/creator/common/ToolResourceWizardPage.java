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
package edu.illinois.ncsa.datawolf.tool.creator.common;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.tool.creator.Wizard;
import edu.illinois.ncsa.datawolf.tool.creator.WizardPage;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ToolResourceWizardPage extends WizardPage {
    private static final String  UNSAVED_DATA = "0";

    private static Logger        logger       = LoggerFactory.getLogger(ToolResourceWizardPage.class);

    private JList                filelist;
    private JButton              add;
    private JButton              del;
    private final WorkflowTool   oldtool;
    private int                  minfiles;
    private final List<String>   ext;
    private final List<String>   name;
    private FileDescriptorsModel model;

    public ToolResourceWizardPage(Wizard wizard, WorkflowTool oldtool) {
        super(wizard);

        this.oldtool = oldtool;
        this.minfiles = 0;
        this.ext = new ArrayList<String>();
        this.name = new ArrayList<String>();

        add(new JLabel("Files:"), 0, 0);

        model = new FileDescriptorsModel();
        filelist = new JList(model);
        filelist.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                checkUI();
            }
        });
        add(filelist, 1, 0, GridBagConstraints.BOTH);
        if (oldtool != null) {
            for (FileDescriptor fd : oldtool.getBlobs()) {
                model.add(fd);
            }
        }

        JPanel buttons = new JPanel(new GridLayout(0, 1));
        add(buttons, 2, 0);

        add = new JButton("Add...");
        buttons.add(add);
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(true);

                for (int i = 0; i < ext.size(); i++) {
                    final String fext = ext.get(i);
                    final String ftxt = name.get(i);
                    chooser.addChoosableFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File arg0) {
                            return arg0.isDirectory() || arg0.getName().endsWith(fext);
                        }

                        @Override
                        public String getDescription() {
                            return ftxt;
                        }

                    });
                }

                if (chooser.showOpenDialog(ToolResourceWizardPage.this) == JFileChooser.APPROVE_OPTION) {
                    for (File file : chooser.getSelectedFiles()) {
                        boolean found = false;
                        try {
                            for (FileDescriptor desc : model.getList()) {
                                if (desc.getId().equals(UNSAVED_DATA) && desc.getDataURL().equals(file.toURI().toURL().toString())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                FileDescriptor desc = new FileDescriptor();
                                desc.setId(UNSAVED_DATA);
                                desc.setFilename(file.getName());
                                desc.setDataURL(file.toURI().toURL().toString());
                                model.add(desc);
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                checkUI();
            }
        });

        del = new JButton("Remove");
        buttons.add(del);
        del.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                FileDescriptor[] removeme = (FileDescriptor[]) filelist.getSelectedValues();
                for (FileDescriptor fd : removeme) {
                    model.remove(fd);
                }
                checkUI();
            }
        });

        checkUI();
    }

    public void setMinimumFiles(int minfiles) {
        this.minfiles = minfiles;
    }

    /**
     * Add a new filter to the file dialog.
     * 
     * @param ext
     *            extentions of filter.
     * @param name
     *            name of the filter will automatically add (ext).
     */
    public void addFilter(String ext, String name) {
        this.ext.add(0, ext);
        this.name.add(0, String.format("%s (%s)", name, ext)); //$NON-NLS-1$
    }

    private void checkUI() {
        del.setEnabled(filelist.getSelectedIndices().length > 0);
        setStepComplete(model.getSize() >= minfiles);
    }

    public List<FileDescriptor> getFileDescriptors() {
        return model.getList();
    }

    /**
     * Add the filedescriptors of the blobs to the file.
     */
    public void updateTool(WorkflowTool tool) throws IOException {
        if (model.getSize() == 0) {
            return;
        }

        // store the blobs
        FileStorage fs = SpringData.getBean(FileStorage.class);
        for (FileDescriptor fd : model.getList()) {
            if (fd.getId().equals(UNSAVED_DATA)) {
                tool.addBlob(fs.storeFile(fd.getFilename(), new URL(fd.getDataURL()).openStream()));
            } else {
                tool.addBlob(fd);
            }
        }
    }

    class FileDescriptorsModel extends AbstractListModel {
        private List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();

        public List<FileDescriptor> getList() {
            return fileDescriptors;
        }

        public void remove(FileDescriptor fd) {
            int index = fileDescriptors.indexOf(fd);
            fileDescriptors.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public void add(FileDescriptor fd) {
            int size = fileDescriptors.size();
            fileDescriptors.add(fd);
            fireIntervalAdded(this, size + 1, size + 1);
        }

        @Override
        public Object getElementAt(int arg0) {
            return fileDescriptors.get(arg0).getFilename();
        }

        @Override
        public int getSize() {
            return fileDescriptors.size();
        }

    }
}
