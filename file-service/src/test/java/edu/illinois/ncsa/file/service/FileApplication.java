package edu.illinois.ncsa.file.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

public class FileApplication extends Application {

    public FileApplication() {
        super();
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();
        rrcs.add(FilesResource.class);
        return rrcs;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> rrcs = new HashSet<Object>();
        rrcs.add(new JacksonJaxbJsonProvider());
        return rrcs;
    }
}
