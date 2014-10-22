package edu.illinois.ncsa.datawolf.jpa.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.datawolf.domain.Submission;
import edu.illinois.ncsa.datawolf.domain.dao.SubmissionDao;

public class SubmissionDAOTest {
    private static Injector injector;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new TestModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();
    }

    @Test
    public void testCreateAndStore() throws Exception {
        SubmissionDao submissionDao = injector.getInstance(SubmissionDao.class);

        Submission submission = new Submission();
        submissionDao.save(submission);

        submission.setCreatorId("test");
        submissionDao.save(submission);

        Submission submission1 = submissionDao.findOne(submission.getId());

        assert (submission.getId().equals(submission1.getId()));
    }
}
