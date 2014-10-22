package edu.illinois.ncsa.datawolf.executor.hpc;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.FailedException;
import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.Execution.State;
import edu.illinois.ncsa.datawolf.domain.WorkflowStep;
import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolData;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter;
import edu.illinois.ncsa.datawolf.domain.WorkflowToolParameter.ParameterType;
import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.datawolf.domain.dao.WorkflowStepDao;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption;
import edu.illinois.ncsa.datawolf.executor.commandline.CommandLineOption.Type;
import edu.illinois.ncsa.datawolf.executor.hpc.handlers.JobInfoParser;
import edu.illinois.ncsa.datawolf.executor.hpc.ssh.SSHInfo;
import edu.illinois.ncsa.datawolf.executor.hpc.ssh.SSHSession;
import edu.illinois.ncsa.datawolf.executor.hpc.util.NonNLSConstants;
import edu.illinois.ncsa.datawolf.executor.hpc.util.SshUtils;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Persistence;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.domain.util.BeanUtil;
import edu.illinois.ncsa.gondola.types.submission.JobSubmissionType;

public class HPCExecutorTest {
    private static final Logger logger = LoggerFactory.getLogger(HPCExecutorTest.class);
    private static Injector injector;
    
    private WorkflowToolParameter username;
    private WorkflowToolParameter userHome;
    private WorkflowToolParameter targetSSH;
    private Person creator;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new TestModule());

        Persistence.setInjector(injector);
        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();
    }

    //@Test
    public void testJobStatus() throws Exception {
        //ParserType parser = new ParserType();
        //RegexType expression = new RegexType();
        //expression.setContent("([\\d]+)[\\s]+.+[\\s]+.+[\\s]+.+[\\s]+([A-Za-z]+)[\\s]+.+");
        //parser.setExpression(expression);
        //JobInfoParser jobParser = new JobInfoParser(parser);
        
        
        File workflow = new File("src/test/resources/submission-trestles-template.xml");
        System.out.println(workflow.exists());
        JobSubmissionType job = getJob(workflow);
        
        File status = new File("src/test/resources/qstat.txt");
        String[] lines = getStatus(status);
        JobInfoParser jobParser = new JobInfoParser(job.getStatusHandler().getParser());
        
        for (String line : lines) {
            //m = pattern.matcher(line);
            //System.out.println(m.matches());
            String[] data = jobParser.parseJobState(line);
            System.out.println(data);
        }
        System.out.println(lines.length);
    }
    
    private String[] getStatus(File status) throws Exception {
        StringBuffer out = new StringBuffer();
        
        BufferedReader br = new BufferedReader(new FileReader(status));
        String line;
        while((line = br.readLine()) != null ) {
            out.append(line + "\n");
        }
        return out.toString().split(NonNLSConstants.REMOTE_LINE_SEP);
    }

    private JobSubmissionType getJob(File workflow) throws Exception
    {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(new Class[] { edu.illinois.ncsa.gondola.types.submission.JobSubmissionType.class, edu.illinois.ncsa.gondola.types.submission.JobStatusListType.class,
                    edu.illinois.ncsa.gondola.types.submission.JobStatusType.class, edu.illinois.ncsa.gondola.types.submission.ObjectFactory.class, });
        } catch (JAXBException e) {
            System.out.println("Failed to instantiate gondola classes.");
            // e.printStackTrace(pw);
            throw new FailedException("Failed to instantiate gondola classes.");
        }
        
        JAXBElement<?> element = null;
        try {
            element = (JAXBElement<?>) jc.createUnmarshaller().unmarshal(workflow);
        } catch (JAXBException e) {
            logger.error("Error unmarshalling workflow.", e);
            throw new FailedException("Error unmarshalling workflow.");
        }
        
        return (JobSubmissionType)element.getValue();
        
    }
    // @Test
    public void sshTest() throws Exception {
        //SSHSession session = SshUtils.maybeGetSession(new URI("gsissh://trestles.sdsc.edu"), "cnavarro", "/home/cnavarro");
        
        //SshUtils.mkdirs("/home/cnavarro/test", session);
        System.out.println("test"); //$NON-NLS-1$
        //PersonDao personDao = injector.getInstance(PersonDao.class);
        //Person person = new Person();
        //personDao.save(person);
        
        //Person person2 = personDao.findOne(person.getId());
        
        System.out.println("person");
        
        
        
    }
    //@Test
    public void createToolTest() throws Exception {
        WorkflowStep step = new WorkflowStep();
        step.setTool(createTool());
        step.setTitle("ergo-hpc");

        WorkflowStepDao workflowStepDao = injector.getInstance(WorkflowStepDao.class);
        workflowStepDao.save(step); 
        
        Execution execution = new Execution();
        execution.setParameter(step.getParameters().get(username.getParameterId()), "cnavarro");
        execution.setParameter(step.getParameters().get(userHome.getParameterId()), "/home/cnavarro");
        execution.setParameter(step.getParameters().get(targetSSH.getParameterId()), "gsissh://trestles.sdsc.edu");
        injector.getInstance(ExecutionDao.class).save(execution);
        
        workflowStepDao.findOne(step.getId());
        injector.getInstance(ExecutionDao.class).findOne(execution.getId());
        
    }
    //@Test
    public void submitJob() throws Exception {
        creator = new Person();
        
        WorkflowStep step = new WorkflowStep();
        step.setTool(createErgoTool());
        step.setTitle("ergo-hpc");
        step.setCreator(creator);

        WorkflowStepDao workflowStepDao = injector.getInstance(WorkflowStepDao.class);
        workflowStepDao.save(step);
        
        Execution execution = new Execution();
        execution.setParameter(step.getParameters().get(username.getParameterId()), "cnavarro");
        execution.setParameter(step.getParameters().get(userHome.getParameterId()), "/home/cnavarro");
        execution.setParameter(step.getParameters().get(targetSSH.getParameterId()), "gsissh://trestles.sdsc.edu");
        
        injector.getInstance(ExecutionDao.class).save(execution);
        
        HPCExecutor exec = injector.getInstance(HPCExecutor.class);
        exec.setJobInformation(execution, step);
        
        exec.startJob();

        int loop = 0;
        //boolean val = true;
        //while(val && loop < 1000) {
        while ((exec.getState() != State.FINISHED) && (loop < 100)) {
            if (exec.getState() == State.FAILED) {
                System.out.println(exec.getLog());
                fail("Execution FAILED");
            }
            if (exec.getState() == State.ABORTED) {
                System.out.println(exec.getLog());
                fail("Execution ABORTED");
            }
            Thread.sleep(2000);
            loop++;
        }
        if (exec.getState() != State.FINISHED) {
            System.out.println(exec.getLog());
            fail("Execution NEVER FINISHED");
        }
    }
    
    // @Test
    public void getResultTest() throws Exception {
        SSHSession session = null;
        try {
        String resultPath = "/home/cnavarro/ergo/4647-1413308740.tmp";
        String host = "gsissh://trestles.sdsc.edu";
        String userName = "cnavarro";
        String home = System.getProperty("user.home");
        session = maybeGetSession(new URI(host), userName, home);
        
        if(!resultPath.endsWith("/")) {
            resultPath = resultPath.concat("/");
        }
        
        String destination = resultPath + "ergo-damage.txt";
        
        File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
        File output = File.createTempFile("ergo-damage", ".txt", tmpFolder);
        try {
            SshUtils.copyFrom(destination, output.getAbsolutePath(), session);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        } finally {
            if(session != null) {
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

    private WorkflowTool createTool() throws IOException {
        WorkflowTool tool = new WorkflowTool();
        tool.setTitle("ergo-tool");
        tool.setDescription("HPC Executor tool");
        tool.setExecutor(HPCExecutor.EXECUTOR_NAME);
        tool.setCreator(creator);

        HPCToolImplementation impl = new HPCToolImplementation();
        impl.setExecutable("/bin/uname -a &gt; uname.out");
        
        File templateFile = new File("src/test/resources/submission-trestles-template.xml");
        FileStorage fs = injector.getInstance(FileStorage.class);
        FileDescriptor fd = new FileDescriptor();
        fd.setFilename("submission-trestles-template.xml");
        tool.addBlob(fs.storeFile(fd.getFilename(), templateFile.toURI().toURL().openStream()));
        impl.setTemplate("submission-trestles-template.xml");

        // Gondola Log
        WorkflowToolData gondolaLog = new WorkflowToolData();
        gondolaLog.setTitle("gondola-log");
        gondolaLog.setDescription("gondola log file");
        gondolaLog.setMimeType("text/plain");
        gondolaLog.setDataId("gondola-log");

        tool.addOutput(gondolaLog);
        impl.setLog(gondolaLog.getDataId());

        WorkflowToolData standardOut = new WorkflowToolData();
        standardOut.setTitle("standard-out");
        standardOut.setDescription("standard out from remote tool execution");
        standardOut.setMimeType("text/plain");

        tool.addOutput(standardOut);
        impl.setCaptureStdOut(standardOut.getDataId());
        
        WorkflowToolData standardErr = new WorkflowToolData();
        standardErr.setTitle("standard-err");
        standardErr.setDescription("standard error of remote tool.");
        standardErr.setMimeType("text/plain");
        
        impl.setCaptureStdErr(standardErr.getDataId());
        tool.addOutput(standardErr);

        // Add username
        username = new WorkflowToolParameter();
        username.setTitle("Target Username");
        username.setDescription("Username on reote host");
        username.setType(ParameterType.STRING);
        username.setAllowNull(false);
        tool.addParameter(username);

        // Parameters
        List<CommandLineOption> options = new ArrayList<CommandLineOption>();
        Map<CommandLineOption, WorkflowToolParameter> parameters = new HashMap<CommandLineOption, WorkflowToolParameter>();

        CommandLineOption usernameOption = new CommandLineOption();
        usernameOption.setType(Type.PARAMETER);
        usernameOption.setOptionId(username.getParameterId());
        usernameOption.setCommandline(false);

        parameters.put(usernameOption, username);
        options.add(usernameOption);

        // User home on host
        userHome = new WorkflowToolParameter();
        userHome.setTitle("Target Userhome");
        userHome.setDescription("User home on host");
        userHome.setAllowNull(false);
        userHome.setType(ParameterType.STRING);
        tool.addParameter(userHome);

        CommandLineOption userhomeOption = new CommandLineOption();
        userhomeOption.setType(Type.PARAMETER);
        userhomeOption.setOptionId(userHome.getParameterId());
        userhomeOption.setCommandline(false);

        parameters.put(userhomeOption, userHome);
        options.add(userhomeOption);

        targetSSH = new WorkflowToolParameter();
        targetSSH.setTitle("Target SSH");
        targetSSH.setType(ParameterType.STRING);
        targetSSH.setDescription("Remote Host SSH");
        targetSSH.setAllowNull(false);
        tool.addParameter(targetSSH);

        CommandLineOption targetsshOption = new CommandLineOption();
        targetsshOption.setType(Type.PARAMETER);
        targetsshOption.setOptionId(targetSSH.getParameterId());
        targetsshOption.setCommandline(false);

        parameters.put(targetsshOption, targetSSH);
        options.add(targetsshOption);

        impl.setCommandLineOptions(options);
        tool.setImplementation(BeanUtil.objectToJSON(impl));

        return tool;
    }
    
    private WorkflowTool createErgoTool() throws IOException {
        WorkflowTool tool = new WorkflowTool();
        tool.setTitle("ergo-tool");
        tool.setDescription("HPC Executor tool");
        tool.setExecutor(HPCExecutor.EXECUTOR_NAME);
        tool.setCreator(creator);

        HPCToolImplementation impl = new HPCToolImplementation();
        
        String execution = "java -Xmx500m -cp \"/home/cnavarro/ergo/lib/*\" edu.illinois.ncsa.ergo.eq.hpc.buildings.HPCBuildingDamage \"/home/cnavarro/ergo/fragilities/Default Building Fragilities 1.0.xml\" \"/home/cnavarro/ergo/fragility-mapping/Default Building Fragility Mapping 1.0.xml\" \"/home/cnavarro/ergo/buildings/all_bldgs_ver4_WGS84.shp\" \"/home/cnavarro/ergo/hazard/shelby_sa.asc\" \"/home/cnavarro/ergo/damage-ratios/Building Damage Ratios v1.1.csv\"";
        //String execution = "java -Xmx500m -cp \"/home/cnavarro/ergo/ergo-building-hpc-0.0.1-SNAPSHOT.jar:/home/cnavarro/ergo/dom4j-1.6.1.jar:/home/cnavarro/ergo/log4j-1.2.17.jar:/home/cnavarro/ergo/slf4j-api-1.7.7.jar:/home/cnavarro/ergo/slf4j-log4j12-1.7.7.jar\" edu.illinois.ncsa.ergo.eq.hpc.buildings.HPCBuildingDamage \"/home/cnavarro/ergo/Default Building Fragilities 1.0.xml\"";
        //impl.setExecutable("java -Xmx500m -cp /home/cnavarro/programs/ergo/ErgoDamage.jar edu.illinois.ncsa.ergo.test.ErgoDamage");
        impl.setExecutable(execution);
        
        File templateFile = new File("src/test/resources/submission-trestles-ergo-template.xml");
        FileStorage fs = injector.getInstance(FileStorage.class);
        FileDescriptor fd = new FileDescriptor();
        fd.setFilename("submission-trestles-template.xml");
        tool.addBlob(fs.storeFile(fd.getFilename(), templateFile.toURI().toURL().openStream()));
        impl.setTemplate("submission-trestles-template.xml");

        // Gondola Log
        WorkflowToolData gondolaLog = new WorkflowToolData();
        gondolaLog.setTitle("gondola-log");
        gondolaLog.setDescription("gondola log file");
        gondolaLog.setMimeType("text/plain");
        gondolaLog.setDataId("gondola-log");

        tool.addOutput(gondolaLog);
        impl.setLog(gondolaLog.getDataId());

        WorkflowToolData standardOut = new WorkflowToolData();
        standardOut.setTitle("standard-out");
        standardOut.setDescription("standard out from remote tool execution");
        standardOut.setMimeType("text/plain");

        tool.addOutput(standardOut);
        impl.setCaptureStdOut(standardOut.getDataId());
        
        WorkflowToolData standardErr = new WorkflowToolData();
        standardErr.setTitle("standard-err");
        standardErr.setDescription("standard error of remote tool.");
        standardErr.setMimeType("text/plain");
        
        impl.setCaptureStdErr(standardErr.getDataId());
        tool.addOutput(standardErr);

        // Add username
        username = new WorkflowToolParameter();
        username.setTitle("Target Username");
        username.setDescription("Username on reote host");
        username.setType(ParameterType.STRING);
        username.setAllowNull(false);
        tool.addParameter(username);

        // Parameters
        List<CommandLineOption> options = new ArrayList<CommandLineOption>();
        Map<CommandLineOption, WorkflowToolParameter> parameters = new HashMap<CommandLineOption, WorkflowToolParameter>();

        CommandLineOption usernameOption = new CommandLineOption();
        usernameOption.setType(Type.PARAMETER);
        usernameOption.setOptionId(username.getParameterId());
        usernameOption.setCommandline(false);

        parameters.put(usernameOption, username);
        options.add(usernameOption);

        // User home on host
        userHome = new WorkflowToolParameter();
        userHome.setTitle("Target Userhome");
        userHome.setDescription("User home on host");
        userHome.setAllowNull(false);
        userHome.setType(ParameterType.STRING);
        tool.addParameter(userHome);

        CommandLineOption userhomeOption = new CommandLineOption();
        userhomeOption.setType(Type.PARAMETER);
        userhomeOption.setOptionId(userHome.getParameterId());
        userhomeOption.setCommandline(false);

        parameters.put(userhomeOption, userHome);
        options.add(userhomeOption);

        targetSSH = new WorkflowToolParameter();
        targetSSH.setTitle("Target SSH");
        targetSSH.setType(ParameterType.STRING);
        targetSSH.setDescription("Remote Host SSH");
        targetSSH.setAllowNull(false);
        tool.addParameter(targetSSH);

        CommandLineOption targetsshOption = new CommandLineOption();
        targetsshOption.setType(Type.PARAMETER);
        targetsshOption.setOptionId(targetSSH.getParameterId());
        targetsshOption.setCommandline(false);

        parameters.put(targetsshOption, targetSSH);
        options.add(targetsshOption);

        impl.setCommandLineOptions(options);
        tool.setImplementation(BeanUtil.objectToJSON(impl));

        return tool;
    }
}
