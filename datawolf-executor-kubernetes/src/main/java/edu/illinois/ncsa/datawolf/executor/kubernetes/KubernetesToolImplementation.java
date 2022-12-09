package edu.illinois.ncsa.datawolf.executor.kubernetes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption;

public class KubernetesToolImplementation {
    private String                  executable         = null;
    private List<CommandLineOption> commandLineOptions = new ArrayList<CommandLineOption>();
    private Map<String, String>     env                = new HashMap<String, String>();
    private String                  captureStdOut      = null;
    private String                  captureStdErr      = null;
    private boolean                 joinStdOutStdErr   = false;

    public KubernetesToolImplementation() {

    }

    /**
     * @return the executable
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * @param executable
     *            the executable to set
     */
    public void setExecutable(String executable) {
        this.executable = executable;
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

    /**
     * @return the env
     */
    public Map<String, String> getEnv() {
        return env;
    }

    /**
     * @param env
     *            the env to set
     */
    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    /**
     * @return the captureStdOut
     */
    public String getCaptureStdOut() {
        return captureStdOut;
    }

    /**
     * @param captureStdOut
     *            the captureStdOut to set
     */
    public void setCaptureStdOut(String captureStdOut) {
        this.captureStdOut = captureStdOut;
    }

    /**
     * @return the captureStdErr
     */
    public String getCaptureStdErr() {
        return captureStdErr;
    }

    /**
     * @param captureStdErr
     *            the captureStdErr to set
     */
    public void setCaptureStdErr(String captureStdErr) {
        this.captureStdErr = captureStdErr;
    }

    /**
     * @return the joinStdOutStdErr
     */
    public boolean isJoinStdOutStdErr() {
        return joinStdOutStdErr;
    }

    /**
     * @param joinStdOutStdErr
     *            the joinStdOutStdErr to set
     */
    public void setJoinStdOutStdErr(boolean joinStdOutStdErr) {
        this.joinStdOutStdErr = joinStdOutStdErr;
    }
}
