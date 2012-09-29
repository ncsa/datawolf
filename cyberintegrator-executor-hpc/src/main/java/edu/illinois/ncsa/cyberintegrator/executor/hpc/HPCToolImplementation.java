package edu.illinois.ncsa.cyberintegrator.executor.hpc;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;

public class HPCToolImplementation  {
    private String                  executable         = null;
    private String                  template           = null;
    private String                  log                = null;
    private String captureStdOut = null;
    private String captureStdErr = null;
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

	public String getCaptureStdOut() {
		return captureStdOut;
	}

	public void setCaptureStdOut(String captureStdOut) {
		this.captureStdOut = captureStdOut;
	}

	public String getCaptureStdErr() {
		return captureStdErr;
	}

	public void setCaptureStdErr(String captureStdErr) {
		this.captureStdErr = captureStdErr;
	}

}
