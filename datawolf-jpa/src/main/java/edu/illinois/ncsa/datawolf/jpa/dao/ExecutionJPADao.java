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
import java.util.Calendar;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.TemporalType;
import javax.persistence.Temporal;

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
    public List<Execution> findByWorkflowIdAndFilters(String workflowId, String email, boolean deleted,
                                                      int page, int size, String since, String until){
        List<Execution> results;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.workflowId = :workflowId AND e.deleted = :deleted";

        if (!email.trim().equals("")){
            queryString = queryString + " AND e.creator.email = :email";
        }

        if(!since.equals("")){
            queryString = queryString + " AND e.date > :since";
        }

        if(!until.equals("")){
            queryString = queryString + " AND e.date < :until";
        }
        queryString = queryString + " ORDER BY date DESC";

        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("workflowId", workflowId);
        typedQuery.setParameter("deleted", deleted);

        if (!email.trim().equals("")){
            typedQuery.setParameter("email", email);
        }

        addDateToQuery(typedQuery, since, "since");
        addDateToQuery(typedQuery, until, "until");

        // Don't apply pagination if page size < 0
        if (page >= 0) {
            typedQuery.setFirstResult(page * size);
            typedQuery.setMaxResults(size);
        }

        results = typedQuery.getResultList();
        return results;
    }

    @Override
    @Transactional
    public List<Execution> findByWorkflowId(String workflowId, int page, int size) {

        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.workflowId = :workflowId";
        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("workflowId", workflowId);
        results = typedQuery.getResultList();
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

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
    public List<Execution> findByWorkflowIdAndDeleted(String workflowId, boolean deleted, int page, int size) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.workflowId = :workflowId and e.deleted = :deleted";
        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("workflowId", workflowId);
        typedQuery.setParameter("deleted", deleted);
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);
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
    public List<Execution> findByDeleted(boolean deleted, int page, int size) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.deleted = :deleted";

        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("deleted", deleted);
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);
        results = typedQuery.getResultList();

        return results;

    }

    @Override
    @Transactional
    public List<Execution> findByCreatorEmail(String email, String since, String until) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.creator.email = :email";
        if(!since.equals("")){
            queryString = queryString + " AND e.date > :since";
        }

        if(!until.equals("")){
            queryString = queryString + " AND e.date < :until";
        }
        
        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("email", email);
        addDateToQuery(typedQuery, since, "since");
        addDateToQuery(typedQuery, until, "until");

        results = typedQuery.getResultList();

        return results;
    }

    @Override
    @Transactional
    public List<Execution> findByCreatorEmail(String email, int page, int size, String since, String until) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.creator.email = :email";
        if(!since.equals("")){
            queryString = queryString + " AND e.date > :since";
        }

        if(!until.equals("")){
            queryString = queryString + " AND e.date < :until";
        }
        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("email", email);
        addDateToQuery(typedQuery, since, "since");
        addDateToQuery(typedQuery, until, "until");
        
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);
        results = typedQuery.getResultList();

        return results;
    }

    @Override
    @Transactional
    public List<Execution> findByCreatorEmailAndDeleted(String email, boolean deleted, String since, String until) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.creator.email = :email and e.deleted = :deleted";
        if(!since.equals("")){
            queryString = queryString + " AND e.date > :since";
        }

        if(!until.equals("")){
            queryString = queryString + " AND e.date < :until";
        }
        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("email", email);
        typedQuery.setParameter("deleted", deleted);
        typedQuery.setParameter("email", email);
        addDateToQuery(typedQuery, since, "since");
        addDateToQuery(typedQuery, until, "until");
        results = typedQuery.getResultList();

        return results;
    }

    @Override
    @Transactional
    public List<Execution> findByCreatorEmailAndDeleted(String email, boolean deleted, int page, int size, String since, String until) {
        List<Execution> results = null;
        String queryString = "SELECT e FROM Execution e " + "WHERE e.creator.email = :email AND e.deleted = :deleted";
        if(!since.equals("")){
            queryString = queryString + " AND e.date > :since";
        }

        if(!until.equals("")){
            queryString = queryString + " AND e.date < :until";
        }
        TypedQuery<Execution> typedQuery = getEntityManager().createQuery(queryString, Execution.class);
        typedQuery.setParameter("email", email);
        typedQuery.setParameter("deleted", deleted);
        typedQuery.setParameter("email", email);
        addDateToQuery(typedQuery, since, "since");
        addDateToQuery(typedQuery, until, "until");
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);
        results = typedQuery.getResultList();

        return results;
    }

    private void addDateToQuery(TypedQuery<Execution> typedQuery, String date, String matchString) {
        if(!date.equals("")){
            String [] dateArray= date.split("-");
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, Integer.parseInt(dateArray[0]));
            cal.set(Calendar.MONTH, Integer.parseInt(dateArray[1]) - 1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArray[2]));

            typedQuery.setParameter(matchString, cal, TemporalType.DATE);
            return; 
        }
    }
}