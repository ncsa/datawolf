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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
    public static Workflow createDTSWorkflow(String fileId, String fenceURL, String token, Person creator) throws Exception {
        // Find out which extractors were ran on the specified file
        List<String> dtsExtractors = getDTSTools(fileId, fenceURL, token);

        // Create empty workflow
        Workflow workflow = createWorkflow("workflow-" + fileId, "workflow representing dts extractors ran on a file", creator);

        // Get file metadata and technical metadata
        // Is there additional metadata?
        JsonObject metadata = getFileMetadata(fileId, fenceURL, token);

        JsonElement technicalMetadata = getTechnicalMetadata(fileId, fenceURL, token);
        metadata.add("technicalmetadata", technicalMetadata);

        // Format the json attached to the tools and workflow
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Metadata per extractor
        Map<String, String> extractorMetadata = new HashMap<String, String>();

        // Handle preview metadata
        JsonArray jsonArray = metadata.get("previews").getAsJsonArray();
        for (int index = 0; index < jsonArray.size(); index++) {
            JsonObject object = jsonArray.get(index).getAsJsonObject();
            String extractorId = object.get("extractor_id").getAsString();

            // Preserve the metadata key
            JsonObject preview = new JsonObject();
            preview.add("previews", object);

            // Store the preview metadata by extractor id
            extractorMetadata.put(extractorId, gson.toJson(preview));
        }

        // Handle technical metadata
        jsonArray = metadata.get("technicalmetadata").getAsJsonArray();
        for (int index = 0; index < jsonArray.size(); index++) {
            JsonObject object = jsonArray.get(index).getAsJsonObject();
            String extractorId = object.get("extractor_id").getAsString();

            // Preserve the metadata key
            JsonObject dtsTechnicalMetadata = new JsonObject();
            dtsTechnicalMetadata.add("technicalmetadata", object);

            // Store the technical metadata by extractor id
            extractorMetadata.put(extractorId, gson.toJson(dtsTechnicalMetadata));
        }

        workflow.setDescription(gson.toJson(metadata));

        // Step inputs/outputs/parameters
        Map<String, String> inputs = null;
        Map<String, String> outputs = null;
        Map<String, String> parameters = null;

        // Tool inputs/outputs/parameters
        List<WorkflowToolParameter> toolParameters = null;
        List<WorkflowToolData> toolInputs = null;
        List<WorkflowToolData> toolOutputs = null;

        Set<FileDescriptor> blobs = null;

        FileStorage fileStorage = Persistence.getBean(FileStorage.class);
        for (String extractor : dtsExtractors) {
            toolParameters = new ArrayList<WorkflowToolParameter>();
            toolParameters.add(createWorkflowToolParameter("Fence URL", "Fence Hostname", false, ParameterType.STRING, false, fenceURL));
            toolParameters.add(createWorkflowToolParameter("Extractor", "Extractor to run", false, ParameterType.STRING, true, extractor));
            toolParameters.add(createWorkflowToolParameter("File id", "ID of the file to process", false, ParameterType.STRING, false, fileId));
            toolParameters.add(createWorkflowToolParameter("Token", "Authorization token", false, ParameterType.STRING, false, ""));

            List<CommandLineOption> commandlineOptions = new ArrayList<CommandLineOption>();
            String executable = "./dts-script.sh";

            parameters = new HashMap<String, String>();
            for (WorkflowToolParameter parameter : toolParameters) {
                parameters.put(parameter.getParameterId(), UUID.randomUUID().toString());
                commandlineOptions.add(createCommandlineOption(Type.PARAMETER, "", "", parameter.getParameterId(), null, null, true));
            }

            toolInputs = new ArrayList<WorkflowToolData>();
            toolOutputs = new ArrayList<WorkflowToolData>();

            // Capture Standard Out from tool
            WorkflowToolData stdOut = createWorkflowToolData("stdout", "stdout log file", "");
            toolOutputs.add(stdOut);
            outputs = new HashMap<String, String>();
            for (WorkflowToolData output : toolOutputs) {
                outputs.put(output.getDataId(), UUID.randomUUID().toString());
            }

            InputStream is = new ByteArrayInputStream(getDTSShellScript().getBytes(StandardCharsets.UTF_8));
            FileDescriptor fd = fileStorage.storeFile("dts-script.sh", is);

            CommandLineImplementation impl = createCommandlineImplementation(executable, stdOut.getDataId(), null, false, commandlineOptions, null);
            blobs = new HashSet<FileDescriptor>();
            blobs.add(fd);

            String toolDescription = "dts extractor - no metadata found for this tool";
            if (extractorMetadata.containsKey(extractor)) {
                toolDescription = extractorMetadata.get(extractor);
            }
            // Create the workflow tool that wraps the dts extractor
            WorkflowTool tool = createWorkflowTool(extractor, toolDescription, "1.0", creator, toolParameters, toolInputs, toolOutputs, blobs, BeanUtil.objectToJSON(impl), "commandline");

            // Create the workflow step to setup the tool
            WorkflowStep step = createWorkflowStep(extractor, creator, tool, inputs, outputs, parameters);
            workflow.addStep(step);
        }
        return workflow;
    }

    /**
     * Create an empty workflow
     * 
     * @param title
     *            Title of workflow
     * @param description
     *            description of workflow
     * @param creator
     *            workflow creator
     * @return Empty workflow
     */
    public static Workflow createWorkflow(String title, String description, Person creator) {
        Workflow workflow = new Workflow();
        workflow.setTitle(title);
        workflow.setDescription(description);
        workflow.setCreator(creator);

        return workflow;
    }

    /**
     * Create workflow step
     * 
     * @param title
     *            step title
     * @param creator
     *            step creator
     * @param tool
     *            workflow tool associated with the step
     * @param inputs
     *            step inputs
     * @param outputs
     *            step outputs
     * @param parameters
     *            step parameters
     * @return workflow step
     */
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

    /**
     * Create workflow tool
     * 
     * @param title
     *            tool title
     * @param description
     *            tool description
     * @param version
     *            tool version
     * @param creator
     *            tool creator
     * @param parameters
     *            tool parameters
     * @param inputs
     *            tool inputs
     * @param outputs
     *            tool outputs
     * @param blobs
     *            file blobs associated with the tool
     * @param impl
     *            tool implementation
     * @param executor
     *            tool executor type
     * @return Workflow tool
     */
    public static WorkflowTool createWorkflowTool(String title, String description, String version, Person creator, List<WorkflowToolParameter> parameters, List<WorkflowToolData> inputs,
            List<WorkflowToolData> outputs, Set<FileDescriptor> blobs, String impl, String executor) {
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

    /**
     * Create workflow tool parameter
     * 
     * @param title
     *            parameter title
     * @param description
     *            parameter description
     * @param allowNull
     *            parameter accepts null
     * @param type
     *            parameter type
     * @param hidden
     *            is parameter hidden
     * @param value
     *            initial parameter value
     * @return Workflow tool parameter
     */
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

    /**
     * Create workflow tool data
     * 
     * @param title
     *            tool data title
     * @param description
     *            tool data description
     * @param mimeType
     *            tool data mime type
     * @return Workflow tool data
     */
    public static WorkflowToolData createWorkflowToolData(String title, String description, String mimeType) {
        WorkflowToolData toolData = new WorkflowToolData();
        toolData.setTitle(title);
        toolData.setDescription(description);
        toolData.setMimeType(mimeType);
        return toolData;
    }

    /**
     * Get list of DTS tools
     * 
     * @param fileId
     *            file id
     * @param fenceURL
     *            BD-API gateway URL
     * @param token
     *            authorization token
     * @return List of DTS tools
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static List<String> getDTSTools(String fileId, String fenceURL, String token) throws ClientProtocolException, IOException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();

        String extractionEndpoint = fenceURL;
        if (fenceURL.endsWith("/")) {
            extractionEndpoint = fenceURL + "dts/api/extractions/" + fileId + "/status";
        } else {
            extractionEndpoint = fenceURL + "/dts/api/extractions/" + fileId + "/status";
        }
        HttpGet httpGet = new HttpGet(extractionEndpoint);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, token);

        BasicResponseHandler responseHandler = new BasicResponseHandler();

        String response = null;
        List<String> extractors = new ArrayList<String>();
        try {
            response = client.execute(httpGet, responseHandler);
            JsonElement jsonResponse = new JsonParser().parse(response);
            JsonObject object = jsonResponse.getAsJsonObject();
            Iterator<Entry<String, JsonElement>> set = object.entrySet().iterator();
            while (set.hasNext()) {
                Entry<String, JsonElement> element = set.next();
                if (!element.getKey().equalsIgnoreCase("Status")) {
                    extractors.add(element.getKey());
                }
            }
        } catch (ClientProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }

        return extractors;
    }

    /**
     * Get file metadata from file extraction
     * 
     * @param fileId
     *            file id
     * @param fenceURL
     *            BD-API gateway URL
     * @param token
     *            authorization token
     * @return file metadata in json format
     * @throws ClientProtocolException
     * @throws IOException
     */
    private static JsonObject getFileMetadata(String fileId, String fenceURL, String token) throws ClientProtocolException, IOException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();

        String extractionEndpoint = fenceURL;
        if (fenceURL.endsWith("/")) {
            extractionEndpoint = fenceURL + "dts/api/extractions/" + fileId + "/metadata";
        } else {
            extractionEndpoint = fenceURL + "/dts/api/extractions/" + fileId + "/metadata";
        }

        HttpGet httpGet = new HttpGet(extractionEndpoint);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, token);

        BasicResponseHandler responseHandler = new BasicResponseHandler();
        String response = null;
        try {
            response = client.execute(httpGet, responseHandler);
            JsonElement jsonResponse = new JsonParser().parse(response);

            return jsonResponse.getAsJsonObject();
        } catch (ClientProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Get technical metadata for file extraction
     * 
     * @param fileId
     *            file id
     * @param fenceURL
     *            BD-API gateway URL
     * @param token
     *            authorization token
     * @return technical metadata in json format
     * @throws ClientProtocolException
     * @throws IOException
     */
    private static JsonElement getTechnicalMetadata(String fileId, String fenceURL, String token) throws ClientProtocolException, IOException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();

        String extractionEndpoint = fenceURL;
        if (fenceURL.endsWith("/")) {
            extractionEndpoint = fenceURL + "dts/api/extractions/" + fileId + "/metadata";
        } else {
            extractionEndpoint = fenceURL + "/dts/api/files/" + fileId + "/technicalmetadatajson";
        }

        HttpGet httpGet = new HttpGet(extractionEndpoint);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, token);

        BasicResponseHandler responseHandler = new BasicResponseHandler();
        String response = null;
        try {
            response = client.execute(httpGet, responseHandler);
            JsonElement jsonResponse = new JsonParser().parse(response);

            return jsonResponse;
        } catch (ClientProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Create command line option
     * 
     * @param type
     *            command line option type
     * @param value
     *            initial value of command line option
     * @param flag
     *            flag associated with the command line option
     * @param optionId
     *            command line option id
     * @param inputOutput
     *            specify whether command line option is an input, output or
     *            both
     * @param filename
     *            filename for command line option, if applicable
     * @param commandline
     *            pass option at the command line
     * @return Command line option
     */
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

    /**
     * Create command line implementation based on options, standard out/error
     * logs, environment, etc
     * 
     * @param executable
     *            Executable to run
     * @param captureStdOut
     *            data id to capture standard out
     * @param captureStdErr
     *            data id to capture standard error
     * @param joinStdOutStdErr
     *            Join standard out and error to a single file
     * @param commandLineOptions
     *            command line options
     * @param env
     *            environment variables to set before execution
     * @return Commandline implementation description for the tool
     */
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
     * Shell script to run DTS extractor
     * 
     * @return shell script for running DTS extractor
     */
    public static String getDTSShellScript() {
        String script = "#!/bin/bash" + "\n\n" + "HOST=$1 \n" + "extractor=$2 \n" + "fileId=$3 \n" + "TOKEN=$4 \n" + "url=$HOST/dts/api/files/$fileId/extractions \n\n"
                + "json=\"{\"\\\"extractor\"\\\" : \"\\\"$extractor\"\\\"}\"" + "\n\n"
                + "curl -i -X POST -H \"Accept:application/json\" -H \"Content-type: application/json\" -H \"Authorization: $TOKEN\" --data \"$json\" $url \n";

        return script;
    }
}
