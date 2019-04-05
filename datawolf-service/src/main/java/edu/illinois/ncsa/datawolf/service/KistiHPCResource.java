package edu.illinois.ncsa.datawolf.service;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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

import edu.illinois.ncsa.datawolf.domain.HPCJobInfo;
import edu.illinois.ncsa.datawolf.domain.dao.HPCJobInfoDao;
import edu.illinois.ncsa.datawolf.executor.hpc.ssh.SSHInfo;
import edu.illinois.ncsa.datawolf.executor.hpc.ssh.SSHSession;
import edu.illinois.ncsa.datawolf.executor.hpc.util.NonNLSConstants;
import edu.illinois.ncsa.datawolf.executor.hpc.util.SshUtils;

@Path("kisti")
public class KistiHPCResource {
    private Logger        log     = LoggerFactory.getLogger(KistiHPCResource.class);

    private static int    RETRIES = 5;

    @Inject
    private HPCJobInfoDao hpcJobInfoDao;

    @GET
    @Path("{execution-id}/checkhpcfile")
    @Produces({ MediaType.TEXT_PLAIN })
    public String checkHpcFile(@PathParam("execution-id") String executionId, @QueryParam("file") @DefaultValue("error.rlt") String file) {
        log.info("Checking " + file + " for convergence graph of the exeuction: " + executionId);
        SSHSession session = null;
        String command = null;
        try {
            // getting HPCJobInfo Bean
            List<HPCJobInfo> hpcJobInfoList = hpcJobInfoDao.findByExecutionId(executionId);

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

    @GET
    @Path("{execution-id}/hpcfile")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response getHpcFile(@PathParam("execution-id") String executionId, @QueryParam("file") @DefaultValue("error.rlt") String file) {
        log.info("Getting " + file + " for convergence graph of the exeuction: " + executionId);
        SSHSession session = null;
        try {
            // getting HPCJobInfo Bean
            List<HPCJobInfo> hpcJobInfoList = hpcJobInfoDao.findByExecutionId(executionId);

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
}
