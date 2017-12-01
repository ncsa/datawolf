package edu.illinois.ncsa.domain.dao;

import java.util.List;

import edu.illinois.ncsa.domain.Dataset;

public interface DatasetDao extends IDao<Dataset, String> {
    List<Dataset> findByDeleted(boolean deleted);

    List<Dataset> findByDeleted(boolean deleted, int page, int size);

    List<Dataset> findByTitleLike(String titlePattern);

    List<Dataset> findByTitleLike(String titlePattern, int page, int size);

    List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted);

    List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted, int page, int size);

    List<Dataset> findByCreatorEmail(String email);

    List<Dataset> findByCreatorEmail(String email, int page, int size);

    List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted);

    List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted, int page, int size);

    List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern);

    List<Dataset> findByCreatorEmailAndTitleLike(String email, String titlePattern, int page, int size);

    List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted);

    List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String titlePattern, boolean deleted, int page, int size);
}
