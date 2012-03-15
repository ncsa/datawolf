package edu.illinois.ncsa.cyberintegrator.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolParameter;

public interface WorkflowToolParameterDAO extends PagingAndSortingRepository<WorkflowToolParameter, String> {
}
