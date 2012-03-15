package edu.illinois.ncsa.cyberintegrator.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;

public interface ExecutionDAO extends PagingAndSortingRepository<Execution, String> {
}
