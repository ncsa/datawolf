package edu.illinois.ncsa.springdata;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

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
        String name = dao.getSimpleName();
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        return (T) context.getBean(name);
    }

    public static void setContext(GenericApplicationContext context) {
        SpringData.context = context;
    }

    public static void loadXMLContext(String configLocation) {
        SpringData.context = new ClassPathXmlApplicationContext(configLocation);
    }
}
