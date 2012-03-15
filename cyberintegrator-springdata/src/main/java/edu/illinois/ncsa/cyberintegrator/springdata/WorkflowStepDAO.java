package edu.illinois.ncsa.cyberintegrator.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.WorkflowStep;

public interface WorkflowStepDAO extends PagingAndSortingRepository<WorkflowStep, String> {
}
