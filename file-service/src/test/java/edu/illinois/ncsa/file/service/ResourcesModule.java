package edu.illinois.ncsa.file.service;

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
        bind(FilesResource.class);
    }
}
