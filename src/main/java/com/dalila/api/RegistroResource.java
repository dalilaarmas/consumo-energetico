package com.dalila.api;

import com.dalila.dao.RegistroDao;
import com.dalila.dto.ConsumoAnualDto;
import com.dalila.dto.DetalleEstadisticoAnualDTO;
import com.dalila.dto.RegistroDTO;
import com.dalila.dto.ResumenGlobalDto;
import com.dalila.service.EstadisticasService;
import com.dalila.service.PdfService;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/registros")
public class RegistroResource {

    private final RegistroDao registroDao = new RegistroDao();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RegistroDTO> getAll(
            @QueryParam("municipio") String municipio,
            @QueryParam("cups") String cups,
            @QueryParam("direccion") String direccion,
            @QueryParam("fechaDesde") String fechaDesde,
            @QueryParam("fechaHasta") String fechaHasta,
            @QueryParam("consumoMin") Double consumoMin,
            @QueryParam("consumoMax") Double consumoMax
    ) {
        return registroDao.findFiltered(
                municipio,
                cups,
                direccion,
                fechaDesde,
                fechaHasta,
                consumoMin,
                consumoMax
        );
    }

    @GET
    @Path("/{id}")
    @Produces({"application/json"})
    public Response getById(@PathParam("id") int id) {
        // Asumiendo que tu DAO tiene un método findById o get
        RegistroDTO registro = this.registroDao.findById(id);

        if (registro == null) {
            // Si no existe, lo correcto en REST es devolver un 404 Not Found
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(registro).build(); // 200 OK
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

    @GET
    @Path("/pdf")
    @Produces("application/pdf")
    public Response descargarPdf(
            @QueryParam("imprimirResumenGlobal") @DefaultValue("false") boolean imprimirResumenGlobal,
            @QueryParam("imprimirTarjetasAnuales") @DefaultValue("false") boolean imprimirTarjetasAnuales,
            @QueryParam("incluirDetallesTarjetas") @DefaultValue("false") boolean incluirDetallesTarjetas,
            @QueryParam("imprimirGrafico") @DefaultValue("false") boolean imprimirGrafico,
            @QueryParam("imprimirTabla") @DefaultValue("true") boolean imprimirTabla,
            @QueryParam("municipio") String municipio,
            @QueryParam("cups") String cups,
            @QueryParam("direccion") String direccion,
            @QueryParam("fechaDesde") String fechaDesde,
            @QueryParam("fechaHasta") String fechaHasta,
            @QueryParam("consumoMin") Double consumoMin,
            @QueryParam("consumoMax") Double consumoMax,
            @QueryParam("aniosTarjetas") String aniosTarjetas,
            @QueryParam("rangoGrafico") String rangoGrafico,
            @QueryParam("rangoTabla") String rangoTabla
    ) {
        List<RegistroDTO> registros = registroDao.findFiltered(
                municipio, cups, direccion, fechaDesde, fechaHasta, consumoMin, consumoMax
        );

        PdfService pdfService = new PdfService();
        byte[] pdf = pdfService.generarPdfRegistros(
                registros,
                imprimirResumenGlobal,
                imprimirTarjetasAnuales,
                incluirDetallesTarjetas,
                imprimirGrafico,
                imprimirTabla,
                aniosTarjetas,
                rangoGrafico,
                rangoTabla
        );

        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=registros.pdf")
                .build();
    }

    // Endpoint 1: Resumen General
    // Ruta final: GET /registros/resumen
    @GET
    @Path("/resumen")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResumenGlobal() {
        // Obtenemos todos los datos reales del DAO (sin filtros para el resumen total)
        List<RegistroDTO> todos = registroDao.findAll();

        EstadisticasService service = new EstadisticasService();
        ResumenGlobalDto resumen = service.calcularResumenGlobal(todos);

        return Response.ok(resumen).build();
    }

    // Endpoint 2: Resumen por Años
    // Ruta final: GET /registros/resumen/anual
    @GET
    @Path("/resumen/anual")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResumenAnual() {
        // 1. Pedir datos brutos
        List<RegistroDTO> todos = registroDao.findAll();

        // 2. Pedir al SERVICIO que haga las estadísticas
        EstadisticasService service = new EstadisticasService();
        List<ConsumoAnualDto> listaAnual = service.calcularResumenAnual(todos);

        // 3. Enviar respuesta
        return Response.ok(listaAnual).build();
    }

    // ENDPOINT 1: Muestra todos los registros del año (La tabla)
    @GET
    @Path("/anio/{anio}/registros")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSoloRegistros(@PathParam("anio") int anio) {
        List<RegistroDTO> todos = registroDao.findAll();
        List<RegistroDTO> filtrados = todos.stream()
                .filter(r -> r.getFecha().startsWith(String.valueOf(anio)))
                .collect(Collectors.toList());
        return Response.ok(filtrados).build();
    }

    // ENDPOINT 2: Muestra las estadísticas (Total, promedio, mes alto, Top 3)
    @GET
    @Path("/anio/{anio}/analisis-completo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAnalisisCompleto(@PathParam("anio") int anio) {
        List<RegistroDTO> todos = registroDao.findAll();
        EstadisticasService service = new EstadisticasService();
        DetalleEstadisticoAnualDTO master = service.obtenerAnalisisCompleto(anio, todos);
        return Response.ok(master).build();
    }
}