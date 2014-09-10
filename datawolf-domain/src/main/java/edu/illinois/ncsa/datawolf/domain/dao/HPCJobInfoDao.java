package edu.illinois.ncsa.datawolf.domain.dao;

import java.util.List;

import edu.illinois.ncsa.datawolf.domain.HPCJobInfo;
import edu.illinois.ncsa.domain.dao.IDao;

public interface HPCJobInfoDao extends IDao<HPCJobInfo, String> {
    List<HPCJobInfo> findByExecutionId(String executionId);
}
