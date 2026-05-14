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

    private final PdfService      pdfService      = new PdfService();
    private final RegistroService registroService = new RegistroService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // 1. Opciones de visualización
        boolean resGlobal   = parseBoolean(request, "imprimirResumenGlobal");
        boolean resAnual    = parseBoolean(request, "imprimirTarjetasAnuales");
        boolean conGrafico  = parseBoolean(request, "imprimirGrafico");
        boolean conTabla    = parseBoolean(request, "imprimirTabla");
        boolean conDetalles = parseBoolean(request, "incluirDetallesTarjetas");

        // 2. Parámetros de rango y años
        String aniosTarjetas = parseString(request, "aniosTarjetas");
        String rangoGrafico  = parseString(request, "rangoGrafico");
        String rangoTabla    = parseString(request, "rangoTabla");

        // 3. Filtros de texto
        String cups      = parseString(request, "cups");
        String direccion = parseString(request, "direccion");
        String municipio = parseString(request, "municipio");
        String fechaMin  = parseString(request, "fechaMin");
        String fechaMax  = parseString(request, "fechaMax");

        // 4. Filtros numéricos — protegidos contra "null" literal y vacío
        Double consumoMin = parseDouble(request, "consumoMin");
        Double consumoMax = parseDouble(request, "consumoMax");

        // 5. Datos filtrados
        List<RegistroDTO> registros = registroService.findFiltered(
                municipio, cups, direccion, fechaMin, fechaMax, consumoMin, consumoMax
        );

        // 6. Generar PDF
        byte[] pdf = pdfService.generarPdfRegistros(
                registros, resGlobal, resAnual, conDetalles, conGrafico, conTabla,
                aniosTarjetas, rangoGrafico, rangoTabla
        );

        response.setContentType("application/pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean parseBoolean(HttpServletRequest req, String name) {
        return Boolean.parseBoolean(req.getParameter(name));
    }

    /**
     * Devuelve "" si el parámetro es null, vacío o el literal "null".
     * Evita que strings como "null" lleguen a la capa de datos.
     */
    private String parseString(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        if (val == null || val.isBlank() || val.equalsIgnoreCase("null")) return "";
        return val.trim();
    }

    /**
     * Parsea Double de forma segura.
     * Devuelve null si el parámetro está ausente, vacío o es el literal "null".
     */
    private Double parseDouble(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        if (val == null || val.isBlank() || val.equalsIgnoreCase("null")) return null;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}