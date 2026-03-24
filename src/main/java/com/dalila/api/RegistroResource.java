package com.dalila.api;

import com.dalila.dao.RegistroDao;
import com.dalila.dto.RegistroDTO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/registros")
public class RegistroResource {

    private final RegistroDao registroDao = new RegistroDao();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegistroDTO> getAll() {
        return registroDao.findAll();
    }
}