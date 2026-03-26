package com.dalila.api;

import com.dalila.service.PdfService;
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
            @QueryParam("fechaMin") String fechaMin,
            @QueryParam("fechaMax") String fechaMax,
            @QueryParam("consumoMin") Double consumoMin,
            @QueryParam("consumoMax") Double consumoMax,
            @QueryParam("aniosTarjetas") String aniosTarjetas,
            @QueryParam("rangoGrafico") String rangoGrafico,
            @QueryParam("rangoTabla") String rangoTabla
    ) {
        List<RegistroDTO> registros = registroDao.findFiltered(
                municipio, cups, direccion, fechaMin, fechaMax, consumoMin, consumoMax
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
}