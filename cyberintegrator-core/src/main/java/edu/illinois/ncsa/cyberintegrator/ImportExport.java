package edu.illinois.ncsa.cyberintegrator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.FileDescriptorDAO;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class ImportExport {
    private static Logger       logger        = LoggerFactory.getLogger(ImportExport.class);

    private static final String WORKFLOW_FILE = "workflow.json";
    private static final String BLOBS_FOLDER  = "blobs";

    /**
     * Exports the given workflow to a zip file. The complete workflow will be
     * exported as a JSON object, including people definitions as well as tools.
     * The blobs associated with the tools will be exported as well.
     * 
     * @param file
     *            the zipfile where the workflow will be saved, this file will
     *            be overwritten.
     * @param workflowId
     *            the id of the workflow to export.
     * @throws Exception
     *             an exception is thrown if the workflow could not be saved.
     */
    public static void exportWorkflow(File file, String workflowId) throws Exception {
        // create zipfile
        ZipOutputStream zipfile = null;

        // create transaction
        Transaction t = SpringData.getTransaction();
        try {
            t.start();
            Workflow workflow = SpringData.getBean(WorkflowDAO.class).findOne(workflowId);

            zipfile = new ZipOutputStream(new FileOutputStream(file));

            // export workflow
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // this will close the outputstream, hence the byte array
            ObjectMapper mapper = new ObjectMapper();
            final JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(baos);
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            mapper.writeValue(jsonGenerator, workflow);

            zipfile.putNextEntry(new ZipEntry(WORKFLOW_FILE));
            zipfile.write(baos.toByteArray());
            zipfile.closeEntry();

            // export blobs
            Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();
            FileStorage fs = SpringData.getFileStorage();
            byte[] buf = new byte[10240];
            int len = 0;
            for (WorkflowStep step : workflow.getSteps()) {
                for (FileDescriptor fd : step.getTool().getBlobs()) {
                    if (!blobs.contains(fd)) {
                        blobs.add(fd);
                        // TODO RK : hack to convert chris workflow
                        if (fd.getDataURL().startsWith("file:/home/cnavarro/data/files")) {
                            fd.setDataURL(fd.getDataURL().replace("/home/cnavarro/data/files", fs.getFolder()));
                        }

                        zipfile.putNextEntry(new ZipEntry(String.format("%s/%s/%s", BLOBS_FOLDER, fd.getId(), fd.getFilename())));
                        InputStream is = fs.readFile(fd);
                        while ((len = is.read(buf)) > 0) {
                            zipfile.write(buf, 0, len);
                        }
                        is.close();
                        zipfile.closeEntry();
                    }
                }
            }

        } finally {
            if (zipfile != null) {
                zipfile.close();
            }
            t.commit();
        }
    }

    /**
     * Imports the workflow from the zipfile. The complete workflow will be
     * imported, including people definitions as well as tools.
     * The blobs associated with the tools will be imported as well. This will
     * return the workflow imported after it is persisted.
     * 
     * @param file
     *            the zipfile where the workflow will be saved, this file will
     *            be overwritten.
     * @return the persisted workflow.
     * @throws Exception
     *             an exception is thrown if the workflow could not be saved.
     */
    public static Workflow importWorkflow(File file) throws Exception {
        // create zipfile
        ZipFile zipfile = null;

        // create transaction
        Transaction t = SpringData.getTransaction();
        try {
            t.start();

            zipfile = new ZipFile(file);

            // get workflow
            Workflow workflow = new ObjectMapper().readValue(zipfile.getInputStream(zipfile.getEntry(WORKFLOW_FILE)), Workflow.class);
            WorkflowDAO workflowDAO = SpringData.getBean(WorkflowDAO.class);
            if (workflowDAO.exists(workflow.getId())) {
                throw (new Exception("Workflow already imported."));
            }

            // import blobs
            FileStorage fs = SpringData.getFileStorage();
            FileDescriptorDAO fdDAO = SpringData.getBean(FileDescriptorDAO.class);
            Enumeration<? extends ZipEntry> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith(BLOBS_FOLDER)) {
                    continue;
                }
                String[] pieces = entry.getName().split("/");
                if (fdDAO.exists(pieces[1])) {
                    logger.info("Already have blob with id : " + pieces[1]);
                    continue;
                }

                // save blob
                InputStream is = zipfile.getInputStream(entry);
                FileDescriptor blob = fs.storeFile(pieces[1], pieces[2], is);
                is.close();

                // fix workflow
                for (WorkflowStep step : workflow.getSteps()) {
                    for (FileDescriptor fd : step.getTool().getBlobs()) {
                        if (fd.getId().equals(pieces[1])) {
                            fd.setDataURL(blob.getDataURL());
                        }
                    }
                }
            }

            // done with zipfile
            zipfile.close();
            zipfile = null;

            // save workflow
            workflow = workflowDAO.save(workflow);
            t.commit();

            return workflow;
        } catch (Exception exc) {
            try {
                if (zipfile != null) {
                    zipfile.close();
                }
                t.rollback();
            } catch (Exception e1) {
                logger.error("Could not rollback transaction.", e1);
            }
            throw exc;
        }

    }
}
