package edu.illinois.ncsa.cyberintegrator.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.springframework.beans.BeansException;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import edu.illinois.ncsa.springdata.SpringData;

public class CyberIntegratorApplication extends Application {

    public CyberIntegratorApplication() {
        super();
//        SpringData.loadXMLContext("applicationContext.xml");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();

        try {
            PreProcessInterceptor interceptor = SpringData.getBean(PreProcessInterceptor.class);
            if (interceptor != null) {
                rrcs.add(interceptor.getClass());
            }
        } catch (BeansException e) {
            // ignore no interceptor found
        }

        rrcs.add(WorkflowsResource.class);
        rrcs.add(ExecutionsResource.class);
        rrcs.add(DatasetsResource.class);
        rrcs.add(PersonsResource.class);
        // rrcs.add(FilesResource.class);
        rrcs.add(LogFilesResource.class);
        return rrcs;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> rrcs = new HashSet<Object>();
        rrcs.add(new JacksonJaxbJsonProvider());
        return rrcs;
    }
}