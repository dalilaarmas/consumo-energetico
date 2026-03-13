package com.dalila.api;

import com.dalila.dto.MunicipioDto;
import com.dalila.service.MunicipioService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.PathParam;

import java.util.List;

@Path("/municipios")
@Produces(MediaType.APPLICATION_JSON)
public class MunicipioResource {

    private final MunicipioService service = new MunicipioService();

    @GET
    public Response getAll() {
        List<MunicipioDto> municipios = service.findAll();
        return Response.ok(municipios).build();
    }
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        MunicipioDto municipio = service.findById(id);

        if (municipio == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Municipio no encontrado\"}")
                    .build();
        }

        return Response.ok(municipio).build();
    }

    @GET
    @Path("/{id}/cups")
    public Response getCupsByMunicipio(@PathParam("id") Long id) {
        return Response.ok(service.findCupsByMunicipio(id)).build();
    }
}