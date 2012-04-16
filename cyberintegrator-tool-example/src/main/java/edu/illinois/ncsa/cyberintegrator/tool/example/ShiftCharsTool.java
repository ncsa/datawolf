/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2012, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
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
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
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
 *******************************************************************************/
package edu.illinois.ncsa.cyberintegrator.tool.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.illinois.ncsa.cyberintegrator.AbortException;
import edu.illinois.ncsa.cyberintegrator.FailedException;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.JavaTool;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Dataset;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Parameter;
import edu.illinois.ncsa.cyberintegrator.executor.java.tool.Parameter.ParameterType;

/**
 * Simple test java tool. Takes in a file and shifts every character by a set
 * ammount.
 * 
 * @author Luigi Marini <lmarini@illinois.edu>
 * 
 */
public class ShiftCharsTool implements JavaTool {

    private Map<String, File>      inputs     = new HashMap<String, File>();
    private Map<String, Parameter> parameters = new HashMap<String, Parameter>();
    private Map<String, File>      outputs    = new HashMap<String, File>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #getName()
     */
    @Override
    public String getName() {
        return "Shift Characters";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #getVersion()
     */
    @Override
    public int getVersion() {
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #getDescription()
     */
    @Override
    public String getDescription() {
        return "A very simple tool that shifts characters in a string by a certain amount.";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #getInputs()
     */
    @Override
    public Collection<Dataset> getInputs() {
        Collection<Dataset> inputs = new HashSet<Dataset>();
        Dataset input = new Dataset("1", "Input text", "Input text", "text/plain");
        inputs.add(input);
        return inputs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #setInput(java.lang.String, java.io.File)
     */
    @Override
    public void setInput(String id, File input) {
        inputs.put(id, input);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #getOutputs()
     */
    @Override
    public Collection<Dataset> getOutputs() {
        Collection<Dataset> outputs = new HashSet<Dataset>();
        Dataset output = new Dataset("1", "Output text", "Output text", "text/plain");
        outputs.add(output);
        return outputs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #getOutput(java.lang.String)
     */
    @Override
    public File getOutput(String id) {
        return outputs.get(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #getParameters()
     */
    @Override
    public Collection<Parameter> getParameters() {
        Collection<Parameter> parameters = new HashSet<Parameter>();
        Parameter param = new Parameter("1", "Shift", "Shift ammount", ParameterType.NUMBER, "1");
        parameters.add(param);
        return parameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #setParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void setParameter(String id, String value) {
        Parameter parameter = parameters.get(id);
        parameter.setValue(value);
        parameters.put(id, parameter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.cyberintegrator.executor.java.tool.CyberintegratorTool
     * #execute()
     */
    @Override
    public void execute() throws AbortException, FailedException {
        int shift = Integer.parseInt(parameters.get("1").getValue());
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(outputs.get("1")));
            try {
                try {
                    BufferedReader input = new BufferedReader(new FileReader(inputs.get("1")));
                    try {
                        String line = null;
                        while ((line = input.readLine()) != null) {
                            StringBuilder newLine = new StringBuilder();
                            for (int i = 0; i < line.length(); i++) {
                                char shifted = (char) (line.charAt(i) + shift);
                                newLine.setCharAt(i, shifted);
                            }
                            output.write(newLine.toString());
                            output.newLine();
                        }
                    } finally {
                        input.close();
                    }
                } catch (IOException ex) {
                    throw new FailedException("Error reading from file", ex);
                }
            } finally {
                output.close();
            }
        } catch (IOException e) {
            throw new FailedException("Error opening file file", e);
        }
    }
}
