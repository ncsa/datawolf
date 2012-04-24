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

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Class to wrap a transaction. The transaction will keep the entitymanager
 * alive during the lifetime of the transaction allowing for lazy loading. The
 * transaction is started using the start method and finished either by
 * committing it, or rolling it back.
 * 
 * @author Rob Kooper <kooper@illinois.edu>
 * 
 */
public class Transaction {
    private PlatformTransactionManager transactionManager;

    private TransactionStatus          status;

    /**
     * Create a new instance of the Transaction using the given transaction
     * manger. This class should be created using SpringData.getTransaction.
     * 
     * @param transactionManager
     *            the transaction manager keeping track of transactions.
     */
    public Transaction(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.status = null;
    }

    /**
     * Start a new transaction. This will call start with readonly set to false.
     * 
     * @throws Exception
     *             throws an exception if there was a problem creating the
     *             transaction or another transaction is already in use.
     */
    public void start() throws Exception {
        start(false);
    }

    /**
     * Start a new transaction. This will use the readonly flag as a hint for
     * the transaction manager..
     * 
     * @param readonly
     *            a hint for the transaction manager that the transaction should
     *            be used for readonly operations only.
     * @throws Exception
     *             throws an exception if there was a problem creating the
     *             transaction or another transaction is already in use.
     */
    public void start(boolean readonly) throws Exception {
        if (status != null) {
            throw (new Exception("Already have transaction open."));
        }
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setReadOnly(readonly);
        status = transactionManager.getTransaction(def);
    }

    /**
     * Commit the transaction. This will write all the changes to the backing
     * store.
     * 
     * @throws Exception
     *             throws an exception if there was an error committing the
     *             transaction.
     */
    public void commit() throws Exception {
        if (status == null) {
            throw (new Exception("No transaction is open."));
        }
        transactionManager.commit(status);
    }

    /**
     * Rolls back the transaction. This will remove all the changes to the
     * backing store.
     * 
     * @throws Exception
     *             throws an exception if there was an error committing the
     *             transaction.
     */
    public void rollback() throws Exception {
        if (status == null) {
            throw (new Exception("No transaction is open."));
        }
        transactionManager.rollback(status);
    }
}