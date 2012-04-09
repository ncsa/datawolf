package edu.illinois.ncsa.springdata;

import java.io.File;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import edu.illinois.ncsa.domain.Person;

public class SpringData {
    private static AbstractApplicationContext context = null;

    /**
     * Create an instance of the bean asked for.
     * 
     * @param dao
     *            the DAO that needs to be created.
     * @return the instance of the DAO to use.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDAO(Class<T> dao) {
        long l = System.currentTimeMillis();
        String name = dao.getSimpleName();
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        Object instance = context.getBean(name);
        System.out.println(name + " took " + (System.currentTimeMillis() - l));
        return (T) instance;
    }

    public static Person getPerson(String email) {
        PersonDAO dao = getDAO(PersonDAO.class);
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
