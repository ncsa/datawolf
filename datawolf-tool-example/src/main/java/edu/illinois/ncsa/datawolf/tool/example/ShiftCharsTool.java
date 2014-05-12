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
package edu.illinois.ncsa.datawolf.tool.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.AbortException;
import edu.illinois.ncsa.datawolf.FailedException;
import edu.illinois.ncsa.datawolf.executor.java.tool.Dataset;
import edu.illinois.ncsa.datawolf.executor.java.tool.JavaTool;
import edu.illinois.ncsa.datawolf.executor.java.tool.Parameter;
import edu.illinois.ncsa.datawolf.executor.java.tool.Parameter.ParameterType;

/**
 * Simple test java tool. Takes in a file and shifts every character by a set
 * ammount.
 * 
 * @author Luigi Marini <lmarini@illinois.edu>
 * 
 */
public class ShiftCharsTool implements JavaTool {
    private static Logger logger     = LoggerFactory.getLogger(ShiftCharsTool.class);

    private File          tempfolder = null;
    private int           shift      = 0;
    private InputStream   input;
    private File          output;

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
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
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
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
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
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
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
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
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
     * #setInput(java.lang.String, java.io.InputStream)
     */
    @Override
    public void setInput(String id, InputStream input) {
        if ("1".equals(id)) {
            this.input = input;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
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
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
     * #getOutput(java.lang.String)
     */
    @Override
    public InputStream getOutput(String id) {
        if ("1".equals(id)) {
            try {
                return new FileInputStream(output);
            } catch (FileNotFoundException e) {
                logger.warn("Could not open output file.", e);
                return null;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
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
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
     * #setParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void setParameter(String id, String value) {
        if ("1".equals(id)) {
            shift = Integer.parseInt(value);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.datawolf.executor.java.tool.JavaTool#setTempFolder
     * (java.io.File)
     */
    @Override
    public void setTempFolder(File tempfolder) {
        this.tempfolder = tempfolder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.illinois.ncsa.datawolf.executor.java.tool.DataWolfTool
     * #execute()
     */
    @Override
    public void execute() throws AbortException, FailedException {
        try {
            output = File.createTempFile("test", ".ci", tempfolder);
            BufferedWriter bw = new BufferedWriter(new FileWriter(output));
            try {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(input));
                    try {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            StringBuilder newLine = new StringBuilder();
                            for (int i = 0; i < line.length(); i++) {
                                char shifted = (char) (line.charAt(i) + shift);
                                newLine.append(shifted);
                            }
                            bw.write(newLine.toString());
                            bw.newLine();
                        }
                    } finally {
                        br.close();
                    }
                } catch (IOException ex) {
                    throw new FailedException("Error reading from file", ex);
                }
            } finally {
                bw.close();
            }
        } catch (IOException e) {
            throw new FailedException("Error opening file file", e);
        }
    }
}
