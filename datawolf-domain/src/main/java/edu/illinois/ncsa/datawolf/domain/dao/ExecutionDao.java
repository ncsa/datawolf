package edu.illinois.ncsa.datawolf.domain.dao;

import java.util.List;

import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.domain.dao.IDao;

public interface ExecutionDao extends IDao<Execution, String> {
    List<Execution> findByDeleted(boolean deleted);

    List<Execution> findByCreatorEmail(String email);

    List<Execution> findByCreatorEmailAndDeleted(String email, boolean deleted);

    List<Execution> findByWorkflowId(String workflowId);

    List<Execution> findByWorkflowIdAndDeleted(String workflowId, boolean deleted);
}
