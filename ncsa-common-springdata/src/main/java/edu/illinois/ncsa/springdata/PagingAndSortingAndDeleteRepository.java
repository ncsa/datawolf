package edu.illinois.ncsa.springdata;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PagingAndSortingAndDeleteRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {
    Iterable<T> findByDeleted(boolean deleted);

    Iterable<T> findByDeleted(boolean deleted, Sort sort);

    Page<T> findByDeleted(boolean deleted, Pageable pageable);
}
