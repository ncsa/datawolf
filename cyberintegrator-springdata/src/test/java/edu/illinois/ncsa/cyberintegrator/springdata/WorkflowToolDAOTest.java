package edu.illinois.ncsa.cyberintegrator.springdata;

import static org.junit.Assert.assertEquals;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;
import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;
import edu.illinois.ncsa.springdata.SpringData;

@Transactional
public class WorkflowToolDAOTest {
    private String        id;

    // the following is necessary for lazy loading
    static SessionFactory sf     = null;

    // open and bind the session for this test thread.
    static Session        s      = null;

    // Session holder
    static SessionHolder  holder = null;

    @BeforeClass
    public static void setUp() throws Exception {
        SpringData.loadXMLContext("testContext.xml");

        sf = SpringData.getBean(SessionFactory.class);
        s = sf.openSession();
        TransactionSynchronizationManager.bindResource(sf, new SessionHolder(s));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        // unbind and close the session.
        holder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
        s = holder.getSession();
        s.flush();
        TransactionSynchronizationManager.unbindResource(sf);
        SessionFactoryUtils.closeSession(s);
    }

    @Before
    public void createTool() {
        WorkflowToolDAO dao = SpringData.getBean(WorkflowToolDAO.class);

        WorkflowTool tool = new WorkflowTool();
        tool.addInput(new WorkflowToolData());
        tool.addInput(new WorkflowToolData());
        tool.addInput(new WorkflowToolData());
        tool.addInput(new WorkflowToolData());
        dao.save(tool);

        id = tool.getId();
    }

    @Test
    public void testCreateAndStore() throws Exception {
        WorkflowToolDAO dao = SpringData.getBean(WorkflowToolDAO.class);
        WorkflowTool tool1 = dao.findOne(id);
        assert (id.equals(tool1.getId()));
    }

    @Test
    public void testCheckInputs() throws Exception {
        WorkflowToolDAO dao = SpringData.getBean(WorkflowToolDAO.class);
        WorkflowTool tool1 = dao.findOne(id);
        for (WorkflowToolData data : tool1.getInputs()) {
            System.out.println(data.getId() + " " + data.getTitle());
        }
        assertEquals(4, tool1.getInputs().size());
    }
}
