package edu.illinois.ncsa.file.service;

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
        // create the context, point to location of resources
        root.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

        GuiceContextListener listener = new GuiceContextListener();
        root.addEventListener(listener);

        // root.setResourceBase(resourceBase);
        // ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
        // root.addServlet(sh, "/*");

        // setup resteasy
        // Map<String, String> initParams = new HashMap<String, String>();
        // initParams.put("javax.ws.rs.Application",
// FileApplication.class.getName());
        // root.addEventListener(new ResteasyBootstrap());
        root.addServlet(HttpServletDispatcher.class, "/*");

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
        EmbededJetty.PORT = 9999;
        jettyServer("src/test/resources", "testContext.xml");
    }
}
