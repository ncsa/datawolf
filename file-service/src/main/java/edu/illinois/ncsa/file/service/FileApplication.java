package edu.illinois.ncsa.file.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class FileApplication extends Application {

    public FileApplication() {
        super();
//        SpringData.loadXMLContext("applicationContext.xml");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();
        rrcs.add(FilesResource.class);
        return rrcs;
    }

}