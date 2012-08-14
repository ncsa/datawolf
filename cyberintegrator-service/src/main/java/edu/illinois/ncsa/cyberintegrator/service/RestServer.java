/*******************************************************************************
 * Copyright (c) 2012 University of Illinois/NCSA.  All rights reserved.
 * 
 *   National Center for Supercomputing Applications (NCSA)
 *   http://www.ncsa.illinois.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of University of Illinois, NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 ******************************************************************************/
package edu.illinois.ncsa.cyberintegrator.service;

import java.util.HashMap;
import java.util.Map;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

public class RestServer {
    public static final int PORT = 8088;
    private static Server   server;

    public static void main(String[] args) throws Exception {
//        jettyServer();
        jettyServerWithWebXml();

        // create some dummy workflows
//        Workflow wf = new Workflow();
//        Execution e = new Execution();
//        e.setWorkflow(wf);
//
//        SpringData.getBean(ExecutionDAO.class).save(e);
    }

    public static void tjwsServer() throws Exception {
        TJWSEmbeddedJaxrsServer tjws = new TJWSEmbeddedJaxrsServer();
        tjws.setPort(PORT);
        tjws.setRootResourcePath("/");
        tjws.getDeployment().setApplication(new CyberIntegratorApplication());

        tjws.start();
//      tjws.getDeployment().getRegistry().addPerRequestResource(WorkflowsResource.class);
//      tjws.getDeployment().getRegistry().addPerRequestResource(ExecutionsResource.class);

//      tjws.getDeployment().getRegistry().addSingletonResource(jsonProvider);
//      tjws.getDeployment().setProviders(providers);
        System.out.println("http://localhost:" + PORT);
    }

    public static void jettyServer(String resourceBase, String configXml) throws Exception {
        server = new Server(PORT);

        // create the context, point to location of resources
        Context root = new Context(server, "/", Context.SESSIONS);
        root.setResourceBase(resourceBase);

        // setup resteasy
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("javax.ws.rs.Application", CyberIntegratorApplication.class.getName());
        root.setInitParams(initParams);
        root.addEventListener(new ResteasyBootstrap());
        root.addServlet(HttpServletDispatcher.class, "/*");

        // create spring context
        XmlWebApplicationContext xmlContext = new XmlWebApplicationContext();
        xmlContext.setConfigLocation(configXml);
        xmlContext.setServletContext(root.getServletContext());
        xmlContext.refresh();

        // spring framework
        root.addEventListener(new ContextLoaderListener(xmlContext));
        root.addFilter(OpenEntityManagerInViewFilter.class, "/*", Handler.DEFAULT);

        // start jetty
        server.start();
        System.out.println("http://localhost:" + PORT);
    }

    public static void jettyServerWithWebXml() throws Exception {
        server = new Server(PORT);

        WebAppContext context = new WebAppContext();
        context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
        context.setResourceBase("src/main/webapp");
        context.setContextPath("/");
        context.setParentLoaderPriority(true);

        server.setHandler(context);

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
}
