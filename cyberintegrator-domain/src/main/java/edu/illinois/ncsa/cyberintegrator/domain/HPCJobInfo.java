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
    public String             executionId      = null;

    /** Directory where job is generating output */
    public String             workingDir       = null;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

}
