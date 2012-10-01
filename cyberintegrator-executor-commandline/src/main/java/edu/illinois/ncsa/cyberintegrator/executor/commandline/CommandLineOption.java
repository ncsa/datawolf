/**
 * 
 */
package edu.illinois.ncsa.cyberintegrator.executor.commandline;

/**
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class CommandLineOption {
    public enum Type {
        VALUE, PARAMETER, DATA
    }

    public enum InputOutput {
        INPUT, OUTPUT, BOTH
    }

    // what type of option
    private Type        type;
    // unique id of the option
    private String      optionId;

    // optional flag to put before next option
    private String      flag;

    // TYPE=VALUE
    // fixed value
    private String      value;

    // TYPE=PARAMETER
    // nothing here

    // TYPE=DATA
    // should data be passed on commandline?
    private boolean     commandline = true;
    // input/output information
    private InputOutput inputOutput;
    // filename of the data when stored on disk
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
