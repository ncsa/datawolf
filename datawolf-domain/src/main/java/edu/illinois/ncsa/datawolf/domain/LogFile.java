package edu.illinois.ncsa.datawolf.domain;

import java.util.Date;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.FileDescriptor;

/**
 * A simple class to store the log.
 * 
 * @author Rob Kooper
 * 
 */
public class LogFile extends AbstractBean {
    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** id of the execution */
    private String            executionId      = null;

    /** id of the step */
    private String            stepId           = null;

    /** the date of the log message */
    private Date              date             = new Date();

    /** the actual log message. */
    private FileDescriptor    log              = null;

    public LogFile() {}

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    /**
     * Returns the date of when this log message was creaed.
     * 
     * @return the date of the log message.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date of the log message.
     * 
     * @param date
     *            the date the log message was created.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Returns the filedescriptor of the log message.
     * 
     * @return the log filedescriptor of the log message.
     */
    public FileDescriptor getLog() {
        return log;
    }

    /**
     * Sets the filedescriptor of the log message.
     * 
     * @param log
     *            the filedescriptor of the log message.
     */
    public void setLog(FileDescriptor log) {
        this.log = log;
    }

}
