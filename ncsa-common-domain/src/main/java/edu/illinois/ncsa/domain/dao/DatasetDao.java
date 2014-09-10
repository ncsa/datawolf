package edu.illinois.ncsa.domain.dao;

import java.util.List;

import edu.illinois.ncsa.domain.Dataset;

public interface DatasetDao extends IDao<Dataset, String> {
    List<Dataset> findByDeleted(boolean deleted);

    List<Dataset> findByTitleLike(String titlePattern);

    List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted);

    List<Dataset> findByCreatorEmail(String email);

    List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted);

    List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern);

    List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted);
}
