package edu.illinois.ncsa.domain.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.domain.AbstractBean;

public class BeanUtil {

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
}
