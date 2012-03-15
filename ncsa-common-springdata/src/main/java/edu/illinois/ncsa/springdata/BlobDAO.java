package edu.illinois.ncsa.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.domain.Blob;

public interface BlobDAO extends PagingAndSortingRepository<Blob, String> {
}
