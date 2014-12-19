package edu.illinois.ncsa.datawolf.softwareserver.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.AbortException;
import edu.illinois.ncsa.datawolf.FailedException;
import edu.illinois.ncsa.datawolf.executor.java.tool.Dataset;
import edu.illinois.ncsa.datawolf.executor.java.tool.JavaTool;
import edu.illinois.ncsa.datawolf.executor.java.tool.Parameter;
import edu.illinois.ncsa.datawolf.executor.java.tool.Parameter.ParameterType;

/**
 * 
 * @author Chris Navarro
 *
 */
public class SoftwareServerTool implements JavaTool {
    private static final Logger logger  = LoggerFactory.getLogger(SoftwareServerTool.class);
    private File                tmpFolder;
    private String              host;
    private String              application;
    private String              targetMimeType;
    private InputStream         sourceIS;
    private File                output;
    private MimeMap             mimeMap = new MimeMap();
    private String              sourceExtension;

    public void execute() throws AbortException, FailedException {

        // Prepare source file
        File sourceFile;
        try {
            if (sourceExtension.startsWith(".")) {
                sourceFile = File.createTempFile("source", sourceExtension, tmpFolder);
            } else {
                sourceFile = File.createTempFile("source", "." + sourceExtension, tmpFolder);
            }

            // Write the source file to local disk
            writeFile(sourceIS, sourceFile);

            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpClient client = builder.build();

            String taskUri = getTaskURI();
            HttpPost httpPost = new HttpPost(taskUri);

            FileBody fileBody = new FileBody(sourceFile);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addPart("file", fileBody);

            httpPost.setEntity(entityBuilder.build());

            String response = client.execute(httpPost, new BasicResponseHandler());

            output = new File(tmpFolder, "target" + "." + mimeMap.getFileExtension(targetMimeType));
            parseResult(response, output);

        } catch (IOException e) {
            logger.error("Failed to execute software server tool", e);
            throw new FailedException("Failed to execute software server tool", e);
        }
    }

    private void parseResult(String response, File targetFile) throws FailedException {
        Pattern patternTag = Pattern.compile(SoftwareServer.HTML_A_TAG_PATTERN);
        Pattern patternLink = Pattern.compile(SoftwareServer.HTML_A_HREF_TAG_PATTERN);
        Matcher matcherTag = patternTag.matcher(response);

        while (matcherTag.find()) {
            String href = matcherTag.group(1);
            // String linkText = matcherTag.group(2);
            Matcher matcherLink = patternLink.matcher(href);
            while (matcherLink.find()) {
                String link = matcherLink.group(1);

                InputStream is = retrieveFile(link);
                writeFile(is, targetFile);
            }
        }
    }

    private InputStream retrieveFile(String link) {
        InputStream is = null;
        URL url;
        boolean success = false;
        int numRetries = 0;
        while (numRetries < SoftwareServer.ATTEMPTS && !success) {
            try {
                url = new URL(link);
                URLConnection con = url.openConnection();
                is = con.getInputStream();
                success = true;
            } catch (MalformedURLException e) {
                // something went wrong with the URL
                return is;
            } catch (IOException e) {
                // File is not there yet, wait
                try {
                    Thread.sleep(SoftwareServer.WAIT_TIME_MS);
                    numRetries++;
                } catch (InterruptedException e1) {
                    // do nothing
                }
            }
        }

        return is;
    }

    private String getTaskURI() {

        String taskUri = host;
        if (!taskUri.endsWith("/")) {
            taskUri = taskUri.concat("/");
        }

        String mimeExt = mimeMap.getFileExtension(targetMimeType);

        taskUri += SoftwareServer.SOFTWARE + "/" + application + SoftwareServer.TASK_CONVERT + "/" + mimeExt;
        return taskUri;
    }

    private void writeFile(InputStream is, File file) throws FailedException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            int read = 0;
            byte[] buffer = new byte[10240];
            while ((read = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not find input file", e);
            throw new FailedException("Could not find input file", e);
        } catch (IOException e) {
            logger.error("Could not read input file", e);
            throw new FailedException("Could not find input file", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                logger.error("Could not close input file stream", e);
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("Could not close output file stream", e);
                    // Should we throw a failed exception?
                }
            }
        }

    }

    public String getName() {
        return "Software Server";
    }

    public int getVersion() {
        return 1;
    }

    public String getDescription() {
        return "Connects to a software server and performs the selected operation";
    }

    public Collection<Dataset> getInputs() {
        Collection<Dataset> inputs = new HashSet<Dataset>();
        inputs.add(new Dataset("0", "Source File", "File to convert", "image/*"));

        return inputs;
    }

    public void setInput(String id, InputStream is) {
        if (id.equals("0")) {
            this.sourceIS = is;
        }
    }

    public Collection<Dataset> getOutputs() {
        Collection<Dataset> outputs = new HashSet<Dataset>();
        outputs.add(new Dataset("0", "Converted", "Converted file", "image/*"));

        return outputs;
    }

    public InputStream getOutput(String id) {
        if (id.equals("0")) {
            try {
                return new FileInputStream(output);
            } catch (FileNotFoundException e) {
                logger.error("Error locating output file", e);
            }
        }

        return null;
    }

    public Collection<Parameter> getParameters() {
        String[] tmpOptions = new String[] { "image/gif", "image/bmp" };

        Collection<Parameter> parameters = new HashSet<Parameter>();
        parameters.add(new Parameter("0", "Host", "Host to launch job", ParameterType.STRING, "http://localhost:8182", true, false));
        parameters.add(new Parameter("1", "Application", "Application to run", ParameterType.STRING, "ImageMagick", true, false));
        parameters.add(new Parameter("2", "Source Extension", "Source file extension", "png", true, false));
        parameters.add(new Parameter("3", "Target Mime Type", "Select target mime type", tmpOptions[0], false, false, tmpOptions));

        return parameters;
    }

    public void setParameter(String id, String value) {
        if (id.equals("0")) {
            this.host = value;
        } else if (id.equals("1")) {
            this.application = value;
        } else if (id.equals("2")) {
            this.sourceExtension = value;
        } else if (id.equals("3")) {
            this.targetMimeType = value;
        }
    }

    public void setTempFolder(File folder) {
        this.tmpFolder = folder;
    }

}
