package edu.illinois.ncsa.datawolf.jpa.dao;

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
}
