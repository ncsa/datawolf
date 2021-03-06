/*******************************************************************************
 * Copyright (c) 2012 University of Illinois/NCSA.  All rights reserved.
 * 
 *   National Center for Supercomputing Applications (NCSA)
 *   http://www.ncsa.illinois.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of University of Illinois, NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 ******************************************************************************/
package edu.illinois.ncsa.springdata;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import edu.illinois.ncsa.domain.Dataset;

public interface DatasetDAO extends PagingAndSortingAndDeleteRepository<Dataset, String> {
    List<Dataset> findByTitleLike(String titlePattern);

    List<Dataset> findByTitleLike(String titlePattern, Sort sort);

    List<Dataset> findByCreatorEmail(String email);

    List<Dataset> findByCreatorEmail(String email, Sort sort);

    List<Dataset> findByCreatorEmailAndTitleLike(String email, String pattern);

    List<Dataset> findByCreatorEmailAndTitleLike(String email, String pattern, Sort sort);

    Page<Dataset> findByTitleLike(String titlePattern, Pageable page);

    Page<Dataset> findByCreatorEmail(String email, Pageable page);

    Page<Dataset> findByCreatorEmailAndTitleLike(String email, String pattern, Pageable page);

    List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted);

    List<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted, Sort sort);

    List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted);

    List<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted, Sort sort);

    List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String pattern, boolean deleted);

    List<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String pattern, boolean deleted, Sort sort);

    Page<Dataset> findByTitleLikeAndDeleted(String titlePattern, boolean deleted, Pageable page);

    Page<Dataset> findByCreatorEmailAndDeleted(String email, boolean deleted, Pageable page);

    Page<Dataset> findByCreatorEmailAndTitleLikeAndDeleted(String email, String pattern, boolean deleted, Pageable page);
}
