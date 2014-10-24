package edu.illinois.ncsa.datawolf.service;

import com.google.inject.persist.PersistFilter;
import com.google.inject.servlet.ServletModule;

public class DevServletModule extends ServletModule {
    protected void configureServlets() {
        super.configureServlets();
        install(new DevPersistenceModule());
        install(new ResourcesModule());

        // Enable HTTP scope unit of work
        filter("/*").through(PersistFilter.class);
    }
}
