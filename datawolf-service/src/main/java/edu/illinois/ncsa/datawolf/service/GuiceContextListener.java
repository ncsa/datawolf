package edu.illinois.ncsa.datawolf.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.domain.Persistence;

public class GuiceContextListener extends GuiceResteasyBootstrapServletContextListener {
    private Injector injector;

    protected List<Module> getModules(final ServletContext context) {
        final List<Module> result = new ArrayList<Module>();
        result.add(new PersistenceModule());
        result.add(new ResourcesModule());
        return result;
    }

    protected void withInjector(Injector injector) {
        // Starts the persistence service for injector
        PersistService service = injector.getInstance(PersistService.class);
        service.start();

        this.injector = injector;

        Persistence.setInjector(this.injector);
    }

    public Injector getInjector() {
        return injector;
    }
}
