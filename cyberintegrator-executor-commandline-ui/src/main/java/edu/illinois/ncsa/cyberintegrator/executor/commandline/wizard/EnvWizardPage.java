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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineImplementation;

public class EnvWizardPage extends WizardPage {
    private static Logger             logger = LoggerFactory.getLogger(EnvWizardPage.class);

    private final Map<String, String> env;
    private Button                    btnAdd;
    private Button                    btnDel;
    private TableViewer               tvVars;

    protected EnvWizardPage(String pageName, WorkflowTool oldTool) {
        super(pageName);
        setTitle(pageName);
        setMessage("Add and edit environment variables.");
        setPageComplete(true);
        env = new HashMap<String, String>();

        if (oldTool != null) {
            try {
                env.putAll(new ObjectMapper().readValue(oldTool.getImplementation(), CommandLineImplementation.class).getEnv());
            } catch (IOException e) {
                logger.error("Could not parse old env variable.", e);
            }
        }
    }

    public void createControl(Composite parent) {
        final Composite top = new Composite(parent, SWT.NONE);
        top.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        top.setLayout(new GridLayout(2, false));

        tvVars = new TableViewer(top, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        tvVars.setUseHashlookup(true);
        Table table = tvVars.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        // create columns
        TableViewerColumn column = new TableViewerColumn(tvVars, SWT.NONE);
        column.getColumn().setMoveable(true);
        column.getColumn().setText("Variable");
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return getEntry(element).getKey();
            }
        });

        column = new TableViewerColumn(tvVars, SWT.NONE);
        column.getColumn().setMoveable(true);
        column.getColumn().setText("Value");
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return getEntry(element).getValue();
            }
        });

        // layout columns
        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(40, 75, true));
        layout.addColumnData(new ColumnWeightData(60, 75, true));
        table.setLayout(layout);

        // editors
        CellEditor[] editors = new CellEditor[2];
        editors[0] = new TextCellEditor(table);
        editors[1] = new TextCellEditor(table);
        tvVars.setCellEditors(editors);
        tvVars.setCellModifier(new ICellModifier() {
            public void modify(Object element, String property, Object value) {
                Entry<String, String> entry = getEntry(element);
                if (property.equals("variable")) { //$NON-NLS-1$
                    env.remove(entry.getKey());
                    env.put(value.toString(), entry.getValue());
                    tvVars.refresh();
                }
                if (property.equals("value")) { //$NON-NLS-1$
                    env.put(entry.getKey(), value.toString());
                    tvVars.refresh();
                }
            }

            public Object getValue(Object element, String property) {
                if (property.equals("variable")) { //$NON-NLS-1$
                    return getEntry(element).getKey();
                }

                if (property.equals("value")) { //$NON-NLS-1$
                    return getEntry(element).getValue();
                }

                return null;
            }

            public boolean canModify(Object element, String property) {
                return true;
            }
        });
        tvVars.setColumnProperties(new String[] { "variable", "value" }); //$NON-NLS-1$ //$NON-NLS-2$
        tvVars.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                btnDel.setEnabled(!event.getSelection().isEmpty());
            }
        });

        // contentprovider and initial input
        tvVars.setContentProvider(new IStructuredContentProvider() {
            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

            @Override
            public void dispose() {}

            @Override
            public Object[] getElements(Object inputElement) {
                ArrayList<Entry<String, String>> list = new ArrayList<Entry<String, String>>(env.entrySet());
                Collections.sort(list, new Comparator<Entry<String, String>>() {
                    @Override
                    public int compare(Entry<String, String> arg0, Entry<String, String> arg1) {
                        if (arg0.getKey().equals(arg1.getKey())) {
                            return arg0.getValue().compareTo(arg1.getValue());
                        } else {
                            return arg0.getKey().compareTo(arg1.getKey());
                        }
                    }
                });
                return list.toArray();
            }
        });
        tvVars.setInput(env);

        // buttons
        Composite buttons = new Composite(top, SWT.NULL);
        buttons.setLayout(new GridLayout());

        btnAdd = new Button(buttons, SWT.PUSH);
        btnAdd.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        btnAdd.setText("Add...");
        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!env.containsKey("VARIABLE")) {
                    env.put("VARIABLE", "VALUE");
                }
                tvVars.refresh();
            }
        });

        btnDel = new Button(buttons, SWT.PUSH);
        btnDel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        btnDel.setText("Remove");
        btnDel.setEnabled(false);
        btnDel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ISelection sel = tvVars.getSelection();
                if (sel.isEmpty() || !(sel instanceof IStructuredSelection)) {
                    return;
                }
                env.remove(((IStructuredSelection) sel).getFirstElement());
                tvVars.refresh();
            }
        });

        setControl(top);
    }

    @SuppressWarnings("unchecked")
    private Entry<String, String> getEntry(Object element) {
        if (element instanceof Item) {
            element = ((Item) element).getData();
        }
        if (element instanceof Entry<?, ?>) {
            return (Entry<String, String>) element;
        }
        return null;
    }

    public void updateTool(WorkflowTool tool) throws IOException {
        CommandLineImplementation impl = new ObjectMapper().readValue(tool.getImplementation(), CommandLineImplementation.class);
        impl.setEnv(env);
        tool.setImplementation(new ObjectMapper().writeValueAsString(impl));
    }
}