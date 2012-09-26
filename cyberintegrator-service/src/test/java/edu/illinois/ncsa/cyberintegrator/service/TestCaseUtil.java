package edu.illinois.ncsa.cyberintegrator.service;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineExecutor;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineImplementation;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.cyberintegrator.executor.commandline.CommandLineOption.Type;
import edu.illinois.ncsa.domain.Person;

public class TestCaseUtil {
    public static Execution createExecution(Person creator, Workflow workflow) {
        Execution execution = new Execution();
        execution.setCreator(creator);
        execution.setWorkflow(workflow);

        return execution;
    }

    public static Workflow createWorkflow(Person creator) throws IOException {
        Workflow workflow = new Workflow();
        workflow.setCreator(creator);
        workflow.setTitle("TEST WORKFLOW");

        workflow.addStep(createWorkflowStep(creator));

        return workflow;
    }

    public static WorkflowStep createWorkflowStep(Person creator) throws IOException {
        WorkflowStep step = new WorkflowStep();
        step.setCreator(creator);
        step.setTool(createCommandLineTool(creator));

        return step;
    }

    public static WorkflowTool createWorkflowTool(Person creator) {
        WorkflowTool tool = new WorkflowTool();
        tool.setCreator(creator);

        WorkflowToolData input = new WorkflowToolData();
        input.setDataId("inp-1");
        input.setMimeType("text/plain");
        input.setTitle("input");
        tool.addInput(input);

        WorkflowToolData output = new WorkflowToolData();
        output.setDataId("out-1");
        output.setMimeType("text/plain");
        output.setTitle("output");
        tool.addOutput(output);

        tool.setExecutor("dummy");

        return tool;
    }

    public static WorkflowTool createCommandLineTool(Person creator) throws IOException {
        WorkflowTool tool = new WorkflowTool();
        tool.setCreator(creator);
        tool.setTitle("Netstat");
        tool.setDescription("Command Line Executor");
        tool.setExecutor(CommandLineExecutor.EXECUTOR_NAME);

        String STDOUT_ID = "output";

        WorkflowToolData stdoutData = new WorkflowToolData();
        stdoutData.setMimeType("text/plain");
        stdoutData.setDataId(STDOUT_ID);
        stdoutData.setTitle("stdout");
        tool.addOutput(stdoutData);

        CommandLineOption option = new CommandLineOption();
        option.setFlag("-n");
        option.setType(Type.VALUE);

        CommandLineImplementation impl = new CommandLineImplementation();
        impl.setExecutable("netstat");
        impl.setCaptureStdOut(STDOUT_ID);
        impl.getCommandLineOptions().add(option);

        tool.setImplementation(new ObjectMapper().writeValueAsString(impl));

        return tool;
    }

}
