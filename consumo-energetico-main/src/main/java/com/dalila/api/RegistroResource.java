package com.dalila.api;

import com.dalila.dto.*;
import com.dalila.service.PdfService;
import com.dalila.service.RegistroService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * REST API — Registros de consumo
 * Base URL: /api/registros
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ CRUD                                                                │
 * │  GET    /api/registros                    Lista filtrable           │
 * │  GET    /api/registros/{id}               Por ID                   │
 * │  POST   /api/registros                    Crear                    │
 * │  PUT    /api/registros/{id}               Actualizar               │
 * │  DELETE /api/registros/{id}               Eliminar                 │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ Estadísticas                                                        │
 * │  GET    /api/registros/resumen            Resumen global           │
 * │  GET    /api/registros/resumen/anual      Totales por año          │
 * │  GET    /api/registros/anio/{a}/registros Registros de un año      │
 * │  GET    /api/registros/anio/{a}/analisis  Análisis de un año       │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ PDF                                                                 │
 * │  GET    /api/registros/pdf                Descargar PDF con filtros│
 * └─────────────────────────────────────────────────────────────────────┘
 */
@Path("/registros")
@Produces(MediaType.APPLICATION_JSON)
public class RegistroResource {

    private final RegistroService registroService = new RegistroService();
    private final PdfService      pdfService      = new PdfService();

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /**
     * GET /api/registros
     * Parámetros opcionales:
     *   ?municipio=ADEJE  ?cups=ES003  ?direccion=Autopista
     *   ?fechaDesde=2023  ?fechaHasta=2024-06
     *   ?consumoMin=100   ?consumoMax=5000
     */
    @GET
    public List<RegistroDTO> getAll(
            @QueryParam("municipio")  String municipio,
            @QueryParam("cups")       String cups,
            @QueryParam("direccion")  String direccion,
            @QueryParam("fechaDesde") String fechaDesde,
            @QueryParam("fechaHasta") String fechaHasta,
            @QueryParam("consumoMin") Double consumoMin,
            @QueryParam("consumoMax") Double consumoMax
    ) {
        return registroService.findFiltered(municipio, cups, direccion, fechaDesde, fechaHasta, consumoMin, consumoMax);
    }

    /** GET /api/registros/{id} */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        RegistroDTO r = registroService.findById(id);
        return r != null ? Response.ok(r).build() : error(404, "Registro no encontrado");
    }

    /**
     * POST /api/registros
     * Body: { "cups": "ES00316...", "fecha": "2024-03-15", "consumo": 123.45 }
     * El CUPS debe existir en la BD. 201 Created si va bien.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(RegistroDTO dto) {
        try {
            registroService.crear(dto);
            return Response.status(201).entity(dto).build();
        } catch (IllegalArgumentException e) {
            return error(400, e.getMessage());
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    /**
     * PUT /api/registros/{id}
     * Body: { "cups": "ES00316...", "fecha": "2024-03-15", "consumo": 150.00 }
     * Nota: municipio y dirección no son editables (pertenecen a la tabla cups).
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") int id, RegistroDTO dto) {
        try {
            RegistroDTO actualizado = registroService.actualizar(id, dto);
            return Response.ok(actualizado).build();
        } catch (IllegalArgumentException e) {
            return error(404, e.getMessage());
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    /**
     * DELETE /api/registros/{id}
     * 204 No Content si va bien. 404 si no existe.
     */
    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") int id) {
        try {
            registroService.eliminar(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return error(404, e.getMessage());
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    // ── Estadísticas ──────────────────────────────────────────────────────────

    /** GET /api/registros/resumen — día máx/mín, top 3, año con más/menos consumo */
    @GET
    @Path("/resumen")
    public Response getResumenGlobal() {
        return Response.ok(registroService.getResumenGlobal()).build();
    }

    /** GET /api/registros/resumen/anual — total por año, de más reciente a más antiguo */
    @GET
    @Path("/resumen/anual")
    public Response getResumenAnual() {
        return Response.ok(registroService.getResumenAnual()).build();
    }

    /**
     * GET /api/registros/anio/{anio}/registros
     * Todos los registros individuales de un año. Ej: /api/registros/anio/2023/registros
     */
    @GET
    @Path("/anio/{anio}/registros")
    public Response getRegistrosPorAnio(@PathParam("anio") int anio) {
        return Response.ok(registroService.getRegistrosPorAnio(anio)).build();
    }

    /**
     * GET /api/registros/anio/{anio}/analisis
     * Estadísticas completas del año: total, promedio mensual, top días, desglose por mes.
     * Ej: /api/registros/anio/2023/analisis
     */
    @GET
    @Path("/anio/{anio}/analisis")
    public Response getAnalisisPorAnio(@PathParam("anio") int anio) {
        DetalleEstadisticoAnualDTO detalle = registroService.getAnalisisPorAnio(anio);
        return detalle != null ? Response.ok(detalle).build()
                : error(404, "No hay datos para el año " + anio);
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    /**
     * GET /api/registros/pdf
     * Genera y devuelve un PDF con los registros y secciones seleccionadas.
     *
     * Secciones (true/false):
     *   ?imprimirResumenGlobal=true  ?imprimirTarjetasAnuales=true
     *   ?incluirDetallesTarjetas=false  ?imprimirGrafico=true  ?imprimirTabla=true
     *
     * Filtros: mismos que GET /registros
     *
     * Opciones:
     *   ?aniosTarjetas=2023,2024   ?rangoGrafico=1-500   ?rangoTabla=1-200
     */
    @GET
    @Path("/pdf")
    @Produces("application/pdf")
    public Response descargarPdf(
            @QueryParam("imprimirResumenGlobal")   @DefaultValue("false") boolean imprimirResumenGlobal,
            @QueryParam("imprimirTarjetasAnuales") @DefaultValue("false") boolean imprimirTarjetasAnuales,
            @QueryParam("incluirDetallesTarjetas") @DefaultValue("false") boolean incluirDetallesTarjetas,
            @QueryParam("imprimirGrafico")         @DefaultValue("false") boolean imprimirGrafico,
            @QueryParam("imprimirTabla")           @DefaultValue("true")  boolean imprimirTabla,
            @QueryParam("municipio")  String municipio,
            @QueryParam("cups")       String cups,
            @QueryParam("direccion")  String direccion,
            @QueryParam("fechaDesde") String fechaDesde,
            @QueryParam("fechaHasta") String fechaHasta,
            @QueryParam("consumoMin") Double consumoMin,
            @QueryParam("consumoMax") Double consumoMax,
            @QueryParam("aniosTarjetas") String aniosTarjetas,
            @QueryParam("rangoGrafico")  String rangoGrafico,
            @QueryParam("rangoTabla")    String rangoTabla
    ) {
        List<RegistroDTO> registros = registroService.findFiltered(
                municipio, cups, direccion, fechaDesde, fechaHasta, consumoMin, consumoMax
        );
        byte[] pdf = pdfService.generarPdfRegistros(
                registros, imprimirResumenGlobal, imprimirTarjetasAnuales,
                incluirDetallesTarjetas, imprimirGrafico, imprimirTabla,
                aniosTarjetas, rangoGrafico, rangoTabla
        );
        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=registros.pdf")
                .build();
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private Response error(int status, String mensaje) {
        return Response.status(status)
                .entity("{\"error\":\"" + mensaje + "\"}")
                .build();
    }
}