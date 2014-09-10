package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import edu.illinois.ncsa.datawolf.domain.LogFile;
import edu.illinois.ncsa.datawolf.domain.dao.LogFileDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class LogFileJPADao extends AbstractJPADao<LogFile, String> implements LogFileDao {
    @Inject
    public LogFileJPADao(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<LogFile> findByExecutionId(String executionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LogFile findLogByExecutionIdAndStepId(String executionId, String stepId) {
        // TODO Auto-generated method stub
        return null;
    }
}
