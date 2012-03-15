package edu.illinois.ncsa.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.domain.Dataset;

public interface DatasetDAO extends PagingAndSortingRepository<Dataset, String> {
}
