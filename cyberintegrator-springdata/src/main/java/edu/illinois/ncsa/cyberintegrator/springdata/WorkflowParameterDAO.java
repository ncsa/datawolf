package edu.illinois.ncsa.cyberintegrator.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowParameter;

public interface WorkflowParameterDAO extends PagingAndSortingRepository<WorkflowParameter, String> {
}
