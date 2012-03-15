package edu.illinois.ncsa.cyberintegrator.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;

@Entity(name = "Execution")
@Document(collection = "Execution")
public class Execution extends AbstractBean {
    public enum State {
        WAITING, RUNNING, FINISHED, ABORTED, ERROR
    }

    /** Used for serialization of object */
    private static final long    serialVersionUID = 1L;

    /** Workflow that is executed */
    @DBRef
    private Workflow             workflow         = null;

    /** Date the execution is created */
    private Date                 date             = new Date();

    /** creator of the execution */
    @DBRef
    private Person               creator          = null;

    /** maping a parameter to a specific parameter in the workflow */
    private Map<String, String>  parameters       = new HashMap<String, String>();

    /** maping a dataset to a specific dataset in the workflow */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ExecutionDatasets")
    @DBRef
    private Map<String, Dataset> datasets         = new HashMap<String, Dataset>();

    /** the state of each step executed */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ExecutionStepState")
    private Map<String, State>   stepState        = new HashMap<String, State>();

    /** the start date of each step executed */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ExecutionStepStart")
    private Map<String, Date>    stepStart        = new HashMap<String, Date>();

    /** the end date of each step executed */
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "ExecutionStepEnd")
    private Map<String, Date>    stepEnd          = new HashMap<String, Date>();

    /**
     * Create a new instance of the execution.
     */
    public Execution() {}

    /**
     * Return the workflow that was executed.
     * 
     * @return workflow that was executed.
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * Sets the workflow that is executed.
     * 
     * @param workflow
     *            sets the workflow this execution is for.
     * 
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Return the date when the execution was created.
     * 
     * @return date the execution was created.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date when the execution was created.
     * 
     * @param date
     *            sets the date when the execution was created.
     * 
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Return the PersonBean that is the creator of the execution
     * 
     * @return PersonBean that represents the creator
     */
    public Person getCreator() {
        return creator;
    }

    /**
     * Sets the PersonBean that represents the creator of the execution.
     * 
     * @param creator
     *            sets the PersonBean that represents the creator of the
     *            execution.
     */
    public void setCreator(Person creator) {
        this.creator = creator;
    }
}
