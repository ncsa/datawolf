/*******************************************************************************
 * University of Illinois/NCSA 
 * Open Source License
 * 
 * Copyright (c) 2006, 2008 NCSA/UIUC.  All rights reserved.
 * 
 *  Developed by:
 *   Image Spatial Data Analysis Group (ISDA Group)
 *   http://isda.ncsa.uiuc.edu/
 *
 *   Cyber-Environments and Technologies
 *   http://cet.ncsa.uiuc.edu/
 *
 *   National Center for Supercomputing Applications (NCSA)
 *   http://www.ncsa.uiuc.edu/
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
 *******************************************************************************/
package edu.illinois.ncsa.cyberintegrator.executor.commandline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.AbortException;
import edu.illinois.ncsa.cyberintegrator.FailedException;
import edu.illinois.ncsa.cyberintegrator.LocalExecutor;
import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.InputOutput;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowStepDAO;
import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.event.ObjectCreatedEvent;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

/**
 * This executor will allow the user to execute any arbitrary executable.
 * 
 * @author kooper
 * 
 */
public class CommandLineExecutor extends LocalExecutor {
    private static Logger       logger        = LoggerFactory.getLogger(CommandLineExecutor.class);
    private static final String NL            = System.getProperty("line.separator");

    public static String        EXECUTOR_NAME = "commandline";

    @Override
    public String getExecutorName() {
        return EXECUTOR_NAME;
    }

    @Override
    public void execute(File cwd) throws AbortException, FailedException {
        ArrayList<String> command = new ArrayList<String>();
        Map<String, String> env = new HashMap<String, String>();
        Map<String, String> outputfiles = new HashMap<String, String>();

        Transaction t = SpringData.getTransaction();
        try {
            t.start();

            WorkflowStep step = SpringData.getBean(WorkflowStepDAO.class).findOne(getStepId());
            Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(getExecutionId());
            CommandLineImplementation impl = (CommandLineImplementation) step.getTool().getImplementation();

            logger.info("Executing " + step.getTitle() + " with tool " + step.getTool().getTitle());

            // get implementation details
            if (impl.getEnv() != null) {
                env.putAll(impl.getEnv());
            }

            // find the app to execute
            command.add(findApp(impl.getExecutable().trim(), cwd));

            // add the options in order
            for (CommandLineOption option : impl.getCommandLineOptions()) {
                // all have a flag option
                if (option.getFlag() != null) {
                    command.add(option.getFlag());
                }

                switch (option.getType()) {
                case VALUE:
                    // fixed value
                    if (option.getValue() != null) {
                        command.add(option.getValue());
                    }
                    break;

                case PARAMETER:
                    // user specified value
                    String value = execution.getParameter(option.getOptionId());
                    if (value == null) {
                        value = option.getParameterValue();
                    }
                    if (value != null) {
                        command.add(value);
                    }
                    break;

                case DATA:
                    // pointer to file on disk
                    String filename = option.getFilename();
                    if (filename == null) {
                        try {
                            filename = File.createTempFile("ci", ".tmp", cwd).getAbsolutePath();
                        } catch (IOException exc) {
                            throw (new FailedException("Could not create temp file.", exc));
                        }
                    }

                    if (option.isCommandline()) {
                        if (option.getInputOutput() != InputOutput.OUTPUT) {
                            Dataset ds = execution.getDataset(option.getOptionId());
                            if (ds == null) {
                                throw (new AbortException("Dataset is missing."));
                            }
                            try {
                                InputStream is = SpringData.getFileStorage().readFile(ds.getFileDescriptors().get(0));
                                FileOutputStream fos = new FileOutputStream(filename);
                                byte[] buf = new byte[10240];
                                int len = 0;
                                while ((len = is.read(buf)) > 0) {
                                    fos.write(buf, 0, len);
                                }
                                is.close();
                                fos.close();
                            } catch (IOException e) {
                                throw (new FailedException("Could not get input file.", e));
                            }
                        }
                        command.add(filename);
                    }

                    if (option.getInputOutput() != InputOutput.INPUT) {
                        // for this to work, with BOTH input and output need to
                        // have the same ID
                        outputfiles.put(option.getOptionId(), filename);
                    }
                    break;
                }
            }

        } catch (AbortException e) {
            throw e;
        } catch (FailedException e) {
            throw e;
        } catch (Throwable e) {
            throw (new FailedException("Could not run transaction to get information about step.", e));
        } finally {
            try {
                if (t != null) {
                    t.commit();
                }
            } catch (Exception e) {
                throw (new FailedException("Could not commit transaction to retrieve information about step.", e));
            }
        }

        // show command that will be executed
        StringBuilder sb = new StringBuilder();
        for (String s : command) {
            sb.append(s);
            sb.append(" ");
        }
        println("Executing : " + sb.toString());

        // create the process builder
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(cwd);
        pb.redirectErrorStream(true);

        // setup env variables
        Map<String, String> pbenv = pb.environment();
        for (Entry<String, String> var : env.entrySet()) {
            if (var.getKey().equals("PATH") && pbenv.containsKey(var.getKey())) { //$NON-NLS-1$
                pbenv.put(var.getKey(), var.getValue() + File.pathSeparator + pbenv.get(var.getKey()));
            } else {
                pbenv.put(var.getKey(), var.getValue());
            }
        }

        // check if job is still alive
        if (isJobStopped()) {
            throw (new AbortException("Job is stopped."));
        }

        // Start actual process
        Process process;
        try {
            process = pb.start();
        } catch (IOException exc) {
            throw (new FailedException("Could not start process.", exc));
        }

        // don't let the process hang forever waiting for stdin
        BufferedWriter stdinWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        try {
            stdinWriter.close();
        } catch (IOException e) {}

        // wait for the process to be done, store output in log
        StringBuilder stdout = new StringBuilder();
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder stderr = new StringBuilder();
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        do {
            // check if job is still alive
            if (isJobStopped()) {
                throw (new AbortException("Job is stopped."));
            }

            boolean wait = true;
            try {
                if (stdoutReader != null) {
                    if (stdoutReader.ready()) {
                        String line = stdoutReader.readLine();
                        if (line == null) {
                            stdoutReader.close();
                            stdoutReader = null;
                        }
                        println("STDOUT : " + line);
                        stdout.append(line);
                        stdout.append(NL);
                        wait = false;
                    } else if (!isRunning(process)) {
                        stdoutReader.close();
                        stdoutReader = null;
                        wait = false;
                    }
                }
            } catch (IOException exc) {
                logger.info("Exception reading line from stdout.", exc);
                stdoutReader = null;
            }
            try {
                if (stderrReader != null) {
                    if (stderrReader.ready()) {
                        String line = stderrReader.readLine();
                        if (line == null) {
                            stderrReader.close();
                            stderrReader = null;
                        }
                        println("STDERR : " + line);
                        stderr.append(line);
                        stderr.append(NL);
                        wait = false;
                    } else if (!isRunning(process)) {
                        stderrReader.close();
                        stderrReader = null;
                        wait = false;
                    }
                }
            } catch (IOException exc) {
                logger.info("Exception reading line from stderr.", exc);
                stderrReader = null;
            }
            if (wait) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
            }
        } while ((stdoutReader != null) || (stderrReader != null));

        t = SpringData.getTransaction();
        // List of created datasets
        List<AbstractBean> datasets = new ArrayList<AbstractBean>();
        try {
            t.start();
            WorkflowStep step = SpringData.getBean(WorkflowStepDAO.class).findOne(getStepId());
            Execution execution = SpringData.getBean(ExecutionDAO.class).findOne(getExecutionId());
            CommandLineImplementation impl = (CommandLineImplementation) step.getTool().getImplementation();

            // collect stdout/stderr
            boolean saveExecution = false;
            if (impl.getCaptureStdOut() != null) {
                try {
                    if (impl.isJoinStdOutStdErr()) {
                        stdout.append("\n");
                        stdout.append(stderr);
                    }
                    ByteArrayInputStream bais = new ByteArrayInputStream(stdout.toString().getBytes("UTF-8"));
                    FileDescriptor fd = SpringData.getFileStorage().storeFile(bais);

                    Dataset ds = new Dataset();
                    ds.setCreator(execution.getCreator());
                    ds.addFileDescriptor(fd);
                    SpringData.getBean(DatasetDAO.class).save(ds);

                    execution.setDataset(step.getOutputs().get(impl.getCaptureStdOut()), ds);
                    datasets.add(ds);
                    saveExecution = true;
                } catch (IOException exc) {
                    logger.warn("Could not store output.", exc);
                }
            }
            if (!impl.isJoinStdOutStdErr() && (impl.getCaptureStdErr() != null)) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(stderr.toString().getBytes("UTF-8"));
                    FileDescriptor fd = SpringData.getFileStorage().storeFile(bais);

                    Dataset ds = new Dataset();
                    ds.setCreator(execution.getCreator());
                    ds.addFileDescriptor(fd);
                    SpringData.getBean(DatasetDAO.class).save(ds);

                    execution.setDataset(step.getOutputs().get(impl.getCaptureStdErr()), ds);
                    datasets.add(ds);
                    saveExecution = true;
                } catch (IOException exc) {
                    logger.warn("Could not store output.", exc);
                }
            }

            // collect the output files
            for (Entry<String, String> entry : outputfiles.entrySet()) {
                try {
                    FileInputStream fis = new FileInputStream(entry.getValue());
                    FileDescriptor fd = SpringData.getFileStorage().storeFile(fis);

                    Dataset ds = new Dataset();
                    ds.setCreator(execution.getCreator());
                    ds.addFileDescriptor(fd);
                    SpringData.getBean(DatasetDAO.class).save(ds);

                    execution.setDataset(step.getOutputs().get(entry.getValue()), ds);
                    datasets.add(ds);
                    saveExecution = true;
                } catch (IOException exc) {
                    logger.warn("Could not store output.", exc);
                }
            }
            if (saveExecution) {
                SpringData.getBean(ExecutionDAO.class).save(execution);
            }

        } catch (AbortException e) {
            throw e;
        } catch (FailedException e) {
            throw e;
        } catch (Throwable e) {
            throw (new FailedException("Could not run transaction to save information about step.", e));
        } finally {
            try {
                if (t != null) {
                    t.commit();
                    SpringData.getEventBus().fireEvent(new ObjectCreatedEvent(datasets));
                }
            } catch (Exception e) {
                throw (new FailedException("Could not commit transaction to save information about step.", e));
            }
        }
    }

    private boolean isRunning(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    private String findApp(String app, File dir) {
        // check absolute path
        if (new File(app).exists()) {
            return app;
        }

        // check current folder
        File exe = new File(dir, app);
        if (exe.exists()) {
            return exe.getAbsolutePath();
        }

        // add default extensions to windows apps.
        String[] extensions = new String[] { "" };
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
            extensions = new String[] { "", ".exe", ".com", ".bat" };
        }

        // check library path
        for (String path : System.getProperty("java.library.path").split(File.pathSeparator)) { //$NON-NLS-1$
            for (String ext : extensions) {
                exe = new File(path, app + ext);
                if (exe.exists()) {
                    return exe.getAbsolutePath();
                }
            }
        }

        // check system path
        for (String path : System.getenv("PATH").split(File.pathSeparator)) {
            for (String ext : extensions) {
                exe = new File(path, app + ext);
                if (exe.exists()) {
                    return exe.getAbsolutePath();
                }
            }
        }

        // did not find the app, wait and see what happens.
        return app;
    }
}