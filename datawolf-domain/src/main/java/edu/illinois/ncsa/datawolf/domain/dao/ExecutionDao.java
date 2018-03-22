package edu.illinois.ncsa.datawolf.domain.dao;

import java.util.List;

import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.domain.dao.IDao;

public interface ExecutionDao extends IDao<Execution, String> {
    List<Execution> findByDeleted(boolean deleted);

    List<Execution> findByDeleted(boolean deleted, int page, int size);

    List<Execution> findByCreatorEmail(String email, String since, String until);

    List<Execution> findByCreatorEmail(String email, int page, int size, String since, String until);

    List<Execution> findByCreatorEmailAndDeleted(String email, boolean deleted, String since, String until);

    List<Execution> findByCreatorEmailAndDeleted(String email, boolean deleted, int page, int size, String since, String until);

    List<Execution> findByWorkflowId(String workflowId);

    List<Execution> findByWorkflowId(String workflowId, int page, int size);

    List<Execution> findByWorkflowIdAndDeleted(String workflowId, boolean deleted);

    List<Execution> findByWorkflowIdAndDeleted(String workflowId, boolean deleted, int page, int size);
}
