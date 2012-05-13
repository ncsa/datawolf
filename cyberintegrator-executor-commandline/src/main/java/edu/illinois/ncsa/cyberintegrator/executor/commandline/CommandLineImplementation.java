/*******************************************************************************
 * Copyright (c) 2012 University of Illinois/NCSA.  All rights reserved.
 * 
 *   National Center for Supercomputing Applications (NCSA)
 *   http://www.ncsa.illinois.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of University of Illinois, NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 ******************************************************************************/
package edu.illinois.ncsa.cyberintegrator.executor.commandline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolImplementation;

@Entity(name = "CommandLineImplementation")
public class CommandLineImplementation extends WorkflowToolImplementation {
    private static final long       serialVersionUID   = 1L;

    private String                  executable         = null;
    private List<CommandLineOption> commandLineOptions = new ArrayList<CommandLineOption>();
    private Map<String, String>     env                = new HashMap<String, String>();
    private String                  captureStdOut      = null;
    private String                  captureStdErr      = null;
    private boolean                 joinStdOutStdErr   = true;

    public CommandLineImplementation() {}

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
     * @param captureStdOut the captureStdOut to set
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
     * @param captureStdErr the captureStdErr to set
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
     * @param joinStdOutStdErr the joinStdOutStdErr to set
     */
    public void setJoinStdOutStdErr(boolean joinStdOutStdErr) {
        this.joinStdOutStdErr = joinStdOutStdErr;
    }
}
