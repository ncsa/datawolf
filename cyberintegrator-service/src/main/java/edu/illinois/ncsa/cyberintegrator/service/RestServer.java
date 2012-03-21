package edu.illinois.ncsa.cyberintegrator.service;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

public class RestServer {
    public static final int PORT = 8088;

    public static void main(String[] args) {
        TJWSEmbeddedJaxrsServer tjws = new TJWSEmbeddedJaxrsServer();
        tjws.setPort(PORT);
        tjws.start();
        tjws.setRootResourcePath("/");
        tjws.getDeployment().getRegistry().addSingletonResource(new WorkflowsResource());
        System.out.println("http://localhost:" + PORT);
    }
}
