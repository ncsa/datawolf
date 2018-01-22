package edu.illinois.ncsa.datawolf.executor.hpc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.AbortException;
import edu.illinois.ncsa.datawolf.FailedException;
import edu.illinois.ncsa.datawolf.RemoteExecutor;
import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.Execution.State;
import edu.illinois.ncsa.datawolf.domain.HPCJobInfo;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter;
import edu.illinois.ncsa.datawolf.domain.dao.HPCJobInfoDao;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption.InputOutput;
import edu.illinois.ncsa.datawolf.executor.hpc.handlers.JobInfoParser;
import edu.illinois.ncsa.datawolf.executor.hpc.ssh.SSHSession;
import edu.illinois.ncsa.datawolf.executor.hpc.util.FileUtils;
import edu.illinois.ncsa.datawolf.executor.hpc.util.NonNLSConstants;
import edu.illinois.ncsa.datawolf.executor.hpc.util.SshUtils;
import edu.illinois.ncsa.datawolf.executor.hpc.util.SystemUtils;
import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.util.BeanUtil;
import edu.illinois.ncsa.gondola.types.submission.ErrorHandlerType;
import edu.illinois.ncsa.gondola.types.submission.JobStateType;
import edu.illinois.ncsa.gondola.types.submission.JobSubmissionType;
import edu.illinois.ncsa.gondola.types.submission.LineType;
import edu.illinois.ncsa.gondola.types.submission.RegexType;
import edu.illinois.ncsa.gondola.types.submission.ScriptType;

/**
 * The HPCExecutor will launch remote jobs based on machine information provided
 * in the XML template. The executable will be passed an arbitrary set of
 * parameters at the command line based on what the user specified when creating
 * the tool. Limitation: file staging currently needs to happen in a separate
 * tool.
 * 
 * TODO CMN : add file staging capability, executor options (e.g. ncpus)
 * 
 * @author "Chris Navarro <cmnavarr@illinois.edu>"
 * 
 */
public class HPCExecutor extends RemoteExecutor {

    private static Logger       logger             = LoggerFactory.getLogger(HPCExecutor.class);
    private static final String CANNOT_CONNECT     = "Cannot connect to";
    // This should be an executor option
    private static final String NCPUS              = "[NCPUS]";                                 //$NON-NLS-1$
    private static final String HPC_SCRIPT_DIR     = "[HPC-SCRIPT-DIR]";                        //$NON-NLS-1$
    private static final String HPC_EXE            = "[HPC-EXE]";                               //$NON-NLS-1$

    public static final String  EXECUTOR_NAME      = "hpc";

    private SSHSession          session;
    private JobSubmissionType   job;

    // File containing location of staged files
    private String              stagedFiles        = null;

    private String              targetUser         = null;
    private String              contactURI         = null;
    private String              targetUserHome     = null;

    // TODO this should be exposed somehow as an executor specific attribute
    private String              ncpuScriptVar      = null;

    // Path to the executable on remote machine
    private String              executablePath;

    // Parsers qstat information
    private JobInfoParser       jobParser;

    private String              gondolaLogFile     = null;
    // private String remoteLogFile = null;
    private File                gondolaLog         = null;
    private File                stdErrLog          = null;
    private File                stdOutLog          = null;

    // Name of standard error/out files
    private String              standardOut        = null;
    private String              standardErr        = null;

    // Dataset id to store log file as tool output
    private String              gondolaLogId       = null;
    private String              stdOutId           = null;
    private String              stdErrId           = null;
    private String              jobId              = null;

    // Job Result Directory
    private String              jobResultDirectory = null;

    // Information returned when job submitted
    private String              submissionStdOut   = null;
    private String              submissionStdErr   = null;

    private boolean             storedJobInfo      = false;
    private String              scriptPath         = null;

    @Inject
    protected HPCJobInfoDao     hpcJobInfoDao;

    @Override
    public State submitRemoteJob(File cwd) throws AbortException, FailedException {

        // contains logging information from job run
        gondolaLog = new File(cwd, NonNLSConstants.LOG);
        stdErrLog = new File(cwd, "stderr.log");
        stdOutLog = new File(cwd, "stdout.log");

        // This is where we'll look for .ssh keys
        String home = System.getProperty("user.home");

        // Commands to append to command line
        ArrayList<String> command = new ArrayList<String>();
        try {
            work.begin();
            if (scriptPath == null) {
                WorkflowStep step = workflowStepDao.findOne(getStepId());
                Execution execution = executionDao.findOne(getExecutionId());
                HPCToolImplementation impl = BeanUtil.JSONToObject(step.getTool().getImplementation(), HPCToolImplementation.class);

                for (CommandLineOption option : impl.getCommandLineOptions()) {
                    switch (option.getType()) {
                    case VALUE:
                        // fixed value
                        if (option.getValue() != null) {
                            command.add(option.getValue());
                        }
                        break;

                    case PARAMETER:
                        String value = null;
                        if (execution.hasParameter(step.getParameters().get(option.getOptionId()))) {
                            value = execution.getParameter(step.getParameters().get(option.getOptionId()));
                        } else {
                            value = step.getTool().getParameter(option.getOptionId()).getValue();
                        }
                        if (option.isCommandline()) {
                            // add the parameters
                            if (option.getFlag() != null) {
                                if (!option.getFlag().isEmpty()) {
                                    command.add(option.getFlag());
                                }
                            }
                            command.add(value);
                        } else if (!option.isCommandline() && option.getFlag() != null) {
                            // TODO CMN : make this parameter of the HPC
// Executor,
                            // perhaps part of an HPCExecutorParameterPage
                            if (option.getFlag().equals("-n")) {
                                ncpuScriptVar = value;
                            }
                        } else {
                            // add special parameters that are expected
                            WorkflowToolParameter param = step.getTool().getParameter(option.getOptionId());

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
                            String key = inputs.get(optionId);
                            Dataset ds = datasetDao.findOne(execution.getDataset(key));
                            if (ds == null) {
                                throw (new AbortException("Dataset is missing."));
                            }
                            try {
                                InputStream is = fileStorage.readFile(ds.getFileDescriptors().get(0));
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
                gondolaLogId = impl.getLog();
                stdOutId = impl.getCaptureStdOut();
                stdErrId = impl.getCaptureStdErr();

                // Generate a unique id
                String normUuid = normalize(UUID.randomUUID().toString());
                String targetPathSep = NonNLSConstants.REMOTE_PATH_SEP;
                String targetLineSep = NonNLSConstants.REMOTE_LINE_SEP;

                String stagingDir = null;
                if (targetUserHome.endsWith(targetPathSep)) {
                    stagingDir = targetUserHome + NonNLSConstants.JOB_SCRIPTS + targetPathSep + normUuid;
                } else {
                    stagingDir = targetUserHome + targetPathSep + NonNLSConstants.JOB_SCRIPTS + targetPathSep + normUuid;
                }

                standardOut = stagingDir + targetPathSep + "stdout";
                standardErr = stagingDir + targetPathSep + "stderr";

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

                executablePath = impl.getExecutable();

                job = createJob(workflow, command, stagingDir, jc);
                jobParser = new JobInfoParser(job.getStatusHandler().getParser());

                // Generate job script locally
                File tmpScript = writeLocalScript(cwd, job.getScript(), stagingDir, normUuid, targetLineSep);

                // copy job script to target machine
                scriptPath = stageFile(tmpScript, stagingDir, session, NonNLSConstants.SCRIPT);

                // path to remote gondola log file
                gondolaLogFile = stagingDir + targetPathSep + NonNLSConstants.LOG;

                // quick check to see if we should stop
                if (isJobStopped()) {
                    throw (new AbortException("Job is stopped."));
                }

                jobId = submit(job, scriptPath, session, targetLineSep);
                createJobInfo();
            } else {
                // Job already setup, just try submitting it again
                String targetLineSep = NonNLSConstants.REMOTE_LINE_SEP;
                jobId = submit(job, scriptPath, session, targetLineSep);
                createJobInfo();
            }

        } catch (AbortException e) {
            throw e;
        } catch (FailedException e) {
            // Job could not be submitted, set state to waiting to try again
            logger.info("Job not submitted because the job scheduler appears to be down, will try again shortly...");
            return State.WAITING;
            // throw e;
        } catch (Throwable e) {
            throw (new FailedException("Could not run transaction to get information about step.", e));
        } finally {
            work.end();
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

        checkForErrors(stdout, stderr, job.getSubmitErrorHandler());
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

    private void checkForErrors(StringBuffer stdout, StringBuffer stderr, ErrorHandlerType errorHandler) throws FailedException {
        submissionStdErr = stderr.toString();
        submissionStdOut = stdout.toString();
        logger.debug("stderr string: " + submissionStdErr);
        logger.debug("stdout string: " + submissionStdErr);

        for (RegexType regex : errorHandler.getStderrError()) {
            if (Pattern.compile(regex.getContent(), JobInfoParser.getFlags(regex.getFlags())).matcher(submissionStdErr).matches()) {
                throw new FailedException("Error submitting job");
            }
        }
        for (RegexType regex : errorHandler.getStdoutError()) {
            if (Pattern.compile(regex.getContent(), JobInfoParser.getFlags(regex.getFlags())).matcher(submissionStdOut).matches()) {
                throw new FailedException("Error submitting job");
            }
        }

        for (RegexType regex : errorHandler.getStderrWarn()) {
            logger.debug("regex for warn: " + regex.getContent());
            logger.debug("regex flag for warn: " + regex.getFlags());
            if (Pattern.compile(regex.getContent(), JobInfoParser.getFlags(regex.getFlags())).matcher(submissionStdErr).matches()) {
                logger.warn(submissionStdErr);
                break;
            }
        }

        for (RegexType regex : errorHandler.getStdoutWarn()) {
            if (Pattern.compile(regex.getContent(), JobInfoParser.getFlags(regex.getFlags())).matcher(submissionStdOut).matches()) {
                logger.warn(submissionStdOut);
                break;
            }
        }
    }

    public String parseJobId(String[] lines) {
        JobInfoParser parser = new JobInfoParser(job.getJobIdParser());
        return parser.parseJobId(lines);
    }

    private JobSubmissionType createJob(File workflow, List<String> command, String stagingDir, JAXBContext jc) throws FailedException {
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
        updateJobScript(job.getScript(), fileMap, command, stagingDir);

        return job;
    }

    private Map<String, String> getWorkflowInputs() throws FailedException {
        Map<String, String> fileMap = new HashMap<String, String>();
        BufferedReader br = null;

        String line = null;
        if (stagedFiles != null) {
            try {
                br = new BufferedReader(new FileReader(stagedFiles));
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(":");
                    String key = parts[1];
                    String fileLocation = parts[2];
                    fileMap.put(key, fileLocation);
                }

            } catch (IOException e) {
                logger.error("Error reading fileLocations input", e);
                throw new FailedException("Error reading fileLocations input.");
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    logger.warn("A problem occurred closing fileLocations input.", e);
                }
            }
        }
        return fileMap;
    }

    protected void updateJobScript(ScriptType script, Map<String, String> fileMap, List<String> command, String stagingDir) {
        // Create executable line from the user specified exe and inputs
        Iterator<String> iterator = fileMap.keySet().iterator();
        // String content = "sh" + NonNLSConstants.SP + executablePath;//
// line.getContent();
        String content = executablePath;

        // Add parameters to command line
        StringBuilder sb = new StringBuilder();
        for (String s : command) {
            sb.append(s);
            sb.append(NonNLSConstants.SP);

        }

        content = content.concat(NonNLSConstants.SP + sb.toString());
        while (iterator.hasNext()) {
            String key = iterator.next();
            String fileLocation = fileMap.get(key);
            content = content.concat(NonNLSConstants.SP + "-" + key + NonNLSConstants.SP + fileLocation);
        }

        // Add Standard out and Standard error
        content = content.concat(NonNLSConstants.SP + "1>" + standardOut + NonNLSConstants.SP + "2>" + standardErr);

        // executionLine.setContent(content);

        // Add executable at line marked HPC_EXE
        for (LineType line : script.getLine()) {
            if (line.getContent().equals(HPC_EXE)) {
                line.setContent(content);
                break;
            }
        }

        // Replace HPC-SCRIPT-DIR with the gondola working directory
        for (LineType line : script.getLine()) {
            if (line.getContent().contains(HPC_SCRIPT_DIR)) {
                String lineContent = line.getContent();
                lineContent = lineContent.replace(HPC_SCRIPT_DIR, stagingDir);
                line.setContent(lineContent);
                // line.setContent(content);
                // break;
            } else if (line.getContent().contains(NCPUS) && ncpuScriptVar != null) {
                String lineContent = line.getContent();
                lineContent = lineContent.replace(NCPUS, ncpuScriptVar);
                line.setContent(lineContent);
            }
        }
    }

    private File writeLocalScript(File cwd, ScriptType script, String stagingDir, String normUuid, String lineSep) throws Exception {
        String logPath = stagingDir + NonNLSConstants.REMOTE_PATH_SEP + NonNLSConstants.LOG;
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
        String targetPath = stagingDir + NonNLSConstants.REMOTE_PATH_SEP + fileName;
        SshUtils.copyTo(f.getAbsolutePath(), targetPath, session);
        return targetPath;
    }

    private static String normalize(String uuid) {
        return uuid.replaceAll(NonNLSConstants.REMOTE_PATH_SEP, NonNLSConstants.DOT).replaceAll(NonNLSConstants.BKESC, NonNLSConstants.DOT)
                .replaceAll(NonNLSConstants.REGDOT + NonNLSConstants.REGDOT, NonNLSConstants.ZEROSTR);
    }

    @Override
    public void cancelRemoteJob() {

        String cancelJobId = jobId;
        if (cancelJobId == null) {
            try {
                List<HPCJobInfo> jobs = hpcJobInfoDao.findByExecutionId(getExecutionId());
                if (jobs.isEmpty()) {
                    logger.error("No job info beans found for execution " + getExecutionId());
                } else {
                    cancelJobId = jobs.get(0).getJobId();
                }
            } catch (Exception e) {
                logger.error("Error retrieving job information for execution " + getExecutionId());
            }
        }

        logger.info(String.format("Stopping RemoteJob : %s", jobId));
        if (session != null) {
            if (cancelJobId != null) {
                String command = job.getTerminatePath() + NonNLSConstants.SP + cancelJobId;
                try {
                    SshUtils.exec(session, command);
                } catch (IllegalArgumentException e) {
                    logger.error("Could not cancel job " + jobId, e);
                } catch (Exception e) {
                    logger.error("Could not cancel job " + jobId, e);
                }
            } else {
                logger.error("Job ID was null for execution id " + getExecutionId());
            }
        }
    }

    @Override
    public State checkRemoteJob() throws FailedException {
        System.out.println("check remote job state");
        StringBuffer out = new StringBuffer();
        String[] lines = null;
        String commandArgs = job.getStatusHandler().getCommandArgs();
        String commandPath = job.getStatusHandler().getCommandPath();
        try {
            if (session != null) {
                String command = commandPath;
                if (commandArgs != null)
                    command += NonNLSConstants.SP + commandArgs;
                // qstat flags should come from the template
                // command += NonNLSConstants.SP + "-j" + NonNLSConstants.SP +
// jobId;
                command += NonNLSConstants.SP + jobId;

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

                // Determine if remote job queue is unresponsive
                if (line.contains(CANNOT_CONNECT)) {
                    State state = this.getState();
                    if (state != State.WAITING) {
                        // Job status not responding, return previous state
                        return state;
                    } else {
                        // Job submitted, but we have not yet received a
// successful job status since, assume queued
                        return State.QUEUED;
                    }
                }

                String[] data = jobParser.parseJobState(line);
                if (data != null) {
                    if (data[0].equals(jobId)) {
                        State jobState = getJobState(data[1]);
                        if (jobState.equals(State.FINISHED) || jobState.equals(State.ABORTED)) {
                            return getGondolaLogFile();
                        } else if (jobState.equals(State.RUNNING) && !storedJobInfo) {
                            updateJobInfo();
                            return jobState;
                        } else {
                            logger.debug("Job is not finished, aborted, nor running: " + jobState.toString());
                            return jobState;
                        }
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
        } catch (Exception e) {
            throw new FailedException("Failed to update job status", e);
        } finally {
            out.setLength(0);
        }

        // At this point qstat didn't return anything so we assume finished and
        // need to check the log to see if we have a failure or success
        // Getting here might be a bug in Ranger's gondola template because
        // KISTI's machine actually returns "Finished"
        logger.debug("qstat did not return any job status, check if job finished.");
        return getGondolaLogFile();
    }

    private State getJobState(String state) {
        if (state.equals(JobStateType.JOB_QUEUED.toString())) {
            return State.QUEUED;
        } else if (state.equals(JobStateType.JOB_RUNNING.toString())) {
            return State.RUNNING;
        } else if (state.equals(JobStateType.JOB_SUSPENDED.toString())) {
            return State.ABORTED;
        } else if (state.equals(JobStateType.JOB_COMPLETED.toString())) {
            return State.FINISHED;
        }

        // TODO CMN : how do we know if a failure occurred?
        // qstat will not report failure, we'll have to parse that
        // from the log
        logger.warn("Unknown job state message returned from qstat");
        return State.UNKNOWN;

    }

    public State getGondolaLogFile() {
        // if we get here, job is neither queued nor running, get log
        try {
            if (!storedJobInfo) {
                updateJobInfo();
            } else {
                SshUtils.copyFrom(gondolaLogFile, gondolaLog.getAbsolutePath(), session);
            }

            // Capture log as stdout
            StringBuilder gondolaLogContent = new StringBuilder();
            BufferedReader gondolaLogReader = new BufferedReader(new FileReader(gondolaLog));
            boolean done = false;
            String startTime = null;
            String endTime = null;

            if (gondolaLogReader != null) {
                String line;
                while ((line = gondolaLogReader.readLine()) != null) {
                    if (line.startsWith("/")) {
                        jobResultDirectory = line;
                    } else if ("DONE".equals(line.toUpperCase())) {
                        done = true;
                    } else if (line.startsWith("start:")) {
                        startTime = line.substring(7, line.length());
                    } else if (line.startsWith("end:")) {
                        endTime = line.substring(5, line.length());
                    }
                    // println(line);
                    gondolaLogContent.append(line);
                    gondolaLogContent.append(NonNLSConstants.LINE_SEP);
                }
                gondolaLogReader.close();
                gondolaLogReader = null;
            }

            if (!done) {
                // If not done, copy standard error and out
                if (SshUtils.exists(standardErr, session)) {
                    SshUtils.copyFrom(standardErr, stdErrLog.getAbsolutePath(), session);
                }
                if (SshUtils.exists(standardOut, session)) {
                    SshUtils.copyFrom(standardOut, stdOutLog.getAbsolutePath(), session);
                }
                // Job failed, store stdout and stderr datasets
                storeStandardOutDataset();
                return State.FAILED;
            }

            // If not done, copy standard error and out
            if (SshUtils.exists(standardErr, session)) {
                SshUtils.copyFrom(standardErr, stdErrLog.getAbsolutePath(), session);
            }
            if (SshUtils.exists(standardOut, session)) {
                SshUtils.copyFrom(standardOut, stdOutLog.getAbsolutePath(), session);
            }

            // Job finished properly, store stdout and stderr
            storeStandardOutDataset();

            // TODO RK : replace code above with the following code.
            // String log = getRemoteLog();

            // List of created datasets
            try {
                // TODO make this more generic
                work.begin();
                WorkflowStep step = workflowStepDao.findOne(getStepId());
                Execution execution = executionDao.findOne(getExecutionId());

                // Record start/end time
                SimpleDateFormat sdfParser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
                if (startTime != null) {
                    execution.setStepStart(getStepId(), sdfParser.parse(startTime).getTime());
                }

                if (endTime != null) {
                    execution.setStepEnd(getStepId(), sdfParser.parse(endTime).getTime());
                }

                ByteArrayInputStream bais = new ByteArrayInputStream(gondolaLogContent.toString().getBytes("UTF-8"));
                FileDescriptor fd = fileStorage.storeFile("gondola-log", bais);

                Dataset ds = new Dataset();
                ds.setTitle("gondola-log");
                ds.setCreator(execution.getCreator());
                ds.addFileDescriptor(fd);
                ds = datasetDao.save(ds);

                String key = step.getOutputs().get(gondolaLogId);
                execution.setDataset(key, ds.getId());
                
                executionDao.save(execution);
                return State.FINISHED;
            } finally {
                work.end();
                session.close();
                session = null;
            }
        } catch (Throwable e) {
            logger.error("Error retrieving log file from remote system and writing it to a dataset.", e);
        }
        return State.FAILED;
    }

    private void createJobInfo() {
        try {
            HPCJobInfo info = new HPCJobInfo();
            info.setExecutionId(this.getExecutionId());
            if (jobId != null) {
                info.setJobId(jobId);
            } else {
                logger.warn("Job id was null");
            }

            hpcJobInfoDao.save(info);

        } catch (Exception e) {
            logger.error("Error saving job information in HPC Executor", e);
        }

    }

    // TODO RK remove following code, is rolled into getRemoteLog()
    private void updateJobInfo() {
        try {
            SshUtils.copyFrom(gondolaLogFile, gondolaLog.getAbsolutePath(), session);
            String workingDir = null;
            BufferedReader stdoutReader = new BufferedReader(new FileReader(gondolaLog));
            if (stdoutReader != null) {
                if (stdoutReader.ready()) {
                    String line = stdoutReader.readLine();
                    if (line == null) {
                        stdoutReader.close();
                        stdoutReader = null;
                        return;
                    }
                    workingDir = line;
                    jobResultDirectory = workingDir;

                    try {
                        work.begin();
                        List<HPCJobInfo> jobs = hpcJobInfoDao.findByExecutionId(getExecutionId());

                        if (!jobs.isEmpty()) {
                            HPCJobInfo info = jobs.get(0);
                            // info.setExecutionId(this.getExecutionId());
                            info.setWorkingDir(workingDir);

                            hpcJobInfoDao.save(info);
                            storedJobInfo = true;
                        } else {
                            logger.error("No job information bean found for execution id " + getExecutionId());
                        }
                    } finally {
                        work.end();
                        stdoutReader.close();
                    }
                }
            }

        } catch (Throwable e) {
            logger.error("Error getting log file from remote machine and storing as bean", e);
        }
    }

    @Override
    public String getRemoteLog() {
        try {
            if (session != null) {
                // If the session is still open, make sure we get the error log
                // files if there are any
                if (SshUtils.exists(standardErr, session)) {
                    SshUtils.copyFrom(standardErr, stdErrLog.getAbsolutePath(), session);
                }
                if (SshUtils.exists(standardOut, session)) {
                    SshUtils.copyFrom(standardOut, stdOutLog.getAbsolutePath(), session);
                }
            }
            String workingfolder = null;

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            StringBuffer sb = new StringBuffer();
            String line = null;
            sb.append("-------- STDOUT --------");
            sb.append(NonNLSConstants.LINE_SEP);

            if (jobResultDirectory != null) {
                sb.append("run-dir=" + jobResultDirectory);
                sb.append(NonNLSConstants.LINE_SEP);

                stdout.append("run-dir=" + jobResultDirectory);
                stdout.append(NonNLSConstants.LINE_SEP);
            }

            sb.append(submissionStdOut);
            sb.append(NonNLSConstants.LINE_SEP);
            if (stdOutLog.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(stdOutLog));
                while ((line = br.readLine()) != null) {
                    if (workingfolder == null) {
                        workingfolder = line;
                    }
                    sb.append(line);
                    sb.append(NonNLSConstants.LINE_SEP);

                    stdout.append(line);
                    stdout.append(NonNLSConstants.LINE_SEP);

                }
                br.close();
            }

            sb.append("-------- STDERR --------");
            sb.append(NonNLSConstants.LINE_SEP);
            sb.append(submissionStdErr);
            sb.append(NonNLSConstants.LINE_SEP);

            if (stdErrLog.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(stdErrLog));
                line = null;
                while ((line = br.readLine()) != null) {
                    if (workingfolder == null) {
                        workingfolder = line;
                    }
                    sb.append(line);
                    sb.append(NonNLSConstants.LINE_SEP);

                    stderr.append(line);
                    stderr.append(NonNLSConstants.LINE_SEP);

                }
                br.close();
            }

            // TODO RK : store working folder
            // if ((workingfolder != null) && !storedJobInfo) {
            // storedJobInfo = true;
            // Transaction t = null;
            // try {
            // t = SpringData.getTransaction();
            // t.start();
            // HPCJobInfo info = new HPCJobInfo();
            // info.setExecutionId(this.getExecutionId());
            // info.setWorkingDir(workingfolder);
            // SpringData.getBean(HPCJobInfoDAO.class).save(info);
            // } catch (Exception e) {
            // logger.error("Could not store hpc info bean.");
            // } finally {
            // try {
            // t.commit();
            // } catch (Exception e) {
            // logger.error("Could not store hpc info bean.");
            // }
            // }
            // }

            // return log as string
            return sb.toString();
        } catch (Throwable thr) {
            logger.error("Error getting log file from remote machine.", thr);
            return null;
        }
    }

    private void storeStandardOutDataset() {
        // Store stdout and stderr as datasets
        try {
            work.begin();
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            String line = null;
            if (jobResultDirectory != null) {
                stdout.append("run-dir=" + jobResultDirectory);
                stdout.append(NonNLSConstants.LINE_SEP);
            }

            if (stdOutLog.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(stdOutLog));
                while ((line = br.readLine()) != null) {
                    stdout.append(line);
                    stdout.append(NonNLSConstants.LINE_SEP);
                }
                br.close();
            }

            if (stdErrLog.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(stdErrLog));
                line = null;
                while ((line = br.readLine()) != null) {
                    stderr.append(line);
                    stderr.append(NonNLSConstants.LINE_SEP);
                }
                br.close();
            }

            WorkflowStep step = workflowStepDao.findOne(getStepId());
            Execution execution = executionDao.findOne(getExecutionId());

            ByteArrayInputStream bais = new ByteArrayInputStream(stdout.toString().getBytes("UTF-8"));
            FileDescriptor fd = fileStorage.storeFile("stdout", bais);

            Dataset ds = new Dataset();
            ds.setTitle("stdout");
            ds.setCreator(execution.getCreator());
            ds.addFileDescriptor(fd);
            ds = datasetDao.save(ds);

            String key = step.getOutputs().get(stdOutId);
            execution.setDataset(key, ds.getId());

            bais = new ByteArrayInputStream(stderr.toString().getBytes("UTF-8"));
            fd = fileStorage.storeFile("stderr", bais);

            ds = new Dataset();
            ds.setTitle("stderr");
            ds.setCreator(execution.getCreator());
            ds.addFileDescriptor(fd);
            ds = datasetDao.save(ds);

            key = step.getOutputs().get(stdErrId);
            execution.setDataset(key, ds.getId());

            executionDao.save(execution);
        } catch (Throwable e) {
            logger.error("Error getting log file from remote machine and saving it.", e);
        } finally {
            work.end();
        }
    }

    @Override
    public String getExecutorName() {
        return EXECUTOR_NAME;
    }

}
