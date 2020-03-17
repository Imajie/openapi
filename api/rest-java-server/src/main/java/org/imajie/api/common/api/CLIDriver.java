package org.imajie.api.common.api;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.imajie.api.common.api.impl.GsonProvider;
import org.imajie.api.common.api.impl.PetsApiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CLIDriver {
    private static final Logger log = LoggerFactory.getLogger(CLIDriver.class);

    public static void main(final String[] args) {

        ResourceConfig apiConfig = new ResourceConfig()
                .packages(PetsApiImpl.class.getPackage().getName())
                .register(GsonProvider.class);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new ServletContainer(apiConfig)), "/*");

        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        try {
            jettyServer.start();
            jettyServer.join();
        } catch (Throwable t) {
            log.error("Unhandled exception", t);
        }
    }
}
