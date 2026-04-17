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
public class PdfServlet extends HttpServlet{

        private PdfService pdfService = new PdfService();
        private RegistroService registroService = new RegistroService(); // Tu servicio de datos

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. Capturamos las opciones de visualización
        boolean resGlobal = Boolean.parseBoolean(request.getParameter("imprimirResumenGlobal"));
        boolean resAnual = Boolean.parseBoolean(request.getParameter("imprimirTarjetasAnuales"));
        boolean conGrafico = Boolean.parseBoolean(request.getParameter("imprimirGrafico"));
        boolean conTabla = Boolean.parseBoolean(request.getParameter("imprimirTabla"));
        boolean conDetalles = Boolean.parseBoolean(request.getParameter("incluirDetallesTarjetas"));

        // 2. CAPTURAMOS LOS FILTROS que manda el Frontend
        String cups = request.getParameter("cups");
        String direccion = request.getParameter("direccion");
        String municipio = request.getParameter("municipio");
        String fechaMin = request.getParameter("fechaMin");
        String fechaMax = request.getParameter("fechaMax");

        // Convertimos los consumos, manejando el caso de que vengan vacíos
        Double consumoMin = request.getParameter("consumoMin") != null && !request.getParameter("consumoMin").isEmpty() ?
                Double.parseDouble(request.getParameter("consumoMin")) : null;
        Double consumoMax = request.getParameter("consumoMax") != null && !request.getParameter("consumoMax").isEmpty() ?
                Double.parseDouble(request.getParameter("consumoMax")) : null;

        // 3. Obtenemos SOLO los datos filtrados

        List<RegistroDTO> registrosFiltrados = registroService.findFiltered(
                municipio, cups, direccion, fechaMin, fechaMax, consumoMin, consumoMax
        );

        // 4. Tu PdfService ya está perfectamente preparado para recibir la lista y hacer los cálculos
        byte[] pdf = pdfService.generarPdfRegistros(
                registrosFiltrados, resGlobal, resAnual, conDetalles, conGrafico, conTabla, "", "", ""
        );

        // Configuramos la respuesta
        response.setContentType("application/pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }
    }
