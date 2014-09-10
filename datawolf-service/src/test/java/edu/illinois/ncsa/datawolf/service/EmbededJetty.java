package edu.illinois.ncsa.datawolf.service;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;

public class EmbededJetty {
    public static int      PORT = 9977;
    private static Server  server;
    public static Injector injector;

    public static void jettyServer(String resourceBase, String configXml) throws Exception {
        server = new Server(PORT);

        ServletContextHandler root = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        root.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

        GuiceContextListener listener = new GuiceContextListener();
        root.addEventListener(listener);
        // create the context, point to location of resources
        // Context root = new Context(server, "/", Context.SESSIONS);
        // root.setResourceBase(resourceBase);

        // setup resteasy
        // Map<String, String> initParams = new HashMap<String, String>();
        // initParams.put("resteasy.scan.providers", "true");
        // initParams.put("javax.ws.rs.Application",
// DataWolfApplication.class.getName());
        // root.addEventListener(new ResteasyBootstrap());
        root.addServlet(HttpServletDispatcher.class, "/*");

        // root.addEventListener(new ContextLoaderListener(xmlContext));
        // root.addFilter(OpenEntityManagerInViewFilter.class, "/*",
// Handler.DEFAULT);

        // start jetty
        server.start();
        injector = listener.getInjector();
        System.out.println("http://localhost:" + PORT);
    }

    public static void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            System.err.println("Error shutting down jetty server: " + e);
        }
        System.out.println("Jetty server stopped");
    }

    public static void main(String[] args) throws Exception {
        EmbededJetty.PORT = 9093;
        jettyServer("src/test/resources", "applicationContext.xml");
    }
}
