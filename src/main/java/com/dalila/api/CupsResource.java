package com.dalila.api;

import com.dalila.dto.CupsDto;
import com.dalila.service.CupsService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/cups")
@Produces(MediaType.APPLICATION_JSON)
public class CupsResource {

    private final CupsService service = new CupsService();

    @GET
    @Path("/{codigo}")
    public Response getByCodigo(@PathParam("codigo") String codigo) {
        CupsDto cups = service.findByCodigo(codigo);

        if (cups == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"CUPS no encontrado\"}")
                    .build();
        }

        return Response.ok(cups).build();
    }
}