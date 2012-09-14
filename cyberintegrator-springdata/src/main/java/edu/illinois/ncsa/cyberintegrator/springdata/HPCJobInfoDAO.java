package edu.illinois.ncsa.cyberintegrator.springdata;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.HPCJobInfo;

public interface HPCJobInfoDAO extends PagingAndSortingRepository<HPCJobInfo, String> {

}
