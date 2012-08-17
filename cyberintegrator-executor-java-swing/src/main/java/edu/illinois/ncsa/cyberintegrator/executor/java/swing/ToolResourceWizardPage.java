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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
@SuppressWarnings("serial")
public class ToolResourceWizardPage extends PanelWizardStep {
    private static final String     UNSAVED_DATA = "0";

    private FileDescriptorListModel listModel;
    private JList                   filelist;
    private JButton                 add;
    private JButton                 del;
    private int                     minfiles;
    private JFileChooser            fileChooser;

    public ToolResourceWizardPage() {
        super("Select resources for tool", "List of resources to bundle with the tool.");
        this.minfiles = 0;
        this.listModel = new FileDescriptorListModel();
        this.fileChooser = new JFileChooser();

        if (System.getProperty("os.name").toLowerCase().indexOf("win") > 0) {
            addFilter("*.*", "All Files");
        } else {
            addFilter("*", "All Files");
        }

        createGUI();
    }

    @Override
    public void init(WizardModel model) {
        listModel.deleteAll();
        if (((JavaWizardModel) model).getOldTool() != null) {
            for (FileDescriptor fd : ((JavaWizardModel) model).getOldTool().getBlobs()) {
                listModel.add(fd);
            }
        }
    }

    @Override
    public void prepare() {
        checkPage();
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
    public void addFilter(final String ext, final String name) {
        // TODO RK : need filefilter chooser
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return name;
            }

            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(ext.toLowerCase());
            }
        });
    }

    private void createGUI() {
        add(new JLabel("Files:"), BorderLayout.WEST);

        filelist = new JList(listModel);
        add(filelist, BorderLayout.CENTER);

        JPanel pnl = new JPanel(new GridLayout(-1, 1));
        add(pnl, BorderLayout.EAST);

        add = new JButton("Add");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showOpenDialog(ToolResourceWizardPage.this) == JFileChooser.APPROVE_OPTION) {
                    for (File file : fileChooser.getSelectedFiles()) {
                        boolean found = false;
                        for (FileDescriptor desc : listModel.getElements()) {
                            if (desc.getId().equals(UNSAVED_DATA) && desc.getFilename().equals(file.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            FileDescriptor desc = new FileDescriptor();
                            desc.setId(UNSAVED_DATA);
                            desc.setFilename(file.getName());
                            desc.setDataURL(file.toURI().toString());
                            listModel.add(desc);
                        }
                    }
                }
                checkPage();
            }
        });
        pnl.add(add);

        del = new JButton("Remove");
        del.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                listModel.delete(filelist.getSelectedIndex());
                checkPage();
            }
        });
        pnl.add(del);
    }

    private void checkPage() {
        del.setEnabled(filelist.getSelectedIndex() >= 0);
        setComplete(filelist.getModel().getSize() >= minfiles);
    }

    public List<FileDescriptor> getFileDescriptors() {
        return listModel.getElements();
    }

    /**
     * Add the filedescriptors of the blobs to the file.
     */
    public void updateTool(WorkflowTool tool) throws IOException {
        if (listModel.getSize() == 0) {
            return;
        }

        // store the blobs
        FileStorage fs = SpringData.getBean(FileStorage.class);
        for (FileDescriptor fd : listModel.getElements()) {
            if (fd.getId().equals(UNSAVED_DATA)) {
                tool.addBlob(fs.storeFile(fd.getFilename(), new URL(fd.getDataURL()).openStream()));
            } else {
                tool.addBlob(fd);
            }
        }
    }

    public static class FileDescriptorListModel extends AbstractListModel {
        private List<FileDescriptor> fileDescriptors;

        public FileDescriptorListModel() {
            fileDescriptors = new ArrayList<FileDescriptor>();
        }

        public void add(FileDescriptor fd) {
            fileDescriptors.add(fd);
            fireIntervalAdded(this, fileDescriptors.size(), fileDescriptors.size());
        }

        public void delete(int index) {
            fileDescriptors.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public void deleteAll() {
            int last = fileDescriptors.size() - 1;
            fileDescriptors.clear();
            fireIntervalRemoved(this, 0, last);
        }

        public List<FileDescriptor> getElements() {
            return fileDescriptors;
        }

        @Override
        public int getSize() {
            return fileDescriptors.size();
        }

        @Override
        public FileDescriptor getElementAt(int index) {
            return fileDescriptors.get(index);
        }
    }
}
