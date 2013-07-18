/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.LogFile;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.event.StepStateChangedEvent;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.LogFileDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.springdata.FileDescriptorDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public abstract class Executor {
    private static Logger logger      = LoggerFactory.getLogger(Executor.class);

    private StringBuilder log         = new StringBuilder();
    private LogFile       logfile     = new LogFile();
    private int           lastsave    = 0;
    private State         state       = State.UNKNOWN;
    private String        executionId = null;
    private String        stepId      = null;
    private boolean       jobStopped  = false;
    private boolean       storeLog    = true;

    /**
     * Should the executor store the logfiles generated.
     * 
     * @return will return true if the logfiles are stored in the database.
     */
    public boolean isStoreLog() {
        return storeLog;
    }

    /**
     * Should the logfiles be stored in the database. Set this to true and the
     * logfiles generated for the execution of a step are stored in the
     * database.
     * 
     * @param storeLog
     *            set to true (the default) to store the logfiles in the
     *            database.
     */
    public void setStoreLog(boolean storeLog) {
        this.storeLog = storeLog;
    }

    /**
     * Name of the executor. This name is used to find the executor to be used
     * for a tool.
     * 
     * @return the name of the executor.
     */
    public abstract String getExecutorName();

    /**
     * Before a step is executed this will check to see if the executor is ready
     * to run. Some executors need a license, this allows the executor to check
     * and see if a license is available before the step is executed. This can
     * reserve any items needed to run. The executer will be started soon.
     * 
     * @return true if the executor can be run.
     */
    public boolean isExecutorReady() {
        return true;
    }

    /**
     * Set the job information for this executor. Each job will have its own
     * executor. Once all the data for the job is ready the system will call
     * startJob for the job to be launched.
     * 
     * When setJobInformation is called it will be inside a transaction and all
     * values inside execution and step can be accessed, however once this
     * function is finished the transaction is committed and accessing data in
     * the execution and/or step can lead to a lazyloading execution.
     * 
     * @param execution
     *            the execution to be used for job information.
     * @param step
     *            the step that needs to be executed.
     * @return the new state of the step in the execution.
     */
    public void setJobInformation(Execution execution, WorkflowStep step) {
        this.executionId = execution.getId();
        this.stepId = step.getId();
        this.state = State.WAITING;
    }

    /**
     * The job is ready to be executed. This function should submit the job and
     * return quickly. This should not do the actual computation.
     */
    public abstract void startJob();

    /**
     * The job should be stopped. If the job is actually running it should be
     * killed, if it is waiting to be executed it should not be started.
     */
    public void stopJob() {
        jobStopped = true;
    }

    /**
     * Returns true if the job is stopped. This will allow the executor to check
     * to see if the job should be stopped. Any executor should call this
     * function often to see if the execution was stopped.
     * 
     * @return true if the job is stopped and execution should be stopped.
     */
    protected boolean isJobStopped() {
        return jobStopped;
    }

    /**
     * Returns the id of the step associated with this executor.
     * 
     * @return id of the step associated with this executor.
     */
    public String getStepId() {
        return stepId;
    }

    /**
     * Returns the id of the execution associated with this executor.
     * 
     * @return id of the execution associated with this executor.
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * Return any log information. This function will return the current log
     * information. This function should return quickly. It is probably best to
     * have a thread running that is responsible for fetching the data and have
     * this function return the latest information available.
     * 
     * @return the current log information.
     */
    public String getLog() {
        return log.toString();
    }

    /**
     * Append message to the log.
     * 
     * @param msg
     *            the message to be added to the log.
     */
    protected void print(String msg) {
        synchronized (log) {
            log.append(msg);
            logger.debug(msg);
            saveLog(false);
        }
    }

    /**
     * Append formatted message to the log.
     * 
     * @param format
     *            A format string
     * @param args
     *            Arguments referenced by the format specifiers in the format
     *            string.
     */
    protected void print(String format, Object... args) {
        print(String.format(format, args));
    }

    /**
     * Append message with newline to the log.
     * 
     * @param msg
     *            the message to be added to the log.
     */
    protected void println(String msg) {
        synchronized (log) {
            log.append(msg);
            log.append("\n");
            logger.debug(msg);
            saveLog(false);
        }
    }

    /**
     * Append formatted message with newline to the log.
     * 
     * @param format
     *            A format string
     * @param args
     *            Arguments referenced by the format specifiers in the format
     *            string.
     */
    protected void println(String format, Object... args) {
        println(String.format(format, args));
    }

    /**
     * Append message and stacktrace of throwable to the log.
     * 
     * @param msg
     *            the messsage to be added to the log.
     * @param thr
     *            the throwable to be added to the log.
     */
    protected void println(String msg, Throwable thr) {
        synchronized (log) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            thr.printStackTrace(pw);

            log.append(msg);
            log.append("\n");
            log.append(sw.toString());
            logger.debug(msg, thr);
            saveLog(false);
        }
    }

    /**
     * Sets the log to the message. This will erase any previous log messages.
     * 
     * @param msg
     *            the new log message.
     */
    protected void setLog(String msg) {
        synchronized (log) {
            log = new StringBuilder(msg);
            logger.debug(msg);
            saveLog(true);
        }
    }

    protected void flushLog() {
        saveLog(true);
    }

    private void saveLog(boolean flush) {
        if (isStoreLog() && (flush || ((log.length() - lastsave) > 1024))) {
            lastsave = log.length();
            if (logfile.getLog() == null) {
                logfile.setExecutionId(executionId);
                logfile.setStepId(stepId);
                logfile.setDate(new Date());
                logfile.setLog(new FileDescriptor());
                logfile = SpringData.getBean(LogFileDAO.class).save(logfile);
            }

            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(log.toString().getBytes("UTF-8"));
                FileDescriptor fd = logfile.getLog();
                fd = SpringData.getFileStorage().storeFile(fd.getId(), "log.txt", bais);
                SpringData.getBean(FileDescriptorDAO.class).save(fd);
            } catch (Exception e) {
                logger.error("Could not save log message.", e);
            }
        }
    }

    /**
     * Return current state of the execution. This function will return the
     * current state of the execution. This function should return quickly. It
     * is probably best to have a thread running that is responsible for
     * fetching the data and have this function return the latest information
     * available.
     * 
     * @return the current state of the execution.
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the state of the job. This will change the state of the job in the
     * execution object and save it. This will create its own transaction and
     * should not be called inside a transaction.
     */
    public void setState(State state) {
        logger.debug("Job " + executionId + ":" + stepId + " entered " + state);
        Transaction t = SpringData.getTransaction();
        try {
            t.start();
            WorkflowStep step = SpringData.getBean(WorkflowStepDAO.class).findOne(stepId);
            Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(executionId);
            execution.setStepState(stepId, state);
            switch (state) {
            case UNKNOWN:
                logger.error("execution entered state UNKNOWN.");
                break;
            case WAITING:
                break;
            case QUEUED:
                execution.setStepQueued(stepId);
                break;
            case RUNNING:
                execution.setStepStart(stepId);
                break;
            case FAILED:
            case ABORTED:
            case FINISHED:
                execution.setStepEnd(stepId);
                for (String id : step.getOutputs().values()) {
                    if (!execution.hasDataset(id)) {
                        execution.setDataset(id, Execution.EMPTY_DATASET);
                    }
                }
                break;
            default:
                logger.info("a new state is entered that is not known.");
            }
            SpringData.getBean(ExecutionDAO.class).save(execution);

            // fire an event to inform everybody of the new state
            StepStateChangedEvent event = new StepStateChangedEvent(step, execution, state);
            SpringData.getEventBus().fireEvent(event);

            try {
                t.commit();
                this.state = state;
            } catch (Exception e) {
                logger.error("Could not set state of step in execution.", e);
            }
        } catch (Exception e) {
            try {
                t.rollback();
                this.state = State.ABORTED;
            } catch (Exception e2) {
                logger.error("Could not roll back transaction.", e2);
            }
            logger.error("Could not set state of step in execution.", e);
        }
    }
}
