package edu.illinois.ncsa.cyberintegrator.executor.hpc;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolImplementation;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;

@Entity(name = "HPCToolImplementation")
public class HPCToolImplementation extends WorkflowToolImplementation {

    private static final long       serialVersionUID   = 1L;
    private String                  executable         = null;
    private String                  template           = null;
    private String                  log                = null;

    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
    @JoinTable(name = "HPCToolImplementationOptions")
    private List<CommandLineOption> commandLineOptions = new ArrayList<CommandLineOption>();

    public HPCToolImplementation() {

    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getLog() {
        return log;
    }

    /**
     * @return the options
     */
    public List<CommandLineOption> getCommandLineOptions() {
        return commandLineOptions;
    }

    /**
     * @param options
     *            the options to set
     */
    public void setCommandLineOptions(List<CommandLineOption> commandLineOptions) {
        this.commandLineOptions = commandLineOptions;
    }

}
