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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineImplementation;
import edu.illinois.ncsa.cyberintegrator.tool.creator.Wizard;
import edu.illinois.ncsa.cyberintegrator.tool.creator.WizardPage;

public class EnvWizardPage extends WizardPage {
    private static Logger logger = LoggerFactory.getLogger(EnvWizardPage.class);

    private JButton       btnAdd;
    private JButton       btnDel;
    private JTable        tblVars;
    private EnvModel      model;

    protected EnvWizardPage(Wizard wizard, WorkflowTool oldTool) {
        super(wizard);
        setStepComplete(true);

        add(new JLabel("ENV"), 0, 0);

        model = new EnvModel();
        tblVars = new JTable(model);
        tblVars.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                checkUI();
            }
        });
        add(new JScrollPane(tblVars), 1, 0, GridBagConstraints.BOTH);

        // buttons
        JPanel buttons = new JPanel(new GridLayout(0, 1));
        add(buttons, 2, 0);

        btnAdd = new JButton("Add...");
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (!model.getMap().containsKey("NEW")) {
                    model.add("NEW", "VALUE");
                }
                checkUI();
            }
        });
        buttons.add(btnAdd);

        btnDel = new JButton("Remove");
        btnDel.setEnabled(false);
        btnDel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                for (int idx : tblVars.getSelectedRows()) {
                    model.remove(idx);
                }
                checkUI();
            }
        });
        buttons.add(btnDel);

        if (oldTool != null) {
            try {
                for (Entry<String, String> entry : new ObjectMapper().readValue(oldTool.getImplementation(), CommandLineImplementation.class).getEnv().entrySet()) {
                    model.add(entry.getKey(), entry.getValue());
                }
            } catch (IOException e) {
                logger.error("Could not parse old env variable.", e);
            }
        }

        checkUI();
    }

    private void checkUI() {
        btnDel.setEnabled(tblVars.getSelectedRows().length != 0);
    }

    public void updateTool(WorkflowTool tool) throws IOException {
        CommandLineImplementation impl = new ObjectMapper().readValue(tool.getImplementation(), CommandLineImplementation.class);
        impl.setEnv(model.getMap());
        tool.setImplementation(new ObjectMapper().writeValueAsString(impl));
    }

    class EnvModel extends AbstractTableModel {
        private List<String> keys = new ArrayList<String>();
        private List<String> vals = new ArrayList<String>();

        public void add(String key, String val) {
            int idx = keys.indexOf(key);
            if (idx == -1) {
                keys.add(key);
                vals.add(val);
                fireTableRowsInserted(keys.size() - 1, keys.size() - 1);
            } else {
                vals.set(idx, val);
                fireTableCellUpdated(idx, 1);
            }
        }

        public void remove(String key) {
            int idx = keys.indexOf(key);
            if (idx != -1) {
                keys.remove(idx);
                vals.remove(idx);
                fireTableRowsDeleted(idx, idx);
            }
        }

        public void remove(int idx) {
            keys.remove(idx);
            vals.remove(idx);
            fireTableRowsDeleted(idx, idx);
        }

        public Map<String, String> getMap() {
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), vals.get(i));
            }
            return map;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "Variable";
            } else if (column == 1) {
                return "Value";
            } else {
                return "";
            }
        }

        @Override
        public int getRowCount() {
            return keys.size();
        }

        @Override
        public Object getValueAt(int arg0, int arg1) {
            if (arg1 == 0) {
                return keys.get(arg0);
            } else if (arg1 == 1) {
                return vals.get(arg0);
            } else {
                return "";
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                keys.set(rowIndex, aValue.toString());
            } else if (columnIndex == 1) {
                vals.set(rowIndex, aValue.toString());
            }
        }
    }
}