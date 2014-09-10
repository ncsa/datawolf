package edu.illinois.ncsa.datawolf.domain.dao;

import java.util.List;

import edu.illinois.ncsa.datawolf.domain.LogFile;
import edu.illinois.ncsa.domain.dao.IDao;

public interface LogFileDao extends IDao<LogFile, String> {
    List<LogFile> findByExecutionId(String executionId);

    LogFile findLogByExecutionIdAndStepId(String executionId, String stepId);
}
