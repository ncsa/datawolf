package edu.illinois.ncsa.cyberintegrator.domain;

import javax.persistence.Entity;

import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;

@Entity(name = "HPCJobInfo")
@Document(collection = "HPCJobInfo")
public class HPCJobInfo extends AbstractBean {

    /** Used for serialization of object */
    private static final long serialVersionUID = 1L;

    /** Execution id returned from the workflows execution engine **/
    private String            executionId      = null;

    /** Directory where job is generating output */
    private String            workingDir       = null;

    /** File with job output */
    private String            standardOutput   = null;

    /** File with job error */
    private String            standardError    = null;

    /** Job ID from scheduler */
    private String            jobId            = null;

    /**
     * Return the execution id assigned by the engine
     * 
     * @return the execution id assigned by the engine
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * Sets the execution id assigned by the engine for the workflow
     * 
     * @param executionId
     *            the execution id assigned by the engine for the workflow
     */
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    /**
     * Return the working directory for the job on the remote machine
     * 
     * @return the working directory for the job on the remote machine
     */
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * Sets the working directory for the job on the remote machine
     * 
     * @param workingDir
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Return the standard output from the job execution
     * 
     * @return the standard output from the job execution
     */
    public String getStandardOutput() {
        return standardOutput;
    }

    /**
     * Sets the standard output from the job execution
     * 
     * @param standardOutput
     *            the standard output from the job execution
     */
    public void setStandardOutput(String standardOutput) {
        this.standardOutput = standardOutput;
    }

    /**
     * Return the standard error from the job execution
     * 
     * @return the standard error from the job execution
     */
    public String getStandardError() {
        return standardError;
    }

    /**
     * Sets the standard error for the job execution
     * 
     * @param standardError
     *            the standard error for the job execution
     */
    public void setStandardError(String standardError) {
        this.standardError = standardError;
    }

    /**
     * Return the job id from the scheduler
     * 
     * @return the job id from the scheduler
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Sets the job id assigned by the scheduler
     * 
     * @param jobId
     *            the jobid from the scheduler
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

}
