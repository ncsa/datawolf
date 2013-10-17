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
package edu.illinois.ncsa.cyberintegrator.springdata;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.domain.Person;
import edu.illinois.ncsa.springdata.PagingAndSortingAndDeleteRepository;

public interface WorkflowDAO extends PagingAndSortingAndDeleteRepository<Workflow, String> {

    // return all workflows created by email including deleted ones
    List<Workflow> findByCreatorEmail(String email);

    List<Workflow> findByCreatorEmail(String email, Sort sort);

    Page<Workflow> findByCreatorEmail(String email, Pageable pageable);

    @Deprecated
    List<Workflow> findByCreatorEmailOrderByDateDesc(String email);

    @Deprecated
    Page<Workflow> findByCreatorEmailOrderByDateDesc(String email, Pageable pageable);

    // return all workflows created by email and not-deleted (or deleted)

    List<Workflow> findByCreatorEmailAndDeleted(String email, boolean deleted);

    List<Workflow> findByCreatorEmailAndDeleted(String email, boolean deleted, Sort sort);

    Page<Workflow> findByCreatorEmailAndDeleted(String email, boolean deleted, Pageable pageable);

    @Deprecated
    List<Workflow> findByCreatorEmailAndDeletedOrderByDateDesc(String email, boolean deleted);

    @Deprecated
    Page<Workflow> findByCreatorEmailAndDeletedOrderByDateDesc(String email, boolean deleted, Pageable pageable);

    List<Workflow> findByCreator(Person person);

    List<Workflow> findByCreator(Person person, Sort sort);

    List<Workflow> findByCreatorAandDeleted(Person person, boolean deleted);

    List<Workflow> findByCreatorAandDeleted(Person person, boolean deleted, Sort sort);

    Page<Workflow> findByCreator(Person person, Pageable pageable);

    Page<Workflow> findByCreatorAandDeleted(Person person, boolean deleted, Pageable pageable);

    List<Workflow> findByTitle(String title);

    List<Workflow> findByTitle(String title, Sort sort);

    List<Workflow> findByTitleAandDeleted(String title, boolean deleted);

    List<Workflow> findByTitleAandDeleted(String title, boolean deleted, Sort sort);

    Page<Workflow> findByTitle(String title, Pageable pageable);

    Page<Workflow> findByTitleAandDeleted(String title, boolean deleted, Pageable pageable);
}