package org.imajie.api.common.api.impl;

import org.imajie.api.common.api.gen.api.PetsApi;
import org.imajie.api.common.error.gen.model.SwagError;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@DeclareRoles({"user", "admin"})
@Path("/pets")
public class PetsApiImpl implements PetsApi {

    @DenyAll
    @Override
    public Response createPets() {
        return Response.serverError().entity(new SwagError().code(1).message("createPets: Not yet implemented!")).build();
    }

    @RolesAllowed("admin")
    @Override
    public Response listPets(final Integer limit) {
        return Response.serverError().entity(new SwagError().code(1).message("listPets: Not yet implemented!")).build();
    }

    @PermitAll
    @Override
    public Response showPetById(final String petId) {
        return Response.serverError().entity(new SwagError().code(1).message("showPetById: Not yet implemented!")).build();
    }
}
