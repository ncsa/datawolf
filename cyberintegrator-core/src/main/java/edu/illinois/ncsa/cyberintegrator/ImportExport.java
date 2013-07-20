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

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.LogFile;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.LogFileDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.FileDescriptorDAO;
import edu.illinois.ncsa.springdata.FileStorage;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class ImportExport {
    private static Logger       logger         = LoggerFactory.getLogger(ImportExport.class);

    private static final String WORKFLOW_FILE  = "workflow.json";
    private static final String EXECUTION_FILE = "execution.json";
    private static final String STEP_FILE      = "step.json";
    private static final String DATASET_FILE   = "dataset.json";

    private static final String BLOBS_FOLDER   = "blobs";
    private static final String LOGS_FOLDER    = "logs";

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
            t.start(true);
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
        } catch (Exception e) {
            logger.warn("Error saving workflow.", e);
        } finally {
            if (zipfile != null) {
                zipfile.close();
            }
            t.rollback();
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
                FileDescriptor blob = null;
                if (fdDAO.exists(pieces[1])) {
                    logger.info("Already have blob with id : " + pieces[1]);
                    blob = fdDAO.findOne(pieces[1]);
                } else {
                    // save blob
                    InputStream is = zipfile.getInputStream(entry);
                    blob = fs.storeFile(pieces[1], pieces[2], is);
                    is.close();
                }

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
            t = null;

            return workflow;
        } catch (Exception exc) {
            try {
                if (zipfile != null) {
                    zipfile.close();
                }
            } catch (Exception e1) {
                logger.error("Could not close zipfile.", e1);
            }
            try {
                t.rollback();
            } catch (Exception e1) {
                logger.error("Could not rollback transaction.", e1);
            }
            throw exc;
        }

    }

    /**
     * Exports the given execution to a zip file. The complete execution will be
     * exported as a JSON object, including all outputs generated during the
     * execution
     * 
     * @param file
     *            the zipfile where the execution will be saved, this file will
     *            be overwritten.
     * @param executionId
     *            the id of the exection to export.
     * @throws Exception
     *             an exception is thrown if the execution could not be saved.
     */
    public static void exportExecution(File file, String executionId) throws Exception {
        // create zipfile
        ZipOutputStream zipfile = null;

        // create transaction
        Transaction t = SpringData.getTransaction();
        try {
            t.start(true);
            Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(executionId);

            zipfile = new ZipOutputStream(new FileOutputStream(file));

            // export dataset
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // this will close the outputstream, hence the byte array
            ObjectMapper mapper = new ObjectMapper();
            final JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(baos);
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            mapper.writeValue(jsonGenerator, execution);

            zipfile.putNextEntry(new ZipEntry(EXECUTION_FILE));
            zipfile.write(baos.toByteArray());
            zipfile.closeEntry();

            // export blobs
            Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();
            FileStorage fs = SpringData.getFileStorage();
            byte[] buf = new byte[10240];
            int len = 0;
            Set<String> datasets = new HashSet<String>(execution.getDatasets().values());
            for (String datasetId : datasets) {
                Dataset dataset = SpringData.getBean(DatasetDAO.class).findOne(datasetId);
                for (FileDescriptor fd : dataset.getFileDescriptors()) {
                    if (!blobs.contains(fd)) {
                        blobs.add(fd);

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

            // export logfiles
            for (LogFile logfile : SpringData.getBean(LogFileDAO.class).findByExecutionId(execution.getId())) {
                FileDescriptor fd = logfile.getLog();
                if (!blobs.contains(fd)) {
                    blobs.add(fd);

                    zipfile.putNextEntry(new ZipEntry(String.format("%s/%s/%s", LOGS_FOLDER, fd.getId(), fd.getFilename())));
                    InputStream is = fs.readFile(fd);
                    while ((len = is.read(buf)) > 0) {
                        zipfile.write(buf, 0, len);
                    }
                    is.close();
                    zipfile.closeEntry();
                }
            }

        } finally {
            if (zipfile != null) {
                zipfile.close();
            }
            t.rollback();
        }
    }

    /**
     * Exports the given step to a zip file. The complete step will be
     * exported as a JSON object, including all outputs generated during the
     * specific execution.
     * 
     * @param file
     *            the zipfile where the step will be saved, this file will
     *            be overwritten.
     * @param stepId
     *            the id of the step to export.
     * @param executionId
     *            the id of the exection to find the data to export.
     * @throws Exception
     *             an exception is thrown if the step could not be saved.
     */
    public static void exportStep(File file, String stepId, String executionId) throws Exception {
        // create zipfile
        ZipOutputStream zipfile = null;

        // create transaction
        Transaction t = SpringData.getTransaction();
        try {
            t.start(true);
            WorkflowStep step = SpringData.getBean(WorkflowStepDAO.class).findOne(stepId);

            zipfile = new ZipOutputStream(new FileOutputStream(file));

            // export dataset
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // this will close the outputstream, hence the byte array
            ObjectMapper mapper = new ObjectMapper();
            final JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(baos);
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            mapper.writeValue(jsonGenerator, step);

            zipfile.putNextEntry(new ZipEntry(STEP_FILE));
            zipfile.write(baos.toByteArray());
            zipfile.closeEntry();

            // export blobs
            if (executionId != null) {
                Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(executionId);
                Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();
                FileStorage fs = SpringData.getFileStorage();
                byte[] buf = new byte[10240];
                int len = 0;
                Set<String> datasets = new HashSet<String>();
                for (WorkflowToolData tooldata : step.getTool().getOutputs()) {
                    if (execution.getDataset(tooldata.getId()) != null) {
                        datasets.add(execution.getDataset(tooldata.getDataId()));
                    }
                }
                for (String datasetId : datasets) {
                    Dataset dataset = SpringData.getBean(DatasetDAO.class).findOne(datasetId);
                    for (FileDescriptor fd : dataset.getFileDescriptors()) {
                        if (!blobs.contains(fd)) {
                            blobs.add(fd);

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

                LogFile logfile = SpringData.getBean(LogFileDAO.class).findLogByExecutionIdAndStepId(execution.getId(), step.getId());
                FileDescriptor fd = logfile.getLog();
                if (!blobs.contains(fd)) {
                    blobs.add(fd);

                    zipfile.putNextEntry(new ZipEntry(String.format("%s/%s/%s", LOGS_FOLDER, fd.getId(), fd.getFilename())));
                    InputStream is = fs.readFile(fd);
                    while ((len = is.read(buf)) > 0) {
                        zipfile.write(buf, 0, len);
                    }
                    is.close();
                    zipfile.closeEntry();
                }

            }

        } finally {
            if (zipfile != null) {
                zipfile.close();
            }
            t.rollback();
        }
    }

    /**
     * Exports the given dataset to a zip file. The complete dataset will be
     * exported as a JSON object, including blobs.
     * 
     * @param file
     *            the zipfile where the dataset will be saved, this file will
     *            be overwritten.
     * @param datasetId
     *            the id of the dataset to export.
     * @throws Exception
     *             an exception is thrown if the dataset could not be saved.
     */
    public static void exportDataset(File file, String datasetId) throws Exception {
        // create zipfile
        ZipOutputStream zipfile = null;

        // create transaction
        Transaction t = SpringData.getTransaction();
        try {
            t.start(true);
            Dataset dataset = SpringData.getBean(DatasetDAO.class).findOne(datasetId);

            zipfile = new ZipOutputStream(new FileOutputStream(file));

            // export dataset
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // this will close the outputstream, hence the byte array
            ObjectMapper mapper = new ObjectMapper();
            final JsonGenerator jsonGenerator = mapper.getJsonFactory().createJsonGenerator(baos);
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            mapper.writeValue(jsonGenerator, dataset);

            zipfile.putNextEntry(new ZipEntry(DATASET_FILE));
            zipfile.write(baos.toByteArray());
            zipfile.closeEntry();

            // export blobs
            Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();
            FileStorage fs = SpringData.getFileStorage();
            byte[] buf = new byte[10240];
            int len = 0;
            for (FileDescriptor fd : dataset.getFileDescriptors()) {
                if (!blobs.contains(fd)) {
                    blobs.add(fd);

                    zipfile.putNextEntry(new ZipEntry(String.format("%s/%s/%s", BLOBS_FOLDER, fd.getId(), fd.getFilename())));
                    InputStream is = fs.readFile(fd);
                    while ((len = is.read(buf)) > 0) {
                        zipfile.write(buf, 0, len);
                    }
                    is.close();
                    zipfile.closeEntry();
                }
            }

        } finally {
            if (zipfile != null) {
                zipfile.close();
            }
            t.rollback();
        }
    }

    /**
     * Imports the dataset from the zipfile. The blobs associated with the
     * dataset will be imported. This will return the dataset imported after it
     * is persisted.
     * 
     * @param file
     *            the zipfile where the dataset (json) will be saved, this file
     *            will be overwritten.
     * @return the persisted dataset.
     * @throws Exception
     *             an exception is thrown if the dataset could not be saved.
     */
    public static Dataset importDataset(File file) throws Exception {
        // create zipfile
        ZipFile zipfile = null;

        // create transaction
        Transaction t = SpringData.getTransaction();
        try {
            t.start();

            zipfile = new ZipFile(file);

            // get workflow
            Dataset dataset = new ObjectMapper().readValue(zipfile.getInputStream(zipfile.getEntry(DATASET_FILE)), Dataset.class);
            DatasetDAO datasetDAO = SpringData.getBean(DatasetDAO.class);
            if (datasetDAO.exists(dataset.getId())) {
                throw (new Exception("Dataset already imported."));
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
                for (FileDescriptor fd : dataset.getFileDescriptors()) {
                    if (fd.getId().equals(pieces[1])) {
                        fd.setDataURL(blob.getDataURL());
                    }
                }
            }

            // done with zipfile
            zipfile.close();
            zipfile = null;

            // save dataset
            dataset = datasetDAO.save(dataset);
            t.commit();

            return dataset;
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

    /**
     * Exports the given logfile to a textfile.
     * 
     * @param file
     *            the textfile where the log will be saved, this file will
     *            be overwritten.
     * @param logfileId
     *            the id of the workflow to export.
     * @throws Exception
     *             an exception is thrown if the logfile could not be saved.
     */
    public static void exportLogfile(File file, String logfileId) throws Exception {
        // create transaction
        Transaction t = SpringData.getTransaction();
        try {
            t.start(true);
            LogFile logfile = SpringData.getBean(LogFileDAO.class).findOne(logfileId);

            FileDescriptor fd = logfile.getLog();
            FileStorage fileStorage = SpringData.getFileStorage();

            FileOutputStream output = new FileOutputStream(file);
            InputStream input = fileStorage.readFile(fd);
            byte[] buf = new byte[10240];
            int len;
            while ((len = input.read(buf)) > 0) {
                output.write(buf, 0, len);
            }
            input.close();
            output.close();
        } finally {
            t.rollback();
        }
    }
}
