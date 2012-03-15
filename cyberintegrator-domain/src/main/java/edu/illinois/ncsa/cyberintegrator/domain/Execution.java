package edu.illinois.ncsa.cyberintegrator.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Person;

@Entity(name = "Execution")
@Document(collection = "Execution")
public class Execution extends AbstractBean {
    /** Used for serialization of object */
    private static final long  serialVersionUID = 1L;

    /** Date the execution is created */
    private Date               date             = new Date();

    /** creator of the execution */
    @DBRef
    private Person             creator          = null;

    /** list of steps executed */
    private Set<ExecutionStep> steps            = new HashSet<ExecutionStep>();

    /**
     * Create a new instance of the execution.
     */
    public Execution() {}

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
