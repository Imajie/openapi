package org.imajie.api.common.api.impl;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

@Provider
public class AuthFilter implements ContainerRequestFilter {
    public static final Response UNAUTH = Response
            .status(Response.Status.UNAUTHORIZED)
            .build();
    public static final Response FORBIDDEN = Response
            .status(Response.Status.FORBIDDEN)
            .build();
    private static final String AUTH_PROPERTY = "Authorization";
    private static final String AUTH_SCHEME = "Basic";

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {

        final Method method = resourceInfo.getResourceMethod();

        // If the method allows access for anything, then allow
        if (method.isAnnotationPresent(PermitAll.class)) {
            return;
        }

        // If the method does not allow access, then deny
        if (method.isAnnotationPresent(DenyAll.class)) {
            containerRequestContext.abortWith(FORBIDDEN);
            return;
        }

        final SecurityContext securityContext = containerRequestContext.getSecurityContext();
        final Principal userPrincipal = securityContext.getUserPrincipal();

        if (userPrincipal == null) {
            containerRequestContext.abortWith(FORBIDDEN);
            return;
        }

        if (method.isAnnotationPresent(RolesAllowed.class)) {
            final RolesAllowed annotation = method.getAnnotation(RolesAllowed.class);
            for (String role : annotation.value()) {
                if (securityContext.isUserInRole(role)) {
                    return;
                }
            }
        }

        containerRequestContext.abortWith(FORBIDDEN);
    }
}
