package edu.illinois.ncsa.datawolf.service;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.inject.AbstractModule;

/**
 * Binds resource files
 * 
 * @author Chris Navarro <cmnavarr@illinois.edu>
 * 
 */
public class ResourcesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AuthenticationInterceptor.class);
        bind(DatasetsResource.class);
        bind(ExecutionsResource.class);
        bind(ExecutorsResource.class);
        bind(LogFilesResource.class);
        bind(LoginResource.class);
        bind(PersonsResource.class);
        bind(WorkflowsResource.class);
        bind(WorkflowToolsResource.class);
        bind(JacksonJaxbJsonProvider.class);
    }
}