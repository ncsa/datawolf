package edu.illinois.ncsa.cyberintegrator.springdata;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.illinois.ncsa.cyberintegrator.domain.HPCJobInfo;

public interface HPCJobInfoDAO extends PagingAndSortingRepository<HPCJobInfo, String> {
    List<HPCJobInfo> findByExecutionId(String executionId);
}
