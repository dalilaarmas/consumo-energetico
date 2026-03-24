package com.dalila.api;

import com.dalila.dao.RegistroDao;
import com.dalila.dto.RegistroDTO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/registros")
public class RegistroResource {

    private final RegistroDao registroDao = new RegistroDao();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegistroDTO> getAll() {
        return registroDao.findAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearRegistro(RegistroDTO dto) {
        registroDao.insert(dto);
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, RegistroDTO dto) {
        dto.setId(id);
        registroDao.update(id, dto);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        registroDao.delete(id);
        return Response.noContent().build();
    }
}