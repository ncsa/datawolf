package edu.illinois.ncsa.file.service;

import java.util.HashMap;
import java.util.Map;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

public class EmbededJetty {
    public static int      PORT = 9977;
    private static Server  server;
    public static Injector injector;

    public static void jettyServer(String resourceBase, String configXml) throws Exception {
        server = new Server(PORT);

        // create the context, point to location of resources
        Context root = new Context(server, "/", Context.SESSIONS);
        root.setResourceBase(resourceBase);

        // setup resteasy
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("javax.ws.rs.Application", FileApplication.class.getName());
        root.setInitParams(initParams);
        root.addEventListener(new ResteasyBootstrap());
        root.addServlet(HttpServletDispatcher.class, "/*");

        injector = Guice.createInjector(new TestModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();
        // create spring context
        // XmlWebApplicationContext xmlContext = new XmlWebApplicationContext();
        // xmlContext.setConfigLocation(configXml);
        // xmlContext.setServletContext(root.getServletContext());
        // xmlContext.refresh();

        // start jetty
        server.start();
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
