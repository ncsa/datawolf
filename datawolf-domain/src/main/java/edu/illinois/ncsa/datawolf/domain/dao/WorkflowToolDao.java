package edu.illinois.ncsa.datawolf.domain.dao;

import java.util.List;

import edu.illinois.ncsa.datawolf.domain.WorkflowTool;
import edu.illinois.ncsa.domain.dao.IDao;

public interface WorkflowToolDao extends IDao<WorkflowTool, String> {
    List<WorkflowTool> findByDeleted(boolean deleted);

    List<WorkflowTool> findByCreatorEmail(String email);

    List<WorkflowTool> findByCreatorEmailAndDeleted(String email, boolean deleted);
}
