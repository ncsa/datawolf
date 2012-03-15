package edu.illinois.ncsa.cyberintegrator.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowTool;

public interface WorkflowToolDAO extends PagingAndSortingRepository<WorkflowTool, String> {
}
