package edu.illinois.ncsa.cyberintegrator.executor.hpc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.AbortException;
import edu.illinois.ncsa.cyberintegrator.FailedException;
import edu.illinois.ncsa.cyberintegrator.RemoteExecutor;
import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.InputOutput;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.handlers.JobInfoParser;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.ssh.SSHSession;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.util.FileUtils;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.util.NonNLSConstants;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.util.SshUtils;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.util.SystemUtils;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.event.ObjectCreatedEvent;
import edu.illinois.ncsa.gondola.types.submission.JobStateType;
import edu.illinois.ncsa.gondola.types.submission.JobSubmissionType;
import edu.illinois.ncsa.gondola.types.submission.LineType;
import edu.illinois.ncsa.gondola.types.submission.ScriptType;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * The HPCExecutor will launch remote jobs based on machine information provided
 * in the XML template. The executable will be passed an arbitrary set of
 * parameters at the command line based on what the user specified when creating
 * the tool. Limitation: file staging currently needs to happen in a separate
 * tool.
 * 
 * TODO CMN : add file staging capability
 * 
 * @author "Chris Navarro <cmnavarr@illinois.edu>"
 * 
 */
public class HPCExecutor extends RemoteExecutor {
    private static Logger       logger         = LoggerFactory.getLogger(HPCExecutor.class);
    private static final String NL             = System.getProperty("line.separator");

    public static final String  EXECUTOR_NAME  = "hpc";

    private SSHSession          session;
    private JobSubmissionType   job;

    // File containing location of staged files
    private String              stagedFiles    = null;

    private String              targetUser     = null;
    private String              contactURI     = null;
    private String              targetUserHome = null;

    // Path to the executable on remote machine
    private String              executablePath;

    // Parsers qstat information
    private JobInfoParser       jobParser;

    private String              remoteLogFile  = null;
    private File                log            = null;

    // Dataset id to store log file as tool output
    private String              logId          = null;

    @Override
    public State submitRemoteJob(File cwd) throws AbortException, FailedException {

        // contains logging information from job run
        log = new File(cwd, NonNLSConstants.LOG);

        // This is where we'll look for .ssh keys
        String home = System.getProperty("user.home");

        // Commands to append to command line
        ArrayList<String> command = new ArrayList<String>();
        Transaction t = SpringData.getTransaction();
        try {
            t.start();
            WorkflowStep step = SpringData.getBean(WorkflowStepDAO.class).findOne(getStepId());
            Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(getExecutionId());
            HPCToolImplementation impl = (HPCToolImplementation) step.getTool().getImplementation();

            for (CommandLineOption option : impl.getCommandLineOptions()) {
                switch (option.getType()) {
                case PARAMETER:
                    String id = option.getParameterValue();
                    String key = step.getParameters().get(id);
                    String value = execution.getParameter(key);
                    if (option.isCommandline()) {
                        // add the parameters
                        if (option.getFlag() != null) {
                            if (!option.getFlag().isEmpty()) {
                                command.add(option.getFlag());
                            }
                        }
                        command.add(value);
                        // parameters.add(option);
                    } else {
                        // add special parameters that are expected
                        WorkflowToolParameter param = step.getParameter(key);

                        if (param.getTitle().equals("Target Username")) {
                            targetUser = value;
                        } else if (param.getTitle().equals("Target SSH")) {
                            contactURI = value;
                        } else if (param.getTitle().equals("Target Userhome")) {
                            targetUserHome = value;
                        }
                    }
                    break;

                case DATA:
                    String filename = option.getFilename();
                    if (filename == null) {
                        try {
                            filename = File.createTempFile("ci", ".tmp", cwd).getAbsolutePath();
                        } catch (IOException exc) {
                            throw (new FailedException("Could not create temp file.", exc));
                        }
                    }
                    stagedFiles = cwd.getAbsolutePath() + NonNLSConstants.PATH_SEP + filename;
                    if (option.getInputOutput() != InputOutput.OUTPUT) {

                        String optionId = option.getOptionId();
                        Map<String, String> inputs = step.getInputs();
                        key = inputs.get(optionId);
                        Dataset ds = execution.getDataset(key);
                        if (ds == null) {
                            throw (new AbortException("Dataset is missing."));
                        }
                        try {
                            InputStream is = SpringData.getFileStorage().readFile(ds.getFileDescriptors().get(0));
                            FileOutputStream fos = new FileOutputStream(new File(cwd, filename));
                            byte[] buf = new byte[10240];
                            int len = 0;
                            while ((len = is.read(buf)) > 0) {
                                fos.write(buf, 0, len);
                            }
                            is.close();
                            fos.close();
                        } catch (IOException e) {
                            throw (new FailedException("Could not get input file.", e));
                        }
                    }
                    break;
                }
            }

            try {
                session = SshUtils.maybeGetSession(new URI(contactURI), targetUser, home);
            } catch (URISyntaxException e) {
                logger.error("Failed to open ssh session.", e);
                throw new FailedException("Failed to open ssh session.");
            } catch (Exception e) {
                logger.error("Failed to open ssh session.", e);
                throw new FailedException("Failed to open ssh session.");
            }

            // Dataset id of log file to store as tool output
            logId = impl.getLog();
            // Generate a unique id
            String normUuid = normalize(UUID.randomUUID().toString());
            String targetPathSep = NonNLSConstants.REMOTE_PATH_SEP;
            String targetLineSep = NonNLSConstants.REMOTE_LINE_SEP;

            String stagingDir = null;
            if(targetUserHome.endsWith(targetPathSep)) {
            	stagingDir = targetUserHome + NonNLSConstants.JOB_SCRIPTS + targetPathSep + normUuid + targetPathSep;
            } else {
            	stagingDir = targetUserHome + targetPathSep + NonNLSConstants.JOB_SCRIPTS + targetPathSep + normUuid + targetPathSep;
            }
            //String stagingDir = targetUserHome + targetPathSep + NonNLSConstants.JOB_SCRIPTS + targetPathSep + normUuid + targetPathSep;

            JAXBContext jc;
            try {
                jc = JAXBContext.newInstance(new Class[] { edu.illinois.ncsa.gondola.types.submission.JobSubmissionType.class, edu.illinois.ncsa.gondola.types.submission.JobStatusListType.class,
                        edu.illinois.ncsa.gondola.types.submission.JobStatusType.class, edu.illinois.ncsa.gondola.types.submission.ObjectFactory.class, });
            } catch (JAXBException e) {
                logger.error("Failed to instantiate gondola classes.", e);
                // e.printStackTrace(pw);
                throw new FailedException("Failed to instantiate gondola classes.");
            }

            File workflow = new File(cwd, impl.getTemplate());
            //File executable = new File(cwd, impl.getExecutable());

            executablePath = impl.getExecutable(); //stageFile(executable, stagingDir, session, impl.getExecutable());

            job = createJob(workflow, command, jc);
            jobParser = new JobInfoParser(job.getStatusHandler().getParser());

            // Generate job script locally
            File tmpScript = writeLocalScript(cwd, job.getScript(), stagingDir, normUuid, targetLineSep);

            // copy job script to target machine
            String scriptPath = stageFile(tmpScript, stagingDir, session, NonNLSConstants.SCRIPT);

            // path to remote log file
            remoteLogFile = stagingDir + NonNLSConstants.LOG;

            String jobId = submit(job, scriptPath, session, targetLineSep);

        } catch (AbortException e) {
            throw e;
        } catch (FailedException e) {
            throw e;
        } catch (Throwable e) {
            throw (new FailedException("Could not run transaction to get information about step.", e));
        } finally {
            try {
                if (t != null) {
                    t.commit();
                }
            } catch (Exception e) {
                throw (new FailedException("Could not commit transaction to retrieve information about step.", e));
            }
        }

        // assume at this point it's queued, but we could run qstat to be sure
        return State.QUEUED;
    }

    private String submit(JobSubmissionType job, String targetPath, SSHSession session, String lineSep) throws IllegalArgumentException, Exception, IOException {
        StringBuffer stdout = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        String[] lines = null;
        if (session != null) {
            String command = job.getSubmitPath() + NonNLSConstants.SP + targetPath;
            logger.debug("submit command: " + command);
            SshUtils.exec(session, command, stdout, stderr);
        } // else {
          // List<String> args = new ArrayList<String>();
          // args.add(job.getSubmitPath());
          // args.add(targetPath);
          // SystemUtils.doExec(args, null, null, true, stdout, stderr);
          // }

        // checkForErrors(stdout, stderr, subE.getErrorHandler());

        // if (subE.getJobIdParser().isStderr())
        // lines = stderr.toString().split(lineSep);
        // else
        lines = stdout.toString().split(lineSep);
        if (job.getJobIdParser() != null) {
            return parseJobId(lines);
        }

        return "job-id-unknown";
    }

    public String parseJobId(String[] lines) {
        JobInfoParser parser = new JobInfoParser(job.getJobIdParser());
        return parser.parseJobId(lines);
    }

    private JobSubmissionType createJob(File workflow, List<String> command, JAXBContext jc) throws FailedException {
        JAXBElement<?> element = null;
        try {
            element = (JAXBElement<?>) jc.createUnmarshaller().unmarshal(workflow);
        } catch (JAXBException e) {
            logger.error("Error unmarshalling workflow.", e);
            throw new FailedException("Error unmarshalling workflow.");
        }

        Map<String, String> fileMap = getWorkflowInputs();

        JobSubmissionType job = (JobSubmissionType) element.getValue();
        // job.setTargetUri(targetURI);
        // job.setMyproxyUser(myproxyUser);
        job.setTargetUser(targetUser);
        job.setTargetUserHome(targetUserHome);
        updateJobScript(job.getScript(), fileMap, command);

        return job;
    }

    private Map<String, String> getWorkflowInputs() throws FailedException {
        Map<String, String> fileMap = new HashMap<String, String>();
        BufferedReader br = null;

        String line = null;
        try {
            br = new BufferedReader(new FileReader(stagedFiles));
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                String key = parts[1];
                String fileLocation = parts[2];
                fileMap.put(key, fileLocation);
            }

            return fileMap;
        } catch (IOException e) {
            logger.error("Error reading fileLocations input", e);
            throw new FailedException("Error reading fileLocations input.");
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.warn("A problem occurred closing fileLocations input.", e);
            }
        }
    }

    protected void updateJobScript(ScriptType script, Map<String, String> fileMap, List<String> command) {
        // Create executable line from the user specified exe and inputs
        LineType executionLine = new LineType();
        Iterator<String> iterator = fileMap.keySet().iterator();
        String content = "sh" + NonNLSConstants.SP + executablePath;// line.getContent();

        // Add parameters to command line
        StringBuilder sb = new StringBuilder();
        for (String s : command) {
            sb.append(s);
            sb.append(" ");
        }

        content = content.concat(" " + sb.toString());

        while (iterator.hasNext()) {
            String key = iterator.next();
            String fileLocation = fileMap.get(key);
            content = content.concat(NonNLSConstants.SP + "-" + key + NonNLSConstants.SP + fileLocation);
        }

        executionLine.setContent(content);
        script.getLine().add(executionLine);

        LineType done = new LineType();
        done.setContent("echo" + NonNLSConstants.SP + NonNLSConstants.DONE);
        done.setLog(true);
        script.getLine().add(done);
    }

    private File writeLocalScript(File cwd, ScriptType script, String stagingDir, String normUuid, String lineSep) throws Exception {
        String logPath = stagingDir + NonNLSConstants.LOG;
        // status.setLogFile(logPath);
        StringBuffer sb = new StringBuffer();
        for (LineType line : script.getLine()) {
            sb.append(line.getContent());
            if (line.isLog())
                sb.append(NonNLSConstants.GTGT).append(logPath);
            sb.append(lineSep);
        }
        
        File tmp = new File(cwd, normUuid);
        FileUtils.writeBytes(tmp, sb.toString().getBytes(), false);

        return tmp;
    }

    private String stageFile(File f, String stagingDir, SSHSession session, String fileName) throws Throwable {
        SshUtils.mkdirs(stagingDir, session);
        String targetPath = stagingDir + fileName;
        SshUtils.copyTo(f.getAbsolutePath(), targetPath, session);
        return targetPath;
    }

    private static String normalize(String uuid) {
        return uuid.replaceAll(NonNLSConstants.REMOTE_PATH_SEP, NonNLSConstants.DOT).replaceAll(NonNLSConstants.BKESC, NonNLSConstants.DOT)
                .replaceAll(NonNLSConstants.REGDOT + NonNLSConstants.REGDOT, NonNLSConstants.ZEROSTR);
    }

    @Override
    public void cancelRemoteJob() {
        // TODO : CMN : implement this
    }

    @Override
    public State checkRemoteJob() throws FailedException {
        StringBuffer out = new StringBuffer();
        String[] lines = null;
        String commandArgs = job.getStatusHandler().getCommandArgs();
        String commandPath = job.getStatusHandler().getCommandPath();
        try {
            if (session != null) {
                String command = commandPath;
                if (commandArgs != null)
                    command += NonNLSConstants.SP + commandArgs;
                logger.debug("get status command: " + command);
                SshUtils.exec(session, command, out, null);
                lines = out.toString().trim().split(NonNLSConstants.REMOTE_LINE_SEP);
            } else {
                List<String> args = new ArrayList<String>();
                args.add(commandPath);
                if (commandArgs != null)
                    args.add(commandArgs);
                SystemUtils.doExec(args, null, null, true, out, null);
                lines = out.toString().trim().split(NonNLSConstants.LINE_SEP);
            }
            for (String line : lines) {
                String[] data = jobParser.parseJobState(line);
                if (data != null) {
                	State jobState = getJobState(data[1]);
                	if(jobState.equals(State.FINISHED)) {
                		getLogFile();
                		return jobState;
                	} else {
                		return jobState;
                	}
                    
                    // if (data[1].equals(JobStateType.JOB_QUEUED.toString())) {
                    // return State.QUEUED;
                    // } else if (data[1].equals(JobStateType.JOB_RUNNING)) {
                    // return State.RUNNING;
                    // }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new FailedException("Failed to update job status", e);
        } catch (Exception e) {} finally {
            out.setLength(0);
        }

        
        // At this point qstat didn't return anything so we assume finished and
        // need to check the log to see if we have a failure or success
        // Getting here might be a bug in Ranger's gondola template because KISTI's machine actually returns "Finished"
        getLogFile();
        
        return State.FINISHED;
    }

    private State getJobState(String state) {
        if (state.equals(JobStateType.JOB_QUEUED.toString())) {
            return State.QUEUED;
        } else if (state.equals(JobStateType.JOB_RUNNING.toString())) {
            return State.RUNNING;
        } else if (state.equals(JobStateType.JOB_SUSPENDED.toString())) {
            return State.QUEUED;
        } else if (state.equals(JobStateType.JOB_COMPLETED.toString())) {
            return State.FINISHED;
        }

        // TODO CMN : how do we know if a failure occurred?
        // qstat will not report failure, we'll have to parse that
        // from the log
        logger.warn("Unknown job state message returned from qstat");
        return State.UNKNOWN;

    }
    
    public void getLogFile() {
    	// if we get here, job is neither queued nor running, get log
        try {
            SshUtils.copyFrom(remoteLogFile, log.getAbsolutePath(), session);
            // Capture log as stdout
            StringBuilder stdout = new StringBuilder();
            BufferedReader stdoutReader = new BufferedReader(new FileReader(log));
            if (stdoutReader != null) {
                if (stdoutReader.ready()) {
                    String line = stdoutReader.readLine();
                    if (line == null) {
                        stdoutReader.close();
                        stdoutReader = null;
                    }
                    // println(line);
                    stdout.append(line);
                    stdout.append(NL);
                }
            }
            Transaction t = null;
            // List of created datasets
            List<AbstractBean> datasets = new ArrayList<AbstractBean>();
            try {
                // TODO make this more generic
                t = SpringData.getTransaction();
                t.start();
                WorkflowStep step = SpringData.getBean(WorkflowStepDAO.class).findOne(getStepId());
                Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(getExecutionId());

                ByteArrayInputStream bais = new ByteArrayInputStream(stdout.toString().getBytes("UTF-8"));
                FileDescriptor fd = SpringData.getFileStorage().storeFile(bais);

                Dataset ds = new Dataset();
                ds.setTitle("stdout");
                ds.setCreator(execution.getCreator());
                ds.addFileDescriptor(fd);
                SpringData.getBean(DatasetDAO.class).save(ds);

                String key = step.getOutputs().get(logId);
                execution.setDataset(key, ds);
                datasets.add(ds);
            } finally {
                t.commit();
                SpringData.getEventBus().fireEvent(new ObjectCreatedEvent(datasets));
            }
        } catch (Throwable e) {
            logger.error("Error retrieving log file from remote system and writing it to a dataset.", e);
        }
    }

    @Override
    public String getRemoteLog() {
        return null;
    }

    @Override
    public String getExecutorName() {
        return EXECUTOR_NAME;
    }

}
