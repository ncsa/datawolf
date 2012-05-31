/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.executor.commandline;

import javax.persistence.Entity;

import edu.illinois.ncsa.domain.AbstractBean;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
@Entity(name = "CommandLineOption")
public class CommandLineOption extends AbstractBean {
    private static final long serialVersionUID = 1L;

    public enum Type {
        VALUE, PARAMETER, DATA
    }

    public enum InputOutput {
        INPUT, OUTPUT, BOTH
    }

    private Type        type;
    private String      optionId;

    // optional flag to put before next option
    private String      flag;

    // TYPE=VALUE
    // fixed value
    private String      value;

    // TYPE=PARAMETER
    // user specified parameter with a default value
    private String      parameterValue;

    // TYPE=PARAMETER
    // user specified parameter with a default value
    private boolean     commandline;
    private InputOutput inputOutput;
    private String      filename;

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the optionId
     */
    public String getOptionId() {
        return optionId;
    }

    /**
     * @param optionId
     *            the optionId to set
     */
    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    /**
     * @return the flag
     */
    public String getFlag() {
        return flag;
    }

    /**
     * @param flag
     *            the flag to set
     */
    public void setFlag(String flag) {
        this.flag = flag;
    }

    /**
     * @return the parameterValue
     */
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * @param parameterValue
     *            the parameterValue to set
     */
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the commandline
     */
    public boolean isCommandline() {
        return commandline;
    }

    /**
     * @param commandline
     *            the commandline to set
     */
    public void setCommandline(boolean commandline) {
        this.commandline = commandline;
    }

    /**
     * @return the inputOutput
     */
    public InputOutput getInputOutput() {
        return inputOutput;
    }

    /**
     * @param inputOutput
     *            the inputOutput to set
     */
    public void setInputOutput(InputOutput inputOutput) {
        this.inputOutput = inputOutput;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename
     *            the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

}
