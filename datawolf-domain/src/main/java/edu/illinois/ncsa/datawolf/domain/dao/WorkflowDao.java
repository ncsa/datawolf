package edu.illinois.ncsa.datawolf.domain.dao;

import java.util.List;

import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.domain.dao.IDao;

public interface WorkflowDao extends IDao<Workflow, String> {
    List<Workflow> findByDeleted(boolean deleted);

    List<Workflow> findByCreatorEmail(String email);

    List<Workflow> findByCreatorEmailAndDeleted(String email, boolean deleted);
}
