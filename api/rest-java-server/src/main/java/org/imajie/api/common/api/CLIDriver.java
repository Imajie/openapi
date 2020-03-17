package org.imajie.api.common.api;

import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.imajie.api.common.api.impl.AuthFilter;
import org.imajie.api.common.api.impl.GsonProvider;
import org.imajie.api.common.api.impl.PetsApiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class CLIDriver {
    private static final Logger log = LoggerFactory.getLogger(CLIDriver.class);

    public static void main(final String[] args) {

        ResourceConfig apiConfig = new ResourceConfig()
                .packages(PetsApiImpl.class.getPackage().getName())
                .register(AuthFilter.class)
                .register(GsonProvider.class);

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"user", "admin"});
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        UserStore userStore = new UserStore();
        userStore.addUser("user", new Password("pass"), new String[]{"user"});
        userStore.addUser("admin", new Password("pass"), new String[]{"user","admin"});

        HashLoginService loginService = new HashLoginService();
        loginService.setUserStore(userStore);

        BasicAuthenticator basicAuthenticator = new BasicAuthenticator();

        Set<String> roles = new HashSet<>();
        roles.add("user");
        roles.add("admin");

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.addConstraintMapping(constraintMapping);
        securityHandler.setLoginService(loginService);
        securityHandler.setAuthenticator(basicAuthenticator);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new ServletContainer(apiConfig)), "/*");
        context.setSecurityHandler(securityHandler);

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
