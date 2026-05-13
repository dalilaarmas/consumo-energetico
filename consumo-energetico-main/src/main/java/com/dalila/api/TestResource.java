package com.dalila.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    @GET
    public Response test() {
        return Response.ok("{\"mensaje\":\"API funcionando\"}").build();
    }
}