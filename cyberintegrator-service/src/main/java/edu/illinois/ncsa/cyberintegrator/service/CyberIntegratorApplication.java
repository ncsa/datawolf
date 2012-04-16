package edu.illinois.ncsa.cyberintegrator.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class CyberIntegratorApplication extends Application {

    public CyberIntegratorApplication() {
        super();
//        SpringData.loadXMLContext("applicationContext.xml");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();
        rrcs.add(WorkflowsResource.class);
        rrcs.add(ExecutionsResource.class);
        return rrcs;
    }

}