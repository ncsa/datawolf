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
package edu.illinois.ncsa.springdata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.domain.AbstractBean;
import edu.illinois.ncsa.domain.Person;

public class SpringData implements ApplicationContextAware {
    private static ApplicationContext context = null;

    /* set the context thanks to context aware */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringData.context = applicationContext;
    }

    /**
     * Create an instance of the bean asked for. Most common case is for DAO,
     * but this could be any bean registered with Spring.
     * 
     * @param requiredType
     *            the bean class that needs to be created.
     * @return the instance of the bean class to use.
     */
    public static <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
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

    /**
     * Returns the filestorage to read and write the actual data to and from
     * disk. This class is responsible for reading and writing the actual data
     * of the files to disk. This uses the FiledDescriptor to read and write
     * the data.
     * 
     * @return FileStorage that reads/writes data to disk.
     */
    public static FileStorage getFileStorage() {
        return context.getBean(FileStorage.class);
    }

    /**
     * Returns the eventbus to send and receive events about object creation,
     * deletion and updates. Objects interested in receiving events should
     * register an event listener with this instance.
     * 
     * @return the singleton instance of the eventbus.
     */
    public static EventBus getEventBus() {
        return context.getBean(EventBus.class);
    }

    /**
     * This will clone the given object and return a complete copy. This is
     * especially useful to eagerly load all the elements in a collection when
     * the object is fetched from hibernate. The transaction that was used to
     * fetch the original object will need to be open to fetch any additional
     * fields.
     * 
     * @param object
     *            the object that needs to be cloned
     * @return the cloned object with all fields filled in
     * @throws IOException
     *             throws an IOException if the object could not be cloned.
     */
    @SuppressWarnings("unchecked")
    public static <T> T cloneObject(T object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, object);

        return removeDuplicate((T) mapper.readValue(baos.toByteArray(), object.getClass()));
    }

    /**
     * Convert the object to JSON.
     * 
     * @param object
     *            the object to be converted
     * @return a string with the encoded json.
     * @throws IOException
     *             throws an IOException if the object could not be converted.
     */
    public static String objectToJSON(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, object);
        return new String(baos.toString("UTF-8"));
    }

    /**
     * Converts a JSON string to an actual object. This will convert the string
     * to an object specified by classname. This code will remove any duplicates
     * from the objects created.
     * 
     * @param json
     *            the json encoded object.
     * @param classname
     *            the classname to which the json object should be converted.
     * @return an object of the same type as the classname.
     * @throws IOException
     *             throws an IOException if the string could not be converted to
     *             an actual object.
     */
    public static <T> T JSONToObject(String json, Class<T> classname) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return removeDuplicate((T) mapper.readValue(json.getBytes("UTF-8"), classname));
    }

    /**
     * Converts a JSON inputstream to an actual object. This will convert the
     * inputstream to an object specified by classname. This code will remove
     * any duplicates from the objects created.
     * 
     * @param is
     *            the inputstream with the json code.
     * @param classname
     *            the classname to which the json object should be converted.
     * @return an object of the same type as the classname.
     * @throws IOException
     *             throws an IOException if the string could not be converted to
     *             an actual object.
     */
    public static <T> T JSONToObject(InputStream is, Class<T> classname) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return removeDuplicate((T) mapper.readValue(is, classname));
    }

    /**
     * Check the object for any objects that have same ID but are different
     * instance. This can happen when an object is serialized and deserialized
     * (for example to and from JSON). If another object with the same ID has
     * been seen it will replace the requested object. This function will
     * do a deep check of the object.
     * 
     * @param obj
     *            the object to be checked for duplicates.
     * @return the object with all duplicates removed
     */
    public static <T> T removeDuplicate(T obj) {
        return removeDuplicate(obj, new HashMap<String, Object>());
    }

    /**
     * Check each field in object to see if it is already seen. This will modify
     * collections, lists and arrays as well as the fields in an object.
     * 
     * @param obj
     *            the object to be checked for duplicates.
     * @param seen
     *            objects that have already been checked.
     * @return the object with all duplicates removed
     */
    private static <T> T removeDuplicate(T obj, Map<String, Object> seen) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof AbstractBean) {
            String id = ((AbstractBean) obj).getId();
            if (seen.containsKey(id)) {
                return (T) seen.get(id);
            }
            seen.put(id, obj);

            // check all subfields
            for (Field field : obj.getClass().getDeclaredFields()) {
                if ((field.getModifiers() & Modifier.FINAL) == 0) {
                    try {
                        field.setAccessible(true);
                        field.set(obj, removeDuplicate(field.get(obj), seen));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } else if (obj instanceof Collection<?>) {
            Collection col = (Collection) obj;
            ArrayList arr = new ArrayList(col);
            col.clear();
            for (int i = 0; i < arr.size(); i++) {
                Object x = removeDuplicate(arr.get(i), seen);
                col.add(x);
            }
        } else if (obj instanceof Map<?, ?>) {
            Map map = (Map) obj;
            ArrayList<Entry> arr = new ArrayList(map.entrySet());
            map.clear();
            for (int i = 0; i < arr.size(); i++) {
                Object k = removeDuplicate(arr.get(i).getKey(), seen);
                Object v = removeDuplicate(arr.get(i).getValue(), seen);
                map.put(k, v);
            }
        } else if (obj.getClass().isArray()) {
            Object[] arr = (Object[]) obj;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = removeDuplicate(arr[i], seen);
            }
        }

        return obj;
    }

    public static Person getPerson(String email) {
        PersonDAO dao = getBean(PersonDAO.class);
        Person person = dao.findByEmail(email);
        if (person != null) {
            return person;
        }
        return Person.createPerson("", "", email);
    }

    /**
     * Create the applicationContext anyway you want, in your
     * applicationContext.xml file add the following bean to your context.xml:
     * 
     * <pre>
     * &lt;bean id="applicationContextProvider" class="edu.illinois.ncsa.springdata.SpringData" /&gt;
     * </pre>
     * 
     * 
     * @deprecated
     */
    @Deprecated
    public static void setContext(ApplicationContext context) {
        SpringData.context = context;
    }

    /**
     * Create the applicationContext anyway you want, in your
     * applicationContext.xml file add the following bean to your context.xml:
     * 
     * <pre>
     * &lt;bean id="applicationContextProvider" class="edu.illinois.ncsa.springdata.SpringData" /&gt;
     * </pre>
     * 
     * 
     * @deprecated
     */
    @Deprecated
    public static void loadXMLContext(String configLocation) {
        SpringData.context = new GenericXmlApplicationContext(configLocation);
    }

    /**
     * Create the applicationContext anyway you want, in your
     * applicationContext.xml file add the following bean to your context.xml:
     * 
     * <pre>
     * &lt;bean id="applicationContextProvider" class="edu.illinois.ncsa.springdata.SpringData" /&gt;
     * </pre>
     * 
     * 
     * @deprecated
     */
    @Deprecated
    public static void loadXMLContext(File file) {
        SpringData.context = new GenericXmlApplicationContext(new FileSystemResource(file));
    }

    /**
     * Create the applicationContext anyway you want, in your
     * applicationContext.xml file add the following bean to your context.xml:
     * 
     * <pre>
     * &lt;bean id="applicationContextProvider" class="edu.illinois.ncsa.springdata.SpringData" /&gt;
     * </pre>
     * 
     * 
     * @deprecated
     */
    @Deprecated
    public static void loadXMLContext(Resource r) {
        SpringData.context = new GenericXmlApplicationContext(r);
    }
}
