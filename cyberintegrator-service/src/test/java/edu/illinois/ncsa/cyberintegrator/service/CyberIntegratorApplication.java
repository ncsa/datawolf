package edu.illinois.ncsa.cyberintegrator.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import edu.illinois.ncsa.file.service.FilesResource;

public class CyberIntegratorApplication extends Application {
    Logger log = LoggerFactory.getLogger(DatasetsResource.class);

    public CyberIntegratorApplication() {
        super();
//        SpringData.loadXMLContext("applicationContext.xml");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();

        rrcs.add(AuthenticationInterceptor.class);
        rrcs.add(WorkflowsResource.class);
        rrcs.add(ExecutionsResource.class);
        rrcs.add(DatasetsResource.class);
        rrcs.add(PersonsResource.class);
        rrcs.add(FilesResource.class);
        rrcs.add(LogFilesResource.class);
        rrcs.add(ExecutorsResource.class);
        rrcs.add(WorkflowToolsResource.class);
        return rrcs;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> rrcs = new HashSet<Object>();
        rrcs.add(new JacksonJaxbJsonProvider());
        return rrcs;
    }
}