package edu.illinois.ncsa.datawolf.jpa.dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.google.inject.Provider;

import edu.illinois.ncsa.datawolf.domain.Submission;
import edu.illinois.ncsa.datawolf.domain.dao.SubmissionDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class SubmissionJPADao extends AbstractJPADao<Submission, String> implements SubmissionDao {
    @Inject
    public SubmissionJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }
}
