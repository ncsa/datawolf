package edu.illinois.ncsa.springdata;

import java.io.File;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import edu.illinois.ncsa.domain.Person;

/*******************************************************************************
 * Copyright (c) 2012 University of Illinois/NCSA. All rights reserved.
 * 
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
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
 * notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in the
 * documentation and/or other materials provided with the distribution.
 * - Neither the names of University of Illinois, NCSA, nor the names
 * of its contributors may be used to endorse or promote products
 * derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 ******************************************************************************/
public class SpringData {
    private static AbstractApplicationContext context = null;

    /**
     * Create an instance of the bean asked for. Most common case is for DAO,
     * but this could be any bean registerd with Spring.
     * 
     * @param beanclass
     *            the bean class that needs to be created.
     * @return the instance of the bean class to use.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> beanclass) {
        String name = beanclass.getSimpleName();
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        Object instance = context.getBean(name);
        return (T) instance;
    }

    /**
     * Create a transaction. All operations inside the transaction will use the
     * same entitymanager. This will prevent lazyloading exceptions.
     * 
     * @return a new transaction. This transaction has not been started yet.
     */
    public static Transaction getTransaction() {
        PlatformTransactionManager ptm = (PlatformTransactionManager) context.getBean("transactionManager");
        return new Transaction(ptm);
    }

    public static Person getPerson(String email) {
        PersonDAO dao = getBean(PersonDAO.class);
        Person person = dao.findByEmail(email);
        if (person != null) {
            return person;
        }
        return Person.createPerson("", "", email);
    }

    public static void setContext(GenericApplicationContext context) {
        SpringData.context = context;
    }

    public static void loadXMLContext(String configLocation) {
        SpringData.context = new GenericXmlApplicationContext(configLocation);
    }

    public static void loadXMLContext(File file) {
        SpringData.context = new GenericXmlApplicationContext(new FileSystemResource(file));
    }

    public static void loadXMLContext(Resource r) {
        SpringData.context = new GenericXmlApplicationContext(r);
    }
}
