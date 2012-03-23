package edu.illinois.ncsa.cyberintegrator.gui;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface FooDAO extends PagingAndSortingRepository<Foo, Long> {
}
