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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.DatasetDAO;

@Component
public class Main extends JFrame {
    @Autowired
    private DatasetDAO datasetDAO;

    private Person     user;

    public Main() {
        super("Cyberintegrator 3.0");

        user = new Person();
        user.setName("Rob", "Kooper");

        final DatasetTableModel datasetTableModel = new DatasetTableModel();
        final JTable datasetList = new JTable(datasetTableModel);
        add(BorderLayout.WEST, new JScrollPane(datasetList));

        datasetList.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        Dataset dataset = new Dataset();
                        dataset.setDate(new Date());
                        dataset.setCreator(user);

                        FileDescriptor blob = new FileDescriptor();
                        blob.setFilename(file.getName());
                        blob.setMimeType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file));
                        blob.setSize(file.length());

                        datasetDAO.save(dataset);
                    }

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
                return dataset.getBlobs().get(0).getSize();
            }
            return "N/A";
        }

    }

    public static void main(String[] args) {
        // get hold of the repo
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        Main main = ctx.getBean(Main.class);
        main.setPreferredSize(new Dimension(800, 600));
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.pack();
        main.setVisible(true);
    }
}
