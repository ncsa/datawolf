package edu.illinois.ncsa.cyberintegrator.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowToolData;

public interface WorkflowToolDataDAO extends PagingAndSortingRepository<WorkflowToolData, String> {
}
