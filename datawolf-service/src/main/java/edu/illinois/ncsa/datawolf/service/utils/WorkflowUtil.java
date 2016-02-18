package edu.illinois.ncsa.datawolf.service.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter.ParameterType;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineImplementation;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption.InputOutput;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption.Type;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Persistence;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.util.BeanUtil;

public class WorkflowUtil {

    private static final Logger log = LoggerFactory.getLogger(WorkflowUtil.class);

    /**
     * Creates a workflow representing the DTS extractors ran on a file
     * 
     * @param fileId
     *            - id of the file to create the workflow for
     * @param dtsURL
     *            - DTS URL to communicate with
     * @param creator
     *            - user to create the workflow under
     * @return workflow representing DTS extractors
     * @throws Exception
     */
    public static Workflow createDTSWorkflow(String fileId, String dtsURL, Person creator) throws Exception {
        // DTS script
        FileStorage fileStorage = Persistence.getBean(FileStorage.class);

        Workflow workflow = createWorkflow("workflow-" + fileId, "workflow representing dts extractors ran on a file", creator);
        // Find out which extractors were ran
        List<String> dtsExtractors = getDTSTools(fileId, dtsURL);

        // Step inputs/outputs/parameters
        Map<String, String> inputs = null;
        Map<String, String> outputs = null;
        Map<String, String> parameters = null;

        // Tool inputs/outputs/parameters
        List<WorkflowToolParameter> toolParameters = null;
        List<WorkflowToolData> toolInputs = null;
        List<WorkflowToolData> toolOutputs = null;

        Set<FileDescriptor> blobs = null;

        for (String extractor : dtsExtractors) {
            toolParameters = new ArrayList<WorkflowToolParameter>();
            toolParameters.add(createWorkflowToolParameter("DTS Host", "DTS Hostname", false, ParameterType.STRING, false, dtsURL));
            toolParameters.add(createWorkflowToolParameter("Extractor", "extractor to run", false, ParameterType.STRING, true, extractor));
            toolParameters.add(createWorkflowToolParameter("File id", "ID of the file to process", false, ParameterType.STRING, false, fileId));
            toolParameters.add(createWorkflowToolParameter("Key", "Secret key", false, ParameterType.STRING, false, ""));
            // TODO add a parameter for passing in comma separated key/value
            // parameter pairs for the extractor

            List<CommandLineOption> commandlineOptions = new ArrayList<CommandLineOption>();
            String executable = "./dts-script.sh";

            parameters = new HashMap<String, String>();
            for (WorkflowToolParameter parameter : toolParameters) {
                parameters.put(parameter.getParameterId(), UUID.randomUUID().toString());
                commandlineOptions.add(createCommandlineOption(Type.PARAMETER, "", "", parameter.getParameterId(), null, null, true));
            }

            toolOutputs = new ArrayList<WorkflowToolData>();

            // Std Out
            WorkflowToolData stdOut = createWorkflowToolData("stdout", "stdout log file", "");
            toolOutputs.add(stdOut);
            outputs = new HashMap<String, String>();
            for (WorkflowToolData output : toolOutputs) {
                outputs.put(output.getDataId(), UUID.randomUUID().toString());
            }

            InputStream is = new ByteArrayInputStream(getDTSShellScript().getBytes(StandardCharsets.UTF_8));
            FileDescriptor fd = fileStorage.storeFile("dts-script.sh", is);
            log.warn("File descriptor = " + fd.getDataURL());

            CommandLineImplementation impl = createCommandlineImplementation(executable, stdOut.getDataId(), null, false, commandlineOptions, null);
            blobs = new HashSet<FileDescriptor>();
            blobs.add(fd);
            WorkflowTool tool = createWorkflowTool(extractor, "dts extractor", "1.0", creator, toolParameters, toolInputs, toolOutputs, blobs, BeanUtil.objectToJSON(impl));
            WorkflowStep step = createWorkflowStep(extractor, creator, tool, inputs, outputs, parameters);
            workflow.addStep(step);
        }
        return workflow;
    }

    public static Workflow createWorkflow(String title, String description, Person creator) {
        Workflow workflow = new Workflow();
        workflow.setTitle(title);
        workflow.setDescription(description);
        workflow.setCreator(creator);

        return workflow;
    }

    public static WorkflowStep createWorkflowStep(String title, Person creator, WorkflowTool tool, Map<String, String> inputs, Map<String, String> outputs, Map<String, String> parameters) {
        WorkflowStep step = new WorkflowStep();
        step.setTitle(title);
        step.setCreator(creator);
        step.setTool(tool);
        step.setInputs(inputs);
        step.setOutputs(outputs);
        step.setParameters(parameters);

        return step;
    }

    public static WorkflowTool createWorkflowTool(String title, String description, String version, Person creator, List<WorkflowToolParameter> parameters, List<WorkflowToolData> inputs,
            List<WorkflowToolData> outputs, Set<FileDescriptor> blobs, String impl) {
        String executor = "commandline";
        WorkflowTool tool = new WorkflowTool();
        tool.setTitle(title);
        tool.setDescription(description);
        tool.setVersion(version);
        tool.setExecutor(executor);
        tool.setCreator(creator);
        tool.setParameters(parameters);
        tool.setInputs(inputs);
        tool.setOutputs(outputs);
        tool.setImplementation(impl);
        tool.setBlobs(blobs);

        return tool;
    }

    public static WorkflowToolParameter createWorkflowToolParameter(String title, String description, boolean allowNull, ParameterType type, boolean hidden, String value) {
        WorkflowToolParameter parameter = new WorkflowToolParameter();
        parameter.setTitle(title);
        parameter.setDescription(description);
        parameter.setAllowNull(allowNull);
        parameter.setType(type);
        parameter.setHidden(hidden);
        parameter.setValue(value);

        return parameter;
    }

    public static WorkflowToolData createWorkflowToolData(String title, String description, String mimeType) {
        WorkflowToolData toolData = new WorkflowToolData();
        toolData.setTitle(title);
        toolData.setDescription(description);
        toolData.setMimeType(mimeType);
        return toolData;
    }

    public static List<String> getDTSTools(String fileId, String dtsURL) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
        String extractionEndpoint = dtsURL + "/api/extractions/" + fileId + "/status";

        HttpGet httpGet = new HttpGet(extractionEndpoint);
        httpGet.setHeader("Content-type", "application/json");

        BasicResponseHandler responseHandler = new BasicResponseHandler();

        String response = null;
        List<String> extractors = new ArrayList<String>();
        try {
            response = client.execute(httpGet, responseHandler);
            JsonElement jsonResponse = new JsonParser().parse(response);
            JsonObject object = jsonResponse.getAsJsonObject();
            log.warn("Response = " + response);
            Iterator<Entry<String, JsonElement>> set = object.entrySet().iterator();
            while (set.hasNext()) {
                Entry<String, JsonElement> element = set.next();
                if (!element.getKey().equalsIgnoreCase("Status")) {
                    extractors.add(element.getKey());
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractors;
    }

    public static CommandLineOption createCommandlineOption(Type type, String value, String flag, String optionId, InputOutput inputOutput, String filename, boolean commandline) {
        CommandLineOption option = new CommandLineOption();
        option.setCommandline(commandline);
        option.setType(type);
        option.setValue(value);
        option.setFlag(flag);
        option.setOptionId(optionId);

        if (filename != null) {
            option.setFilename(filename);
        }
        if (inputOutput != null) {
            option.setInputOutput(inputOutput);
        }

        return option;
    }

    public static CommandLineImplementation createCommandlineImplementation(String executable, String captureStdOut, String captureStdErr, boolean joinStdOutStdErr,
            List<CommandLineOption> commandLineOptions, Map<String, String> env) {

        CommandLineImplementation impl = new CommandLineImplementation();
        impl.setCaptureStdErr(captureStdErr);
        impl.setCaptureStdOut(captureStdOut);
        impl.setCommandLineOptions(commandLineOptions);
        impl.setEnv(env);
        impl.setExecutable(executable);
        impl.setJoinStdOutStdErr(joinStdOutStdErr);

        return impl;
    }

    /**
     * 
     * @return
     */
    // TODO add support for passing in parameters
    public static String getDTSShellScript() {
        String script = "#!/bin/bash" + "\n\n" + "HOST=$1 \n" + "extractor=$2 \n" + "fileId=$3 \n" + "KEY=$4 \n" + "url=$HOST/api/extractions \n\n"
                + "json=\"{\"\\\"extractor\"\\\" : \"\\\"$extractor\"\\\", \"\\\"file_id\"\\\" : \"\\\"$fileId\"\\\", \"\\\"parameters\"\\\" : {}  }\"" + "\n\n"
                + "curl -i -X POST -H \"Accept:application/json\" -H \"Content-type: application/json\" --data \"$json\" $url?key=$KEY \n";

        return script;
    }
}
