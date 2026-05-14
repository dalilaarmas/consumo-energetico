package com.dalila.api;

import com.dalila.dto.CupsDto;
import com.dalila.entity.Cups;
import com.dalila.service.CupsService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * REST API — CUPS
 * Base URL: /api/cups
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  GET    /api/cups                  Lista (filtrable, con límite)     │
 * │  GET    /api/cups/{codigo}         Por código exacto                 │
 * │  POST   /api/cups                  Crear nuevo CUPS                  │
 * │  PUT    /api/cups/{codigo}         Actualizar                        │
 * │  DELETE /api/cups/{codigo}         Eliminar (falla si tiene consumos)│
 * └──────────────────────────────────────────────────────────────────────┘
 */
@Path("/cups")
@Produces(MediaType.APPLICATION_JSON)
public class CupsResource {

    private final CupsService cupsService = new CupsService();

    /**
     * GET /api/cups
     * Parámetros opcionales:
     *   ?municipio=ADEJE   (búsqueda parcial)
     *   ?limit=100         (defecto 100)
     */
    @GET
    public Response getAll(
            @QueryParam("municipio") String municipio,
            @QueryParam("limit") @DefaultValue("100") int limit
    ) {
        List<CupsDto> lista = cupsService.findAll(municipio, limit);
        return Response.ok(lista).build();
    }

    /**
     * GET /api/cups/{codigo}
     * Devuelve el CUPS con su municipio incluido.
     * Ej: GET /api/cups/ES0031601138661001QF0F
     */
    @GET
    @Path("/{codigo}")
    public Response getByCodigo(@PathParam("codigo") String codigo) {
        CupsDto cups = cupsService.findByCodigo(codigo);
        return cups != null ? Response.ok(cups).build() : error(404, "CUPS no encontrado");
    }

    /**
     * POST /api/cups
     * Crea un nuevo CUPS. Para saber el municipioId: GET /api/municipios
     *
     * Body:
     * {
     *   "codigo":        "ES0031601138661001QF0F",
     *   "direccion":     "Calle Ejemplo, 1",
     *   "codigoPostal":  38001,
     *   "municipioId":   5,
     *   "distribuidorId": 1
     * }
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(Cups cups) {
        try {
            CupsDto creado = cupsService.crear(cups);
            return Response.status(201).entity(creado).build();
        } catch (IllegalArgumentException e) {
            return error(cups != null && cupsService.findByCodigo(cups.getCodigo()) != null ? 409 : 400,
                    e.getMessage());
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    /**
     * PUT /api/cups/{codigo}
     * Actualiza dirección, código postal, municipio y distribuidor.
     *
     * Body:
     * {
     *   "direccion":     "Nueva dirección, 2",
     *   "codigoPostal":  38002,
     *   "municipioId":   3,
     *   "distribuidorId": 1
     * }
     */
    @PUT
    @Path("/{codigo}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("codigo") String codigo, Cups cups) {
        try {
            CupsDto actualizado = cupsService.actualizar(codigo, cups);
            return Response.ok(actualizado).build();
        } catch (IllegalArgumentException e) {
            return error(404, e.getMessage());
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    /**
     * DELETE /api/cups/{codigo}
     * Falla si el CUPS tiene registros de consumo (restricción FK).
     * Elimina antes con DELETE /api/registros/{id}.
     */
    @DELETE
    @Path("/{codigo}")
    public Response eliminar(@PathParam("codigo") String codigo) {
        try {
            cupsService.eliminar(codigo);
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