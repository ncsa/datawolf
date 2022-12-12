package edu.illinois.ncsa.datawolf.executor.kubernetes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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

public class KubernetesExecutor extends RemoteExecutor {
    private static Logger       logger        = LoggerFactory.getLogger(KubernetesExecutor.class);
    public static final String  EXECUTOR_NAME = "kubernetes";

    private static final int    gracePeriod   = 3600;

    // Need to populate this or maybe this should be Job?
    private V1Job               job           = null;
    private BatchV1Api          api           = null;
    private String              jobId         = null;


    // Output files specified in the tool description
    // These should be in the folder that is mounted to the docker image
    // and will be saved to file storage
    private Map<String, String> outputfiles   = new HashMap<String, String>();

    @Override
    public State submitRemoteJob(File cwd) throws AbortException, FailedException {
        // TODO - We need a folder we can put any input files or tool files in and mount
        // it
        // Currently - my Dockerfile assumes the working directory will be /tmp

        // cwd should be a unique folder in the datawolf data folder. This is a shared
        // folder available to datawolf and all kubernetes jobs. The jobID can be used
        // as this magic folder.
        // TODO unique name of execution of this step. can we use execution-id+step-id
        String jobID = UUID.randomUUID().toString();

        // Here is an example of running the docker tool I created assuming we mount a
        // folder called /data into the image as /tmp

        // docker run --rm -v /data/:/tmp/
        // incore/dw-pyincore --analysis
        // pyincore.analyses.buildingdamage.buildingdamage:BuildingDamage --service_url
        // https://incore.ncsa.illinois.edu --token /tmp/cbi/.incore_token --result_name
        // slc-bldg-dmg --hazard_type earthquake --hazard_id 628fc3a2be7de109e898d718
        // --fragility_key --use_liquefaction False --use_hazard_uncertainty --num_cpu 4
        // --seed 1234 --liquefaction_geology_dataset_id --buildings
        // 62fea288f5438e1f8c515ef8 --dfr3_mapping_set 6309005ad76c6d0e1f6be081
        // --retrofit_strategy

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

                    // TODO files need to be mounted to the image that will run
                    // pointer to file on disk
                    String filename;
                    if ((option.getFilename() != null) && !option.getFilename().equals("")) {
                        filename = new File(cwd, option.getFilename()).getAbsolutePath();
                    } else {
                        filename = File.createTempFile("cbi", ".tmp", cwd).getAbsolutePath();
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
                            command.add(filename);
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
            StringBuilder sb = new StringBuilder();
            for (String s : command) {
                sb.append(s);
                sb.append(" ");
            }
            System.out.println("Executing : " + sb.toString());

            // TODO - setup kubernetes job to docker container
            ApiClient client = Config.defaultClient();
            api = new BatchV1Api(client);

            // create the job
            job = new V1Job();

            // add metadata, name is critical
            V1ObjectMeta metadata = new V1ObjectMeta();
            job.setMetadata(metadata);
            metadata.setNamespace(impl.getNamespace());
            //metadata.setName("unique name of execution of this step");
            metadata.setName(jobID);

            // add spec for a job, allow job to stay around for 1 hour after it finishes
            V1JobSpec jobSpec = new V1JobSpec();
            job.setSpec(jobSpec);
            jobSpec.setTtlSecondsAfterFinished(gracePeriod);

            // selector to find pod associated with this job
            V1LabelSelector jobSelector = new V1LabelSelector();
            jobSpec.setSelector(jobSelector);
            jobSelector.putMatchLabelsItem("job-id", jobID);

            V1PodTemplateSpec templateSpec = new V1PodTemplateSpec();
            jobSpec.setTemplate(templateSpec);

            // metadata for pod, used by selector
            V1ObjectMeta templateMeta = new V1ObjectMeta();
            templateSpec.setMetadata(templateMeta);
            templateMeta.putLabelsItem("job-id", jobID);
            templateMeta.putLabelsItem("execution-id", getExecutionId());
            templateMeta.putLabelsItem("step-id", getStepId());

            // actual job
            V1PodSpec podSpec = new V1PodSpec();
            templateSpec.setSpec(podSpec);

            // create the container that will be executed
            V1Container container = new V1Container();
            podSpec.addContainersItem(container);
            // set the name
            container.setName(step.getTitle().replaceAll(" ", "-"));
            // docker image
            container.setImage(impl.getImage());
            // command line arguments for container
            container.args(command);
            // add any environment variables
            //container.addEnvItem();
            // add volumes, this is a subpath in the datawolf volume
            V1VolumeMount volumeMount = new V1VolumeMount();
            container.addVolumeMountsItem(volumeMount);
            volumeMount.setName("data");
            volumeMount.setMountPath("/data");
            volumeMount.setSubPath(jobID);

            // add volume, this is the same pvc as mounted to datawolf
            V1Volume volume = new V1Volume();
            volume.setName("data");
            V1PersistentVolumeClaimVolumeSource pvc = new V1PersistentVolumeClaimVolumeSource();
            volume.setPersistentVolumeClaim(pvc);
            // TODO need the same pvc as mounted into datawolf
            pvc.claimName("datawolf-pvc");

            // create the actual job
            job = api.createNamespacedJob(impl.getNamespace(), job, null, null, null, null);
        } catch (AbortException e) {
            throw e;
        } catch (FailedException e) {
            // Job could not be submitted, set state to waiting to try again
            logger.info("Job not submitted because the job scheduler appears to be down, will try again shortly...");
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
            api.deleteNamespacedJob(this.job.getMetadata().getName(), this.job.getMetadata().getNamespace(), null, null, gracePeriod, null, null, null);
        } catch (ApiException e1) {
            logger.error("Unable to delete job from Kubernetes", e1);
        }

    }

    @Override
    public State checkRemoteJob() throws FailedException {
        // TODO - need to get this from kubernetes and add any state that must be saved
        // to communicate with the job

        try {
            V1Job job = api.readNamespacedJob(this.job.getMetadata().getName(), this.job.getMetadata().getNamespace(), null);
            V1JobStatus status = job.getStatus();

            // This isn't quite right
            // If the job is finished, get the log files and any outputs we need to store in
            // DataWolf
            if (status.equals(V1JobStatus.SERIALIZED_NAME_SUCCEEDED)) {

                try {
                    work.begin();
                    WorkflowStep step = workflowStepDao.findOne(getStepId());
                    Execution execution = executionDao.findOne(getExecutionId());
                    KubernetesToolImplementation impl = BeanUtil.JSONToObject(step.getTool().getImplementation(), KubernetesToolImplementation.class);

                    // If there are any changes to the execution - save it
                    boolean saveExecution = false;

                    // This is a placeholder - we need to find what kind of logs we capture with
                    // kubernetes
                    if (impl.getCaptureStdOut() != null) {
                        // TODO - Fetch and Store any log file that we get from kubernetes execution
                        String stdout = "job finished";
                        Dataset ds = new Dataset();
                        ds.setTitle(step.getTool().getOutput(impl.getCaptureStdOut()).getTitle());
                        ds.setCreator(execution.getCreator());

                        ByteArrayInputStream bais = new ByteArrayInputStream(stdout.toString().getBytes("UTF-8"));
                        FileDescriptor fd = fileStorage.storeFile(step.getTool().getOutput(impl.getCaptureStdOut()).getTitle(), bais, execution.getCreator(), ds);

                        ds = datasetDao.save(ds);

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

                                for (File file : files) {
                                    logger.debug("adding files to a dataset: " + file);
                                    FileInputStream fis = new FileInputStream(file);
                                    fileStorage.storeFile(file.getName(), fis, ds.getCreator(), ds);
                                    fis.close();
                                }

                            } else {
                                FileInputStream fis = new FileInputStream(entry.getValue());
                                fileStorage.storeFile(new File(entry.getValue()).getName(), fis, ds.getCreator(), ds);
                            }
                            ds = datasetDao.save(ds);

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
                return State.FINISHED;
            } else if (status.equals(V1JobStatus.SERIALIZED_NAME_FAILED)) {
                return State.FAILED;
            } else if (status.equals(V1JobStatus.SERIALIZED_NAME_ACTIVE)) {
                return State.RUNNING;
            }

        } catch (ApiException e1) {
            logger.error("Unable to get a job from Kubernetes", e1);
        }
        return State.UNKNOWN;
    }

    @Override
    public String getRemoteLog() {
        // TODO Auto-generated method stub
        return null;
    }

}
