package com.dalila.api;

import com.dalila.dto.CupsDto;
import com.dalila.dto.MunicipioDto;
import com.dalila.service.MunicipioService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * REST API — Municipios
 * Base URL: /api/municipios
 *
 * ┌────────────────────────────────────────────────────────────────────────┐
 * │  GET    /api/municipios              Lista todos (alfabético)          │
 * │  GET    /api/municipios/{id}         Por ID                            │
 * │  GET    /api/municipios/{id}/cups    CUPS del municipio                │
 * │  POST   /api/municipios              Crear                             │
 * │  PUT    /api/municipios/{id}         Renombrar                         │
 * │  DELETE /api/municipios/{id}         Eliminar (falla si tiene CUPS)    │
 * └────────────────────────────────────────────────────────────────────────┘
 */
@Path("/municipios")
@Produces(MediaType.APPLICATION_JSON)
public class MunicipioResource {

    private final MunicipioService municipioService = new MunicipioService();

    /** GET /api/municipios — lista todos los municipios ordenados alfabéticamente */
    @GET
    public Response getAll() {
        return Response.ok(municipioService.findAll()).build();
    }

    /** GET /api/municipios/{id} */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        MunicipioDto m = municipioService.findById(id);
        return m != null ? Response.ok(m).build() : error(404, "Municipio no encontrado");
    }

    /**
     * GET /api/municipios/{id}/cups
     * Lista todos los CUPS de un municipio con su nombre incluido.
     * Ej: GET /api/municipios/5/cups
     */
    @GET
    @Path("/{id}/cups")
    public Response getCupsByMunicipio(@PathParam("id") Long id) {
        if (municipioService.findById(id) == null) return error(404, "Municipio no encontrado");
        List<CupsDto> cups = municipioService.findCupsByMunicipio(id);
        return Response.ok(cups).build();
    }

    /**
     * POST /api/municipios
     * Body: { "nombre": "GRANADILLA DE ABONA" }
     * Devuelve el municipio creado con su ID generado.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(MunicipioDto dto) {
        try {
            MunicipioDto creado = municipioService.crear(dto != null ? dto.getNombre() : null);
            return Response.status(201).entity(creado).build();
        } catch (IllegalArgumentException e) {
            return error(400, e.getMessage());
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    /**
     * PUT /api/municipios/{id}
     * Body: { "nombre": "NUEVO NOMBRE" }
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, MunicipioDto dto) {
        try {
            MunicipioDto actualizado = municipioService.actualizar(id, dto != null ? dto.getNombre() : null);
            return Response.ok(actualizado).build();
        } catch (IllegalArgumentException e) {
            return error(404, e.getMessage());
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    /**
     * DELETE /api/municipios/{id}
     * Falla si tiene CUPS asociados. Elimínalos primero con DELETE /api/cups/{codigo}.
     */
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        try {
            municipioService.eliminar(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return error(404, e.getMessage());
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    private Response error(int status, String mensaje) {
        return Response.status(status)
                .entity("{\"error\":\"" + mensaje + "\"}")
                .build();
    }
}