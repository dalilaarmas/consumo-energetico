package com.dalila.api;

import com.dalila.dto.MunicipioDto;
import com.dalila.service.MunicipioService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
}