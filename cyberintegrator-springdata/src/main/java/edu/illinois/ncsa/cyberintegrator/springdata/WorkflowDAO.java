package edu.illinois.ncsa.cyberintegrator.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.Workflow;

public interface WorkflowDAO extends PagingAndSortingRepository<Workflow, String> {
}
