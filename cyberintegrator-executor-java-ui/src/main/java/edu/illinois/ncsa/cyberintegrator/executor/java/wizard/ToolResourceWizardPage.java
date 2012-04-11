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
package edu.illinois.ncsa.cyberintegrator.executor.java.wizard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class ToolResourceWizardPage extends WizardPage {
    static private Logger        logger          = LoggerFactory.getLogger(ToolResourceWizardPage.class);

    private List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
    private ListViewer           filelist;
    private Button               add;
    private Button               del;
    private final WorkflowTool   oldtool;
    private File                 tempdir;
    private int                  minfiles;
    private final List<String>   ext;
    private final List<String>   name;

    private File                 zip;

    public ToolResourceWizardPage(String pageName, WorkflowTool oldtool) {
        super(pageName);
        this.oldtool = oldtool;
        this.minfiles = 0;
        this.ext = new ArrayList<String>();
        this.name = new ArrayList<String>();
        setTitle(pageName);
        setMessage("List of resources to bundle with the tool.");

        if (System.getProperty("os.name").toLowerCase().indexOf("win") > 0) {
            addFilter("*.*", "All Files");
        } else {
            addFilter("*", "All Files");
        }
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

    public void createControl(Composite parent) {
        final Composite top = new Composite(parent, SWT.NONE);
        top.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        top.setLayout(new GridLayout(3, false));

        Label lbl = new Label(top, SWT.NONE);
        lbl.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
        lbl.setText("Files:");

        filelist = new ListViewer(top);
        filelist.setContentProvider(new IStructuredContentProvider() {
            public void dispose() {}

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

            public Object[] getElements(Object inputElement) {
                return fileDescriptors.toArray(new FileDescriptor[fileDescriptors.size()]);
            }
        });
        filelist.setLabelProvider(new LabelProvider());
        filelist.getList().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                validate();
            }
        });

        if (oldtool != null) {
            for (FileDescriptor fd : oldtool.getBlobs()) {
                fileDescriptors.add(fd);
            }
            filelist.refresh();
        }

        Composite buttons = new Composite(top, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, true));
        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        buttons.setLayout(gl);

        add = new Button(buttons, SWT.PUSH);
        add.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        add.setText("Add...");
        add.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fd = new FileDialog(top.getShell(), SWT.MULTI);
                fd.setFilterExtensions(ext.toArray(new String[ext.size()]));
                fd.setFilterNames(name.toArray(new String[name.size()]));
                fd.setText("Select files to add:");
                if (fd.open() != null) {
                    for (String filename : fd.getFileNames()) {
                        boolean found = false;
                        for (FileDescriptor desc : fileDescriptors) {
                            if (desc.getId().equals("0") && desc.getFilename().equals(filename)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            try {
                                FileDescriptor desc = new FileDescriptor();
                                desc.setId("0");
                                desc.setFilename(filename);
                                desc.setDataURL(new URL("file:" + fd.getFilterPath() + File.separator + filename));
                                fileDescriptors.add(desc);
                            } catch (MalformedURLException e1) {
                                logger.error("Could not add requested file : " + fd.getFilterPath() + File.separator + filename);
                            }
                        }
                    }
                }
                filelist.refresh();
                validate();
            }
        });

        del = new Button(buttons, SWT.PUSH);
        del.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        del.setText("Remove");
        del.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fileDescriptors.remove(filelist.getList().getSelectionIndices());
                validate();
            }
        });

        validate();
        setControl(top);
    }

    private void validate() {
        del.setEnabled(filelist.getList().getSelectionCount() > 0);
        setPageComplete(filelist.getList().getItemCount() >= minfiles);
    }

    public List<FileDescriptor> getFileDescriptors() {
        return fileDescriptors;
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
            if (fd.getId().equals("0")) {
                tool.addBlob(fs.storeFile(fd.getFilename(), fd.getDataURL().openStream()));
            } else {
                tool.addBlob(fd);
            }
        }
    }
}
