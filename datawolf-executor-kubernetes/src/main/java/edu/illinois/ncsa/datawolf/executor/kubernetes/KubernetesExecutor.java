package edu.illinois.ncsa.datawolf.executor.kubernetes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.datawolf.AbortException;
import edu.illinois.ncsa.datawolf.FailedException;
import edu.illinois.ncsa.datawolf.RemoteExecutor;
import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.Execution.State;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption.InputOutput;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.util.BeanUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.util.Config;

import javax.inject.Inject;
import javax.inject.Named;

public class KubernetesExecutor extends RemoteExecutor {
    private static Logger       logger        = LoggerFactory.getLogger(KubernetesExecutor.class);
    public static final String  EXECUTOR_NAME = "kubernetes";

    private static final int    gracePeriod   = 3600;

    private String              jobID         = null;
    private V1Job               job           = null;
    private BatchV1Api          batchApi      = null;
    private CoreV1Api           coreApi       = null;
    private File                jobFolder     = null;
    private String              lastlog       = "";
    @Inject
    @Named("kubernetes.cpu")
    // default number of cpus for a single job, can be changed per tool
    private float               cpu           = 2;
    @Inject
    @Named("kubernetes.memory")
    // default memory for a single job, can be changed per tool
    private float               memory        = 4;
    @Inject
    @Named("kubernetes.namespace")
    private String              namespace     = null;
    @Inject
    @Named("kubernetes.pvc")
    private String              pvcName       = null;
    @Inject
    @Named("kubernetes.data")
    private String              dataFolder    = "/home/datawolf/data";


    // Output files specified in the tool description
    // These should be in the folder that is mounted to the docker image
    // and will be saved to file storage
    private Map<String, String> outputfiles   = new HashMap<String, String>();

    static {
        REMOTE_JOB_CHECK_INTERVAL = 5000;
    }

    @Override
    public State submitRemoteJob(File cwd) throws AbortException, FailedException {
        // cwd is ignored, since that is on the local machine where datawolf runs.

        // jobFolder should be a unique folder in the datawolf data folder. This is a shared
        // folder available to datawolf and all kubernetes jobs. The jobID can be used
        // as this magic folder.
        jobID = UUID.randomUUID().toString();
        jobFolder = new File(new File(dataFolder, "jobs"), jobID);
        jobFolder.mkdirs();

        // Similar to LocalExecutor, we need the commands to append to command line
        // that will get passed to the docker image that runs the tool
        ArrayList<String> command = new ArrayList<String>();

        // Tool should contain the docker image to run in the executable field of the
        // tool implementation

        try {
            work.begin();
            WorkflowStep step = workflowStepDao.findOne(getStepId());
            Execution execution = executionDao.findOne(getExecutionId());
            KubernetesToolImplementation impl = BeanUtil.JSONToObject(step.getTool().getImplementation(), KubernetesToolImplementation.class);

            // add the options in order
            for (CommandLineOption option : impl.getCommandLineOptions()) {
                // all have a flag option
                if ((option.getFlag() != null) && !option.getFlag().trim().equals("")) {
                    command.add(option.getFlag());
                }

                switch (option.getType()) {
                case VALUE:
                    // fixed value
                    if ((option.getValue() != null) && !option.getValue().trim().equals("")) {
                        command.add(option.getValue());
                    }
                    break;

                case PARAMETER:
                    // user specified value
                    String value = null;
                    if (execution.hasParameter(step.getParameters().get(option.getOptionId()))) {
                        value = execution.getParameter(step.getParameters().get(option.getOptionId()));
                    } else {
                        value = step.getTool().getParameter(option.getOptionId()).getValue();
                    }
                    if (value != null) {
                        command.add(value);
                    }
                    break;

                case DATA:
                    // pointer to file on disk
                    String filename;
                    if ((option.getFilename() != null) && !option.getFilename().equals("")) {
                        filename = new File(jobFolder, option.getFilename()).getAbsolutePath();
                    } else {
                        filename = File.createTempFile("cbi", ".tmp", jobFolder).getAbsolutePath();
                    }
                    if (filename == null) {
                        throw (new FailedException("Could not create temp file."));
                    }

                    if (option.getInputOutput() != InputOutput.OUTPUT) {
                        String key = step.getInputs().get(option.getOptionId());
                        Dataset ds = datasetDao.findOne(execution.getDataset(key));// option.getOptionId()));
                        if (ds == null) {
                            throw (new AbortException("Dataset is missing."));
                        }

                        // Define read/write buffer
                        byte[] buf = new byte[10240];
                        if (ds.getFileDescriptors().size() == 1) {
                            try (InputStream is = fileStorage.readFile(ds.getFileDescriptors().get(0)); FileOutputStream fos = new FileOutputStream(filename);) {
                                int len = 0;
                                while ((len = is.read(buf)) > 0) {
                                    fos.write(buf, 0, len);
                                }
                            } catch (IOException e) {
                                throw (new FailedException("Could not get input file.", e));
                            }
                        } else {

                            // Create a folder for the datasets
                            File inputFolder = new File(filename);
                            if (inputFolder.exists()) {
                                // For single file, a tmp file got created above; however in this case, we need
                                // a temporary folder to store the files
                                inputFolder.delete();
                            }

                            if (!inputFolder.mkdirs()) {
                                throw (new FailedException("Could not create folder for input files"));
                            }


                            int duplicate = 1;
                            for (FileDescriptor fd : ds.getFileDescriptors()) {
                                String localFileName = fd.getFilename();

                                // Check if file already exists
                                if (new File(inputFolder, localFileName).exists()) {
                                    int fileExtensionIndex = fd.getFilename().lastIndexOf(".");

                                    String name = new File(inputFolder, localFileName).getName();
                                    String fileExtension = "";
                                    if (fileExtensionIndex != -1) {
                                        fileExtension = fd.getFilename().substring(fileExtensionIndex);
                                        logger.debug("File extension is " + fileExtension);
                                    }

                                    localFileName = new File(inputFolder, name + " (" + duplicate + ")" + fileExtension).getAbsolutePath();
                                    duplicate++;
                                } else {
                                    localFileName = new File(inputFolder, localFileName).getAbsolutePath();
                                }

                                logger.debug("Input file path is " + localFileName);

                                try (InputStream is = fileStorage.readFile(fd); FileOutputStream fos = new FileOutputStream(localFileName);) {
                                    int len = 0;
                                    while ((len = is.read(buf)) > 0) {
                                        fos.write(buf, 0, len);
                                    }
                                } catch (IOException e) {
                                    throw (new FailedException("Could not get input file.", e));
                                }
                            }
                        }
                        if (option.isCommandline()) {
                            String datafile = new File("/data", filename.substring(jobFolder.getAbsolutePath().length())).getAbsolutePath();
                            command.add(datafile);
                        }
                    }

                    // Capture the names of the output files we expect after execution
                    if (option.getInputOutput() != InputOutput.INPUT) {
                        outputfiles.put(option.getOptionId(), filename);
                    }

                    break;
                }
            }

            // We have the docker image name to run and all the arguments to pass in
            // We've also copied any input datasets into the image
            // Next we setup the kubernetes specific run pieces
            if (logger.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append(impl.getImage());
                for (String s : command) {
                    sb.append(s);
                    sb.append(" ");
                }
                logger.debug("Executing : " + sb.toString());
            }

            // create api
            ApiClient client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);
            batchApi = new BatchV1Api(client);
            coreApi = new CoreV1Api(client);

            // create the job
            job = new V1Job();
            job.setApiVersion("batch/v1");
            job.setKind("Job");

            // add metadata, name is critical
            V1ObjectMeta metadata = new V1ObjectMeta();
            job.setMetadata(metadata);
            metadata.setNamespace(namespace);
            //metadata.setName("unique name of execution of this step");
            metadata.setName(jobID);
            metadata.putLabelsItem("execution-id", getExecutionId());
            metadata.putLabelsItem("step-id", getStepId());
            metadata.putLabelsItem("step-name", step.getTitle());

            // add spec for a job, allow job to stay around for 1 hour after it finishes
            V1JobSpec jobSpec = new V1JobSpec();
            job.setSpec(jobSpec);
            jobSpec.setTtlSecondsAfterFinished(gracePeriod);
            jobSpec.setBackoffLimit(0);

            V1PodTemplateSpec templateSpec = new V1PodTemplateSpec();
            jobSpec.setTemplate(templateSpec);

            // metadata for pod
            V1ObjectMeta templateMeta = new V1ObjectMeta();
            templateSpec.setMetadata(templateMeta);
            templateMeta.putLabelsItem("job-id", jobID);
            templateMeta.putLabelsItem("execution-id", getExecutionId());
            templateMeta.putLabelsItem("step-id", getStepId());
            templateMeta.putLabelsItem("step-name", step.getTitle());

            // actual job
            V1PodSpec podSpec = new V1PodSpec();
            templateSpec.setSpec(podSpec);
            podSpec.setRestartPolicy("Never");

            // pull secret
            if (impl.getPullSecretName() != null) {
                V1LocalObjectReference pullsecret = new V1LocalObjectReference();
                podSpec.addImagePullSecretsItem(pullsecret);
                pullsecret.setName(impl.getPullSecretName());
            }

            // create the container that will be executed
            V1Container container = new V1Container();
            podSpec.addContainersItem(container);
            // set the name
            container.setName("executor");
            // docker image
            container.setImage(impl.getImage());
            // command line arguments for container
            container.args(command);
            // add any environment variables
            if (!impl.getEnv().isEmpty()) {
                Map<String, String> environment = impl.getEnv();

                for (Map.Entry<String, String> entry : environment.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    V1EnvVar envVar = new V1EnvVar();
                    envVar.setName(key);
                    envVar.setValue(value);
                    container.addEnvItem(envVar);
                }
            }

            // Add user to the environment in case a tool needs this information
            if(execution.getCreator() != null) {
                String user = execution.getCreator().getEmail();
                V1EnvVar envVar = new V1EnvVar();
                envVar.setName(DATAWOLF_USER);
                envVar.setValue(user);
                container.addEnvItem(envVar);
            }

            // add resource limits
            V1ResourceRequirements resources = new V1ResourceRequirements();
            container.setResources(resources);
            String val = impl.getResources().getOrDefault("memory", Float.toString(memory));
            resources.putLimitsItem("memory", new Quantity(val + "Gi"));
            val = impl.getResources().getOrDefault("cpu", Float.toString(cpu));
            resources.putLimitsItem("cpu", new Quantity(val));

            // add volumes, this is a subpath in the datawolf volume
            V1VolumeMount volumeMount = new V1VolumeMount();
            container.addVolumeMountsItem(volumeMount);
            volumeMount.setName("data");
            volumeMount.setMountPath("/data");
            volumeMount.setSubPath(new File("jobs", jobID).toString());

            // add volume, this is the same pvc as mounted to datawolf
            V1Volume volume = new V1Volume();
            podSpec.addVolumesItem(volume);
            volume.setName("data");
            V1PersistentVolumeClaimVolumeSource pvc = new V1PersistentVolumeClaimVolumeSource();
            volume.setPersistentVolumeClaim(pvc);
            pvc.claimName(pvcName);

            // create the actual job
            job = batchApi.createNamespacedJob(namespace, job, null, null, null, null);
        } catch (AbortException e) {
            throw e;
        } catch (FailedException e) {
            // Job could not be submitted, set state to waiting to try again
            logger.info("Job not submitted because the job scheduler appears to be down, will try again shortly...", e);
            return State.WAITING;
            // throw e;
        } catch (Throwable e) {
            throw (new FailedException("Something went wrong trying to submit the job to kubernetes cluster.", e));
        } finally {
            work.end();
        }
        // assume at this point it's queued, but we could check with the kubernetes job
        // to be sure
        return State.QUEUED;
    }

    @Override
    public String getExecutorName() {
        return EXECUTOR_NAME;
    }

    @Override
    public void cancelRemoteJob() {
        try {
            batchApi.deleteNamespacedJob(this.job.getMetadata().getName(), this.job.getMetadata().getNamespace(), null, null, gracePeriod, null, null, null);
        } catch (ApiException e1) {
            logger.error("Unable to delete job from Kubernetes", e1);
        }
    }

    @Override
    public State checkRemoteJob() throws FailedException {
        try {
            V1Job jobstatus = batchApi.readNamespacedJobStatus(job.getMetadata().getName(), this.job.getMetadata().getNamespace(), null);
            V1JobStatus status = jobstatus.getStatus();

            // This isn't quite right
            // If the job is finished, get the log files and any outputs we need to store in
            // DataWolf
            if (status.getActive() != null) {
                if (status.getReady() != 0) {
                    // job is running
                    return State.RUNNING;
                } else {
                    // no pods ready, return previous state
                    return getState();
                }
            } else if (status.getFailed() != null) {
                return State.FAILED;
            } else if (status.getSucceeded() != null) {
                // job is finished
                try {
                    work.begin();
                    WorkflowStep step = workflowStepDao.findOne(getStepId());
                    Execution execution = executionDao.findOne(getExecutionId());
                    KubernetesToolImplementation impl = BeanUtil.JSONToObject(step.getTool().getImplementation(), KubernetesToolImplementation.class);

                    // If there are any changes to the execution - save it
                    boolean saveExecution = false;

                    // save the logfiles
                    getJobLog();

                    // This is a placeholder - we need to find what kind of logs we capture with
                    // kubernetes
                    if (impl.getCaptureStdOut() != null) {
                        Dataset ds = new Dataset();
                        ds.setTitle(step.getTool().getOutput(impl.getCaptureStdOut()).getTitle());
                        ds.setCreator(execution.getCreator());
                        ds = datasetDao.save(ds);

                        ByteArrayInputStream bais = new ByteArrayInputStream(lastlog.getBytes("UTF-8"));
                        FileDescriptor fd = fileStorage.storeFile(step.getTool().getOutput(impl.getCaptureStdOut()).getTitle(), bais, execution.getCreator(), ds);

                        execution.setDataset(step.getOutputs().get(impl.getCaptureStdOut()), ds.getId());
                        saveExecution = true;
                    }

                    // TODO - collect kubernetes job output files and store in datawolf
                    // This is code is copied over from command line executor to give an example of
                    // what's collected after execution for a local execution. We'll need to copy
                    // this from the kubernetes run instead
                    for (Entry<String, String> entry : outputfiles.entrySet()) {
                        try {
                            Dataset ds = new Dataset();
                            ds.setTitle(step.getTool().getOutput(entry.getKey()).getTitle());
                            ds.setCreator(execution.getCreator());
                            ds.setDescription(step.getTool().getOutput(entry.getKey()).getDescription());
                            ds = datasetDao.save(ds);

                            // TODO This needs to be looking inside the mounted folder and either searching
                            if (entry.getValue().contains("*")) {
                                final String matchme = entry.getValue().replace("*", ".*");
                                File[] files = new File(new File(entry.getValue()).getParent()).listFiles(new FileFilter() {
                                    @Override
                                    public boolean accept(File pathname) {
                                        return pathname.getAbsolutePath().matches(matchme);
                                    }
                                });

                                if (files != null) {
                                    // TODO - once optional datasets is merged, add allow null check for optional datasets
//                                if (files.length == 0 && (!step.getTool().getOutput(entry.getKey()).isAllowNull())) {
                                    if (files.length == 0) {
                                        // Required output was not found, fail the step
                                        logger.error("Could not find required output files, failing the workflow step.");
                                        throw new FailedException("Required output files are missing.");
                                    }

                                    for (File file : files) {
                                        logger.debug("adding files to a dataset: " + file);
                                        FileInputStream fis = new FileInputStream(file);
                                        fileStorage.storeFile(file.getName(), fis, execution.getCreator(), ds);
                                        fis.close();
                                    }
                                }

                            } else {
                                FileInputStream fis = new FileInputStream(entry.getValue());
                                fileStorage.storeFile(new File(entry.getValue()).getName(), fis, execution.getCreator(), ds);
                            }
//                            ds = datasetDao.save(ds);

                            execution.setDataset(step.getOutputs().get(entry.getKey()), ds.getId());
                            saveExecution = true;
                        } catch (IOException exc) {
                            logger.warn("Could not store output.", exc);
                        }
                    }
                    if (saveExecution) {
                        executionDao.save(execution);
                    }

                } catch (Throwable e) {
                    throw (new FailedException("Could not run transaction to save information about step.", e));
                } finally {
                    work.end();
                }
                deleteDirectory(jobFolder);
                batchApi.deleteNamespacedJob(job.getMetadata().getName(), this.job.getMetadata().getNamespace(), null, null, null, null, "Foreground", null);
                return State.FINISHED;
            } else {
                logger.debug("STATUS = " + status);
            }
        } catch (ApiException e1) {
            if (e1.getCode() == 404) {
                throw (new FailedException("Job [" + jobID + "] was removed."));
            }
            logger.error("Unable to get a job from Kubernetes (code=" + e1.getCode() + " body=" + e1.getResponseBody() + ")", e1);
        }
        return State.UNKNOWN;
    }

    private void getJobLog() {
        String selector = "job-id=" + jobID;
        InputStream is = null;
        try {
            V1PodList items = coreApi.listNamespacedPod(job.getMetadata().getNamespace(), null, null, null, null, selector, null, null, null, null, null);
            if (items.getItems().size() > 0) {
                PodLogs logs = new PodLogs();
                is = logs.streamNamespacedPodLog(items.getItems().get(0));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte buf[] = new byte[1024];
                int len = 42;
                while (len > 0) {
                    len = is.read(buf);
                    if (len > 0) {
                        out.write(buf, 0, len);
                    }
                }
                lastlog = out.toString("UTF-8");
            } else {
                logger.debug("Could not find pod " + selector);
            }
        } catch (IOException e1) {
            logger.debug("Unable to get logs from Kubernetes");
        } catch (ApiException e1) {
            logger.debug("Unable to get a job from Kubernetes");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("Could not close log stream");
                }
            }
        }
    }

    @Override
    public String getRemoteLog() {
        getJobLog();
        return "-------- STDOUT/STDERR --------\n" + lastlog;
    }
}
