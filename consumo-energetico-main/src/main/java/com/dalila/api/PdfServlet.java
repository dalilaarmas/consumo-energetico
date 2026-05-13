package com.dalila.api;

import com.dalila.dto.RegistroDTO;
import com.dalila.service.PdfService;
import com.dalila.service.RegistroService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/generar-pdf")
public class PdfServlet extends HttpServlet {

    private PdfService pdfService = new PdfService();
    private RegistroService registroService = new RegistroService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. Opciones de visualización
        boolean resGlobal  = Boolean.parseBoolean(request.getParameter("imprimirResumenGlobal"));
        boolean resAnual   = Boolean.parseBoolean(request.getParameter("imprimirTarjetasAnuales"));
        boolean conGrafico = Boolean.parseBoolean(request.getParameter("imprimirGrafico"));
        boolean conTabla   = Boolean.parseBoolean(request.getParameter("imprimirTabla"));
        boolean conDetalles = Boolean.parseBoolean(request.getParameter("incluirDetallesTarjetas"));

        // 2. Parámetros de rango y años (antes se ignoraban, ahora se leen)
        String aniosTarjetas = request.getParameter("aniosTarjetas") != null ? request.getParameter("aniosTarjetas") : "";
        String rangoGrafico  = request.getParameter("rangoGrafico")  != null ? request.getParameter("rangoGrafico")  : "";
        String rangoTabla    = request.getParameter("rangoTabla")    != null ? request.getParameter("rangoTabla")    : "";

        // 3. Filtros
        String cups      = request.getParameter("cups");
        String direccion = request.getParameter("direccion");
        String municipio = request.getParameter("municipio");
        String fechaMin  = request.getParameter("fechaMin");
        String fechaMax  = request.getParameter("fechaMax");

        Double consumoMin = request.getParameter("consumoMin") != null && !request.getParameter("consumoMin").isEmpty()
                ? Double.parseDouble(request.getParameter("consumoMin")) : null;
        Double consumoMax = request.getParameter("consumoMax") != null && !request.getParameter("consumoMax").isEmpty()
                ? Double.parseDouble(request.getParameter("consumoMax")) : null;

        // 4. Datos filtrados
        List<RegistroDTO> registrosFiltrados = registroService.findFiltered(
                municipio, cups, direccion, fechaMin, fechaMax, consumoMin, consumoMax
        );

        // 5. Generar PDF pasando los parámetros que antes se ignoraban
        byte[] pdf = pdfService.generarPdfRegistros(
                registrosFiltrados, resGlobal, resAnual, conDetalles, conGrafico, conTabla,
                aniosTarjetas, rangoGrafico, rangoTabla
        );

        response.setContentType("application/pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }
}