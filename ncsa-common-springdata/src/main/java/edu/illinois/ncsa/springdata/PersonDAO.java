package edu.illinois.ncsa.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.domain.Person;

public interface PersonDAO extends PagingAndSortingRepository<Person, String> {
}
