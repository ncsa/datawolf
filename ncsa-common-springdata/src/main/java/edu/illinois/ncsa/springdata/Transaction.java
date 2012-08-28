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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static Logger                                   logger       = LoggerFactory.getLogger(Transaction.class);
    private static Map<Thread, Map<Transaction, Exception>> transactions = new HashMap<Thread, Map<Transaction, Exception>>();

    private PlatformTransactionManager                      transactionManager;

    private TransactionStatus                               status;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.debug(getAllTransactions());
            }
        });
    }

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

        Map<Transaction, Exception> localtrans = transactions.get(Thread.currentThread());
        if (localtrans == null) {
            localtrans = new HashMap<Transaction, Exception>();
            transactions.put(Thread.currentThread(), localtrans);
        } else {
            if (localtrans.size() > 0) {
                logger.info(getThreadTransaction());
            }
        }
        localtrans.put(this, new Exception());
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
        transactions.get(Thread.currentThread()).remove(this);
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
        transactions.get(Thread.currentThread()).remove(this);
    }

    public static String getAllTransactions() {
        StringBuilder sb = new StringBuilder();

        StackTraceElement[] elems = new Exception().getStackTrace();
        for (int j = 0; j < elems.length; j++) {
            if (!elems[j].toString().startsWith("edu.illinois.ncsa.springdata.Transaction")) {
                sb.append(String.format("Called from %s\n", elems[j].toString()));
                break;
            }
        }

        for (Thread thr : transactions.keySet()) {
            sb.append(String.format("%s\n", thr.getName()));
            getTransaction(sb, thr);
        }
        return sb.toString();
    }

    public static String getThreadTransaction() {
        StringBuilder sb = new StringBuilder();

        StackTraceElement[] elems = new Exception().getStackTrace();
        for (int j = 0; j < elems.length; j++) {
            if (!elems[j].toString().startsWith("edu.illinois.ncsa.springdata.Transaction")) {
                sb.append(String.format("Called from %s\n", elems[j].toString()));
                break;
            }
        }

        getTransaction(sb, Thread.currentThread());
        return sb.toString();
    }

    private static void getTransaction(StringBuilder sb, Thread thread) {
        Map<Transaction, Exception> transmap = transactions.get(thread);
        if (transmap == null) {
            sb.append("\tThere are no open transactions.\n");
        } else {
            sb.append(String.format("\tThere are %d open transactions : \n", transmap.size()));
            int i = 1;
            for (Entry<Transaction, Exception> entry : transmap.entrySet()) {
                StackTraceElement[] elems = entry.getValue().getStackTrace();
                for (int j = 0; j < elems.length; j++) {
                    if (!elems[j].toString().startsWith("edu.illinois.ncsa.springdata.Transaction")) {
                        sb.append(String.format("\t[%2d] created at %s\n", i++, elems[j].toString()));
                        break;
                    }
                }
            }
        }
    }
}
