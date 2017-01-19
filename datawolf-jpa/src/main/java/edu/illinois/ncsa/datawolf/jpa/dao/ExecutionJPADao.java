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
package edu.illinois.ncsa.datawolf.jpa.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.dao.ExecutionDao;
import edu.illinois.ncsa.jpa.dao.AbstractJPADao;

public class ExecutionJPADao extends AbstractJPADao<Execution, String> implements ExecutionDao {

    @Inject
    public ExecutionJPADao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    @Transactional
    public List<Execution> findByWorkflowId(String workflowId) {

        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.workflowId = :workflowId";
        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("workflowId", workflowId);
        results = typedQuery.getResultList();

        return results;
    }

    @Override
    @Transactional
    public List<Execution> findByWorkflowIdAndDeleted(String workflowId, boolean deleted) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.workflowId = :workflowId and e.deleted = :deleted";
        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("workflowId", workflowId);
        typedQuery.setParameter("deleted", deleted);
        results = typedQuery.getResultList();

        return results;
    }

    @Override
    @Transactional
    public List<Execution> findByDeleted(boolean deleted) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.deleted = :deleted";

        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("deleted", deleted);
        results = typedQuery.getResultList();

        return results;

    }

    @Override
    @Transactional
    public List<Execution> findByCreatorEmail(String email) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.creator.email = :email";

        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("email", email);
        results = typedQuery.getResultList();

        return results;
    }

    @Override
    @Transactional
    public List<Execution> findByCreatorEmailAndDeleted(String email, boolean deleted) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.creator.email = :email and e.deleted = :deleted";

        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("email", email);
        typedQuery.setParameter("deleted", deleted);
        results = typedQuery.getResultList();

        return results;
    }
}