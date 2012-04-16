package edu.illinois.ncsa.cyberintegrator.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.Date;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.SpringData;

public class Main extends JFrame {
    private Person     user;
    private DatasetDAO datasetDAO;

    public Main() {
        super("Cyberintegrator 3.0");

        datasetDAO = SpringData.getBean(DatasetDAO.class);

        user = new Person();
        user.setName("Rob", "Kooper");

        final DatasetTableModel datasetTableModel = new DatasetTableModel();
        final JTable datasetTable = new JTable(datasetTableModel);
        JScrollPane scrollPane = new JScrollPane(datasetTable);
        add(BorderLayout.WEST, scrollPane);

        scrollPane.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {

                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        Dataset dataset = new Dataset();
                        dataset.setTitle(file.getName());
                        dataset.setDate(new Date());
                        dataset.setCreator(user);

                        FileDescriptor fd = new FileDescriptor();
                        fd.setFilename(file.getName());
                        fd.setMimeType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file));
                        fd.setSize(file.length());
                        fd.setDataURL(file.toURI().toURL());
                        dataset.addFileDescriptor(fd);

                        datasetDAO.save(dataset);
                    }
                    evt.dropComplete(true);
                    datasetTableModel.fireTableDataChanged();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    class DatasetTableModel extends AbstractTableModel {
        private String[] columns = new String[] { "Title", "Date", "Size" };

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return String.class;

            case 1:
                return Date.class;

            case 2:
                return Long.class;

            default:
                return String.class;
            }
        }

        @Override
        public int getRowCount() {
            return (int) datasetDAO.count();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Dataset dataset = datasetDAO.findAll(new PageRequest(rowIndex, 1, new Sort("title"))).getContent().get(0);
            switch (columnIndex) {
            case 0:
                return dataset.getTitle();
            case 1:
                return dataset.getDate();
            case 2:
                if (dataset.getFileDescriptors().size() > 0) {
                    return dataset.getFileDescriptors().get(0).getSize();
                } else {
                    return -1;
                }
            }
            return "N/A";
        }

    }

    public static void main(String[] args) {
        // get hold of the repo
        SpringData.loadXMLContext("applicationContext.xml");

        Main main = new Main();
        main.setPreferredSize(new Dimension(800, 600));
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.pack();
        main.setVisible(true);
    }
}
