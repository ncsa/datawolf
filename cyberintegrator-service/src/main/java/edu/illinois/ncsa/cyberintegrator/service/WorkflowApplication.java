package edu.illinois.ncsa.cyberintegrator.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import edu.illinois.ncsa.springdata.SpringData;

public class WorkflowApplication extends Application {

    public WorkflowApplication() {
        super();
        SpringData.loadXMLContext("applicationContext.xml");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();
        rrcs.add(WorkflowsResource.class);
        return rrcs;
    }

}