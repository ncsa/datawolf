package edu.illinois.ncsa.cyberintegrator.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.ExecutionStep;

public interface ExecutionStepDAO extends PagingAndSortingRepository<ExecutionStep, String> {
}
