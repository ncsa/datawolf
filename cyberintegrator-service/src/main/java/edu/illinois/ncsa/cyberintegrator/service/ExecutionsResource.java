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
package edu.illinois.ncsa.cyberintegrator.service;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import edu.illinois.ncsa.cyberintegrator.Engine;
import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.HPCJobInfo;
import edu.illinois.ncsa.cyberintegrator.domain.LogFile;
import edu.illinois.ncsa.cyberintegrator.domain.Submission;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.ssh.SSHInfo;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.ssh.SSHSession;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.util.NonNLSConstants;
import edu.illinois.ncsa.cyberintegrator.executor.hpc.util.SshUtils;
import edu.illinois.ncsa.cyberintegrator.springdata.ExecutionDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.HPCJobInfoDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.LogFileDAO;
import edu.illinois.ncsa.cyberintegrator.springdata.WorkflowDAO;
import edu.illinois.ncsa.springdata.PersonDAO;
import edu.illinois.ncsa.springdata.SpringData;

@Path("/executions")
public class ExecutionsResource {

    Logger             log     = LoggerFactory.getLogger(ExecutionsResource.class);
    private static int RETRIES = 5;

    /**
     * Create execution via Submission JSON
     * 
     * @param submission
     *            a submission JSON object
     * @return
     *         execution Id
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_PLAIN })
    public String createExecution(Submission submission) {

        log.debug("POST /executions received");
        Execution execution = new Execution();
        // find workflow
        WorkflowDAO workflowDAO = SpringData.getBean(WorkflowDAO.class);
        Workflow workflow = workflowDAO.findOne(submission.getWorkflowId());
        if (workflow != null) {
            execution.setWorkflow(workflow);
            execution.setTitle(submission.getTitle());
            execution.setDescription(submission.getDescription());
            execution.setCreator(SpringData.getBean(PersonDAO.class).findOne(submission.getCreatorId()));
            for (Entry<String, String> param : submission.getParameters().entrySet()) {
                execution.setParameter(param.getKey(), param.getValue());
            }
            for (Entry<String, String> dataset : submission.getDatasets().entrySet()) {
                execution.setDataset(dataset.getKey(), dataset.getValue());
            }
            SpringData.getBean(ExecutionDAO.class).save(execution);

            // start execution
            SpringData.getBean(Engine.class).execute(execution);

            return execution.getId();
        } else {
            String error = "Workflow " + submission.getWorkflowId() + " not found.";
            log.error(error);
            return error;
        }
    }

    /**
     * Get all executions
     * 
     * @param size
     *            number of workflows per page (default: -1)
     * @param page
     *            page number starting 0
     * @param email
     *            email of creator
     * @return
     *         list of executions
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<Execution> getExecutions(@QueryParam("size") @DefaultValue("-1") int size, @QueryParam("page") @DefaultValue("0") int page, @QueryParam("email") @DefaultValue("") String email,
            @QueryParam("showdeleted") @DefaultValue("false") boolean showdeleted) {
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);

        Sort sort = new Sort(Sort.Direction.DESC, "date");

        // without paging
        if (size < 1) {
            if (email.equals("")) {
                Iterable<Execution> tmp;
                if (showdeleted) {
                    tmp = exedao.findAll(sort);
                } else {
                    tmp = exedao.findByDeleted(false, sort);
                }
                ArrayList<Execution> list = new ArrayList<Execution>();
                for (Execution d : tmp) {
                    list.add(d);
                }
                return list;
            } else {
                if (showdeleted) {
                    return exedao.findByCreatorEmail(email, sort);
                } else {
                    return exedao.findByCreatorEmailAndDeleted(email, false, sort);
                }
            }

        } else { // with paging
            Page<Execution> results = null;
            if (email.equals("")) {
                if (showdeleted) {
                    results = exedao.findAll(new PageRequest(page, size, sort));
                } else {
                    results = exedao.findByDeleted(false, new PageRequest(page, size, sort));
                }
            } else {
                if (showdeleted) {
                    results = exedao.findByCreatorEmail(email, new PageRequest(page, size, sort));
                } else {
                    results = exedao.findByCreatorEmailAndDeleted(email, false, new PageRequest(page, size, sort));
                }
            }
            return results.getContent();
        }

    }

    /**
     * 
     * Get a execution by Id
     * 
     * @param executionId
     *            execution id
     * @return
     *         a execution in JSON
     */
    @GET
    @Path("{execution-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Execution getExecution(@PathParam("execution-id") String executionId) {
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);
        return exedao.findOne(executionId);
    }

    /**
     * Delete execution by id
     * 
     * @param executionId
     *            execution id
     * @throws Exception
     */
    @DELETE
    @Path("{execution-id}")
    public void deleteExecution(@PathParam("execution-id") @DefaultValue("") String executionId) throws Exception {
        if ("".equals(executionId)) {
            throw (new Exception("Invalid id passed in."));
        }
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);
        Execution execution = exedao.findOne(executionId);
        if (execution == null) {
            throw (new Exception("Invalid id passed in."));
        }
        execution.setDeleted(true);
        exedao.save(execution);
    }

    @GET
    @Path("{execution-id}/checkhpcfile")
    @Produces({ MediaType.TEXT_PLAIN })
    public String checkHpcFile(@PathParam("execution-id") String executionId, @QueryParam("file") @DefaultValue("error.rlt") String file) {
        log.info("Checking " + file + " for convergence graph of the exeuction: " + executionId);
        SSHSession session = null;
        String command = null;
        try {
            // getting HPCJobInfo Bean
            HPCJobInfoDAO hDao = SpringData.getBean(HPCJobInfoDAO.class);
            List<HPCJobInfo> hpcJobInfoList = hDao.findByExecutionId(executionId);

            if (hpcJobInfoList.isEmpty()) {
                log.error("Can't find hpcJogInfo bean for execution: " + executionId);
                return "NO";
            }

            HPCJobInfo hpcJobInfo = hpcJobInfoList.get(0);
            String workingDir = hpcJobInfo.getWorkingDir();

            String fileFullPath = workingDir + "/result/" + file;

            command = "test -e " + fileFullPath + " && echo \"YES\"";

            StringBuffer stdout = new StringBuffer();
            StringBuffer stderr = new StringBuffer();

            String contactURI = "ssh://cyber.kisti.re.kr:22002";
            String user = "pdynam";
            String userHome = System.getProperty("user.home");

            session = maybeGetSession(new URI(contactURI), user, userHome);

            SshUtils.exec(session, command, stdout, stderr);
            if ("YES".equals(stdout.toString().trim().toUpperCase())) {
                if (session != null) {
                    session.close();
                }
                return "YES";
            }

            if (session != null) {
                session.close();
            }
            return "NO";

        } catch (IllegalArgumentException e) {
            log.error("SSH error to execute the command " + command, e);
        } catch (Exception e) {
            log.error("SSH error to execute the command " + command, e);
        } finally {
            // This makes sure the session gets closed if exception thrown
            if (session != null) {
                session.close();
            }
        }
        return "NO";

    }

    // TODO Move this per CBI-486
    @GET
    @Path("{execution-id}/hpcfile")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response getHpcFile(@PathParam("execution-id") String executionId, @QueryParam("file") @DefaultValue("error.rlt") String file) {
        log.info("Getting " + file + " for convergence graph of the exeuction: " + executionId);
        SSHSession session = null;
        try {
            // getting HPCJobInfo Bean
            HPCJobInfoDAO hDao = SpringData.getBean(HPCJobInfoDAO.class);
            List<HPCJobInfo> hpcJobInfoList = hDao.findByExecutionId(executionId);

            if (hpcJobInfoList.isEmpty()) {
                log.error("Can't find hpcJogInfo bean for execution: " + executionId);
                return Response.status(500).entity("Can't create image (id:" + executionId + ")").build();
            }

            HPCJobInfo hpcJobInfo = hpcJobInfoList.get(0);
            String workingDir = hpcJobInfo.getWorkingDir();

            String fileFullPath = workingDir + "/result/" + file;

            // sftp to get the file by using fileFullPath
            final File tempfile = File.createTempFile("error", ".rlt");

            String contactURI = "ssh://cyber.kisti.re.kr:22002";
            String user = "pdynam";
            String userHome = System.getProperty("user.home");

            session = maybeGetSession(new URI(contactURI), user, userHome);

            boolean success = false;
            int attempts = 0;
            while (!success && attempts < RETRIES) {
                try {
                    SshUtils.copyFrom(fileFullPath, tempfile.getAbsolutePath(), session);
                    success = true;
                } catch (Exception e) {
                    attempts++;
                    Thread.sleep(1000);
                }
            }
            // Create chart image

            File dataDir = new File("/tmp/", executionId);
            dataDir.mkdirs();

            File imageFilename = new File(dataDir, executionId + ".jpg");

            boolean created = createChartImage(tempfile, imageFilename);

            final File tmp = imageFilename;
            if (created) {
                // sending the file stream
                ResponseBuilder response = Response.ok(new FileInputStream(tmp) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        tmp.delete();
                    }
                });
                response.type("image/jpeg");
                return response.build();
            } else {
                log.error("Couldn't create image (id:" + executionId + ")");
                return Response.status(500).entity("Can't create image (id:" + executionId + ")").build();
            }
        } catch (Exception e) {
            log.error("Couldn't create image (id:" + executionId + ")", e);
            return Response.status(500).entity("Can't create image (id:" + executionId + ")").build();

        } catch (Throwable e) {
            log.error("Can't sftp file from remote machine (id:" + executionId + ")", e);
            return Response.status(500).entity("Can't sftp file from remote machine").build();
        } finally {
            // This makes sure the session gets closed if exception thrown
            if (session != null) {
                session.close();
            }
        }
    }

    protected SSHSession maybeGetSession(URI contact, String user, String userHome) throws Exception {
        String scheme = contact.getScheme();
        if (scheme != null && scheme.indexOf(NonNLSConstants.SSH) >= 0) {
            SSHInfo info = new SSHInfo();
            info.setUser(user);
            info.setSshHome(new File(userHome, NonNLSConstants.DOT_SSH));
            return new SSHSession(contact, info);
        }
        return null;
    }

    public boolean createChartImage(File dataFilePath, File imageFilePath) {
        try {
            JFreeChart chart = createLineGraph(dataFilePath);
            ChartUtilities.saveChartAsJPEG(imageFilePath, chart, 480, 360);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private JFreeChart createLineGraph(File f) throws Exception {

        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = null;

        String variableLine = reader.readLine();
        String[] parseVariables = variableLine.split(",");
        String xVariable = parseVariables[0].substring(12, parseVariables[0].length());
        String yVariable = parseVariables[1].trim();

        // unnecessary data
        reader.readLine();
        reader.readLine();

        // read point data
        List<String> xValues = new ArrayList<String>();
        List<String> yValues = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            String x = parts[0].trim();
            String y = parts[1].trim();

            xValues.add(x);
            yValues.add(y);
        }
        reader.close();

        XYDataset dataset = createDataset(xValues, yValues);
        JFreeChart chart = createChart(xVariable, yVariable, dataset);
        return chart;

    }

    private JFreeChart createChart(String xVariable, String yVariable, XYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYLineChart("Convergence Graph", xVariable, yVariable, dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return chart;
    }

    private XYDataset createDataset(List<String> xValues, List<String> yValues) {

        XYSeries series = new XYSeries("error");
        for (int i = 0; i < xValues.size(); i++) {
            String x = xValues.get(i);
            String y = yValues.get(i);
            try {
                Double.parseDouble(y);
                series.add(new XYDataItem(Double.parseDouble(x), Double.parseDouble(y)));
            } catch (NumberFormatException nfe) {
                // received ******** as the number, ignore it
            }

        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    /**
     * Get all logfiles belonging to the execution
     * 
     * @param executionId
     *            a execution Id
     * @return
     *         list of logfile ids
     */
    @GET
    @Path("{execution-id}/logfiles")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<LogFile> getLogfiles(@PathParam("execution-id") String executionId) {
        LogFileDAO logfileDao = SpringData.getBean(LogFileDAO.class);
        return logfileDao.findByExecutionId(executionId);
    }

    /**
     * Get the logfile belonging to a single step of the execution
     * 
     * @param executionId
     *            a execution Id
     * @param stepId
     *            a step Id
     * @return
     *         list of logfile ids
     */
    @GET
    @Path("{execution-id}/logfiles/{step-id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public LogFile getLogfiles(@PathParam("execution-id") String executionId, @PathParam("step-id") String stepId) {
        LogFileDAO logfileDao = SpringData.getBean(LogFileDAO.class);
        return logfileDao.findLogByExecutionIdAndStepId(executionId, stepId);
    }

    /**
     * Get the state all steps in an execution
     * 
     * @param executionId
     *            execution id
     * @return step id and state
     */
    @GET
    @Path("{execution-id}/state")
    @Produces({ MediaType.APPLICATION_JSON })
    public Map<String, State> getState(@PathParam("execution-id") String executionId) {
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);
        Execution e = exedao.findOne(executionId);
        return e.getStepStates();
    }

    /**
     * Start a execution
     * 
     * @param executionId
     *            a execution id
     */
    @PUT
    @Path("{execution-id}/start")
    public void startExecution(@PathParam("execution-id") String executionId) {
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);
        Execution execution = exedao.findOne(executionId);
        Engine engine = SpringData.getBean(Engine.class);
        engine.execute(execution);
        execution = exedao.findOne(executionId);
    }

    /**
     * Pause a execution
     * 
     * @param executionId
     */
    @PUT
    @Path("{execution-id}/pause")
    public void pauseExecution(@PathParam("execution-id") String executionId) {
        // TODO implement pauseExecution per CBI-488
    }

    /**
     * Cancel a execution
     * 
     * @param executionId
     */
    @PUT
    @Path("{execution-id}/cancel")
    public void cancelExecution(@PathParam("execution-id") String executionId, @QueryParam("stepids") @DefaultValue("") String stepIds) {
        Engine engine = SpringData.getBean(Engine.class);

        if (stepIds.equals("")) {
            engine.stop(executionId);
        } else {
            String[] ids = stepIds.split(";");
            engine.stop(executionId, ids);
        }
    }

}
