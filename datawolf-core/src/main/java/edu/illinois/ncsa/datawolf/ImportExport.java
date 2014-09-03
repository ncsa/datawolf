package edu.illinois.ncsa.datawolf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.LogFile;
import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.datawolf.domain.dao.LogFileDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowToolDao;
import edu.illinois.ncsa.datawolf.springdata.LogFileDAO;
import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;
import edu.illinois.ncsa.domain.dao.PersonDao;
import edu.illinois.ncsa.springdata.SpringData;

public class ImportExport {
    private static Logger            logger         = LoggerFactory.getLogger(ImportExport.class);

    private static final String      WORKFLOW_FILE  = "workflow.json";
    private static final String      TOOL_FILE      = "tool.json";
    private static final String      EXECUTION_FILE = "execution.json";
    private static final String      STEP_FILE      = "step.json";
    private static final String      DATASET_FILE   = "dataset.json";

    private static final String      BLOBS_FOLDER   = "blobs";
    private static final String      LOGS_FOLDER    = "logs";

    // TODO where should these dao's come from?
    @Inject
    private static WorkflowDao       workflowDao;

    @Inject
    private static WorkflowToolDao   workflowToolDao;

    @Inject
    private static WorkflowStepDao   workflowStepDao;

    @Inject
    private static PersonDao         personDao;

    @Inject
    private static FileStorage       fileStorage;

    @Inject
    private static FileDescriptorDao fileDescriptorDao;

    @Inject
    private static LogFileDao        logFileDao;

    @Inject
    private static DatasetDao        datasetDao;

    @Inject
    private static ExecutionDao      executionDao;

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
        // Transaction t = SpringData.getTransaction();
        try {
            // t.start(true);
            Workflow workflow = workflowDao.findOne(workflowId);

            zipfile = new ZipOutputStream(new FileOutputStream(file));

            // convert the workflow to JSON
            zipfile.putNextEntry(new ZipEntry(WORKFLOW_FILE));
            zipfile.write(SpringData.objectToJSON(workflow).getBytes("UTF-8"));
            zipfile.closeEntry();

            // export blobs
            Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();
            byte[] buf = new byte[10240];
            int len = 0;
            for (WorkflowStep step : workflow.getSteps()) {
                for (FileDescriptor fd : step.getTool().getBlobs()) {
                    if (!blobs.contains(fd)) {
                        blobs.add(fd);

                        zipfile.putNextEntry(new ZipEntry(String.format("%s/%s/%s", BLOBS_FOLDER, fd.getId(), fd.getFilename())));
                        InputStream is = fileStorage.readFile(fd);
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
            // t.rollback();
        }
    }

    /**
     * Check the bean and replace all people with people already stored based on
     * their email addresses. This function is recursive and assumes to be
     * called inside a transaction.
     * 
     * @param bean
     *            the bean to be checked.
     */
    private static void fixPeople(AbstractBean bean) {
        // check to see if bean is person, if so check if we need to swap
        if (bean instanceof Person) {
            Person p1 = (Person) bean;
            Person p2 = personDao.findOne(p1.getId());
            if (p2 != null) {
                return;
            }
            p2 = personDao.findByEmail(((Person) bean).getEmail());
            if (p2 != null) {
                p1.setId(p2.getId());
                p1.setEmail(p2.getEmail());
                p1.setFirstName(p2.getFirstName());
                p1.setLastName(p2.getLastName());
                return;
            }
            return;
        }

        // check all subfields
        for (Field field : bean.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                fixPeople(field.get(bean));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void fixPeople(Object obj) {
        if (obj == null) {
            return;
        }
        if (obj instanceof AbstractBean) {
            fixPeople((AbstractBean) obj);
        } else if (obj instanceof Collection<?>) {
            for (Object o : (Collection<?>) obj) {
                fixPeople(o);
            }
        } else if (obj instanceof Map<?, ?>) {
            for (Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                fixPeople(entry.getKey());
                fixPeople(entry.getValue());
            }
        } else if (obj.getClass().isArray()) {
            for (Object o : Arrays.asList(obj)) {
                fixPeople(o);
            }
        }
    }

    /**
     * Imports the workflow from the zipfile. The complete workflow will be
     * imported, including people definitions as well as tools.
     * The blobs associated with the tools will be imported as well. This will
     * return the workflow imported after it is persisted.
     * 
     * @param file
     *            the zipfile where the workflow is saved.
     * @return the persisted workflow.
     * @throws Exception
     *             an exception is thrown if the workflow could not be loaded.
     */
    public static Workflow importWorkflow(File file) throws Exception {
        // create zipfile
        ZipFile zipfile = null;

        // create transaction
        // Transaction t = SpringData.getTransaction();
        try {
            // t.start();

            zipfile = new ZipFile(file);

            // get workflow
            Workflow workflow = SpringData.JSONToObject(zipfile.getInputStream(zipfile.getEntry(WORKFLOW_FILE)), Workflow.class);
            fixPeople(workflow);

            // WorkflowDAO workflowDAO = SpringData.getBean(WorkflowDAO.class);
            if (workflowDao.exists(workflow.getId())) {
                throw (new Exception("Workflow already imported."));
            }

            // import blobs
            Enumeration<? extends ZipEntry> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith(BLOBS_FOLDER)) {
                    continue;
                }
                String[] pieces = entry.getName().split("/");
                FileDescriptor blob = null;
                if (fileDescriptorDao.exists(pieces[1])) {
                    logger.info("Already have blob with id : " + pieces[1]);
                    blob = fileDescriptorDao.findOne(pieces[1]);
                } else {
                    // save blob
                    InputStream is = zipfile.getInputStream(entry);
                    blob = fileStorage.storeFile(pieces[1], pieces[2], is);
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
            workflowDao.save(workflow);
            // t.commit();
            // t = null;

            return workflow;
        } catch (Exception exc) {
            try {
                if (zipfile != null) {
                    zipfile.close();
                }
            } catch (Exception e1) {
                logger.error("Could not close zipfile.", e1);
            }
            // try {
            // t.rollback();
            // } catch (Exception e1) {
            // logger.error("Could not rollback transaction.", e1);
            // }
            throw exc;
        }
    }

    /**
     * Exports the given tool to a zip file. The complete tool will be
     * exported as a JSON object, including people definitions.
     * The blobs associated with the tools will be exported as well.
     * 
     * @param file
     *            the zipfile where the tool will be saved, this file will
     *            be overwritten.
     * @param toolId
     *            the id of the tool to export.
     * @throws Exception
     *             an exception is thrown if the tool could not be saved.
     */
    public static void exportTool(File file, String toolId) throws Exception {
        // create zipfile
        ZipOutputStream zipfile = null;

        // create transaction
        // Transaction t = SpringData.getTransaction();
        try {
            // t.start(true);
            WorkflowTool tool = workflowToolDao.findOne(toolId);

            zipfile = new ZipOutputStream(new FileOutputStream(file));

            // convert the tool to json and save it.
            zipfile.putNextEntry(new ZipEntry(TOOL_FILE));
            zipfile.write(SpringData.objectToJSON(tool).getBytes("UTF-8"));
            zipfile.closeEntry();

            // export blobs
            Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();
            byte[] buf = new byte[10240];
            int len = 0;
            for (FileDescriptor fd : tool.getBlobs()) {
                if (!blobs.contains(fd)) {
                    blobs.add(fd);

                    zipfile.putNextEntry(new ZipEntry(String.format("%s/%s/%s", BLOBS_FOLDER, fd.getId(), fd.getFilename())));
                    InputStream is = fileStorage.readFile(fd);
                    while ((len = is.read(buf)) > 0) {
                        zipfile.write(buf, 0, len);
                    }
                    is.close();
                    zipfile.closeEntry();
                }
            }
        } catch (Exception e) {
            logger.warn("Error saving workflow.", e);
        } finally {
            if (zipfile != null) {
                zipfile.close();
            }
            // t.rollback();
        }
    }

    /**
     * Imports the tool from the zipfile. The complete tool will be
     * imported, including people definitions.
     * The blobs associated with the tool will be imported as well. This will
     * return the tool imported after it is persisted.
     * 
     * @param file
     *            the zipfile where the tool is saved
     * @return the persisted tool.
     * @throws Exception
     *             an exception is thrown if the tool could not be loaded.
     */
    public static WorkflowTool importTool(File file) throws Exception {
        // create zipfile
        ZipFile zipfile = null;

        // create transaction
        // Transaction t = SpringData.getTransaction();
        try {
            // t.start();

            zipfile = new ZipFile(file);

            // get workflow
            WorkflowTool tool = SpringData.JSONToObject(zipfile.getInputStream(zipfile.getEntry(TOOL_FILE)), WorkflowTool.class);
            fixPeople(tool);

            if (workflowToolDao.exists(tool.getId())) {
                throw (new Exception("tool already imported."));
            }

            // import blobs
            // FileStorage fs = SpringData.getFileStorage();
            // FileDescriptorDAO fdDAO =
// SpringData.getBean(FileDescriptorDAO.class);
            Enumeration<? extends ZipEntry> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith(BLOBS_FOLDER)) {
                    continue;
                }
                String[] pieces = entry.getName().split("/");
                FileDescriptor blob = null;
                if (fileDescriptorDao.exists(pieces[1])) {
                    logger.info("Already have blob with id : " + pieces[1]);
                    blob = fileDescriptorDao.findOne(pieces[1]);
                } else {
                    // save blob
                    InputStream is = zipfile.getInputStream(entry);
                    blob = fileStorage.storeFile(pieces[1], pieces[2], is);
                    is.close();
                }

                // fix workflow
                for (FileDescriptor fd : tool.getBlobs()) {
                    if (fd.getId().equals(pieces[1])) {
                        fd.setDataURL(blob.getDataURL());
                    }
                }
            }

            // done with zipfile
            zipfile.close();
            zipfile = null;

            // save workflow
            workflowToolDao.save(tool);
            // t.commit();
            // t = null;

            return tool;
        } catch (Exception exc) {
            try {
                if (zipfile != null) {
                    zipfile.close();
                }
            } catch (Exception e1) {
                logger.error("Could not close zipfile.", e1);
            }
            // try {
            // t.rollback();
            // } catch (Exception e1) {
            // logger.error("Could not rollback transaction.", e1);
            // }
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
        // Transaction t = SpringData.getTransaction();
        try {
            // t.start(true);
            Execution execution = executionDao.findOne(executionId);

            zipfile = new ZipOutputStream(new FileOutputStream(file));

            // convert the execution to JSON
            zipfile.putNextEntry(new ZipEntry(EXECUTION_FILE));
            zipfile.write(SpringData.objectToJSON(execution).getBytes("UTF-8"));
            zipfile.closeEntry();

            // export blobs
            Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();
            byte[] buf = new byte[10240];
            int len = 0;
            Set<String> datasets = new HashSet<String>(execution.getDatasets().values());
            for (String datasetId : datasets) {
                Dataset dataset = datasetDao.findOne(datasetId);
                for (FileDescriptor fd : dataset.getFileDescriptors()) {
                    if (!blobs.contains(fd)) {
                        blobs.add(fd);

                        zipfile.putNextEntry(new ZipEntry(String.format("%s/%s/%s", BLOBS_FOLDER, fd.getId(), fd.getFilename())));
                        InputStream is = fileStorage.readFile(fd);
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
                    InputStream is = fileStorage.readFile(fd);
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
            // t.rollback();
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
        // Transaction t = SpringData.getTransaction();
        try {
            // t.start(true);
            WorkflowStep step = workflowStepDao.findOne(stepId);

            zipfile = new ZipOutputStream(new FileOutputStream(file));

            // this will export the step to JSON
            zipfile.putNextEntry(new ZipEntry(STEP_FILE));
            zipfile.write(SpringData.objectToJSON(step).getBytes("UTF-8"));
            zipfile.closeEntry();

            // export blobs
            if (executionId != null) {
                Execution execution = executionDao.findOne(executionId);
                Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();
                byte[] buf = new byte[10240];
                int len = 0;
                Set<String> datasets = new HashSet<String>();
                for (WorkflowToolData tooldata : step.getTool().getOutputs()) {
                    if (execution.getDataset(tooldata.getId()) != null) {
                        datasets.add(execution.getDataset(tooldata.getDataId()));
                    }
                }
                for (String datasetId : datasets) {
                    Dataset dataset = datasetDao.findOne(datasetId);
                    for (FileDescriptor fd : dataset.getFileDescriptors()) {
                        if (!blobs.contains(fd)) {
                            blobs.add(fd);

                            zipfile.putNextEntry(new ZipEntry(String.format("%s/%s/%s", BLOBS_FOLDER, fd.getId(), fd.getFilename())));
                            InputStream is = fileStorage.readFile(fd);
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
                    InputStream is = fileStorage.readFile(fd);
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
            // t.rollback();
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
        // Transaction t = SpringData.getTransaction();
        try {
            // t.start(true);
            Dataset dataset = datasetDao.findOne(datasetId);

            zipfile = new ZipOutputStream(new FileOutputStream(file));

            // convert the dataset to json
            zipfile.putNextEntry(new ZipEntry(DATASET_FILE));
            zipfile.write(SpringData.objectToJSON(dataset).getBytes("UTF-8"));
            zipfile.closeEntry();

            // export blobs
            Set<FileDescriptor> blobs = new HashSet<FileDescriptor>();
            byte[] buf = new byte[10240];
            int len = 0;
            for (FileDescriptor fd : dataset.getFileDescriptors()) {
                if (!blobs.contains(fd)) {
                    blobs.add(fd);

                    zipfile.putNextEntry(new ZipEntry(String.format("%s/%s/%s", BLOBS_FOLDER, fd.getId(), fd.getFilename())));
                    InputStream is = fileStorage.readFile(fd);
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
            // t.rollback();
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
        // Transaction t = SpringData.getTransaction();
        try {
            // t.start();

            zipfile = new ZipFile(file);

            // get workflow
            Dataset dataset = SpringData.JSONToObject(zipfile.getInputStream(zipfile.getEntry(DATASET_FILE)), Dataset.class);
            fixPeople(dataset);
            // DatasetDAO datasetDAO = SpringData.getBean(DatasetDAO.class);
            if (datasetDao.exists(dataset.getId())) {
                throw (new Exception("Dataset already imported."));
            }

            // import blobs
            // FileStorage fs = SpringData.getFileStorage();
            // FileDescriptorDAO fdDAO =
// SpringData.getBean(FileDescriptorDAO.class);
            Enumeration<? extends ZipEntry> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith(BLOBS_FOLDER)) {
                    continue;
                }

                String[] pieces = entry.getName().split("/");
                if (fileDescriptorDao.exists(pieces[1])) {
                    logger.info("Already have blob with id : " + pieces[1]);
                    continue;
                }

                // save blob
                InputStream is = zipfile.getInputStream(entry);
                FileDescriptor blob = fileStorage.storeFile(pieces[1], pieces[2], is);
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
            datasetDao.save(dataset);
            // t.commit();

            return dataset;
        } catch (Exception exc) {
            try {
                if (zipfile != null) {
                    zipfile.close();
                }
                // t.rollback();
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
        // Transaction t = SpringData.getTransaction();
        // try {
        // t.start(true);
        LogFile logfile = logFileDao.findOne(logfileId);

        FileDescriptor fd = logfile.getLog();

        FileOutputStream output = new FileOutputStream(file);
        InputStream input = fileStorage.readFile(fd);
        byte[] buf = new byte[10240];
        int len;
        while ((len = input.read(buf)) > 0) {
            output.write(buf, 0, len);
        }
        input.close();
        output.close();
        // } finally {
        // t.rollback();
        // }
    }
}
