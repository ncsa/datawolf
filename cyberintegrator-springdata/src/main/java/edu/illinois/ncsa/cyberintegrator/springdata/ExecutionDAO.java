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

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.springdata.PagingAndSortingAndDeleteRepository;

public interface ExecutionDAO extends PagingAndSortingAndDeleteRepository<Execution, String> {
    List<Execution> findByWorkflowId(String workflowId);

    Page<Execution> findByWorkflowId(String workflowId, Pageable pageable);

    Page<Execution> findByWorkflowIdAndDeleted(String workflowId, boolean deleted, Pageable pageable);

    List<Execution> findByCreatorEmail(String email);

    Page<Execution> findByCreatorEmail(String email, Pageable pageable);

    List<Execution> findByCreatorEmailAndDeleted(String email, boolean deleted);

    Page<Execution> findByCreatorEmailAndDeleted(String email, boolean deleted, Pageable pageable);

    List<Execution> findByCreatorEmailOrderByDateDesc(String email);

    Page<Execution> findByCreatorEmailOrderByDateDesc(String email, Pageable pageable);

    List<Execution> findByCreatorEmailAndDeletedOrderByDateDesc(String email, boolean deleted);

    Page<Execution> findByCreatorEmailAndDeletedOrderByDateDesc(String email, boolean deleted, Pageable pageable);

}
