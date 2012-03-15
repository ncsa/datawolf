package edu.illinois.ncsa.cyberintegrator.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Dataset;

@Entity(name = "ExecutionStep")
@Document(collection = "ExecutionStep")
public class ExecutionStep extends AbstractBean {
    public enum State {
        WAITING, RUNNING, FINISHED, ABORTED, ERROR
    }

    /** Used for serialization of object */
    private static final long    serialVersionUID = 1L;

    /** list of parameter values */
    private Map<String, String>  parameters       = new HashMap<String, String>();

    /** list of input datasets */
    @DBRef
    private Map<String, Dataset> inputs           = new HashMap<String, Dataset>();

    /** list of output datasets */
    @DBRef
    private Map<String, Dataset> outputs          = new HashMap<String, Dataset>();

    private State                state            = State.WAITING;

    private Map<String, State>   outputState      = new HashMap<String, State>();

    private Date                 startDate        = new Date();

    private Date                 endDate          = new Date();
}
