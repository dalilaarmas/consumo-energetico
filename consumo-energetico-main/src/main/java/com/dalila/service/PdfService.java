package com.dalila.service;

import com.dalila.dto.RegistroDTO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PdfService {
    private static final Logger logger = LogManager.getLogger(PdfService.class);

    public byte[] generarPdfRegistros(
            java.util.List<RegistroDTO> registros,
            boolean imprimirResumenGlobal,
            boolean imprimirTarjetasAnuales,
            boolean incluirDetallesTarjetas,
            boolean imprimirGrafico,
            boolean imprimirTabla,
            String aniosTarjetas,
            String rangoGrafico,
            String rangoTabla
    ) {
        logger.info("Generando PDF de registros...");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);

            // CORRECCIÓN 1: Dejamos solo UNA instancia del writer
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();
            writer.setStrictImageSequence(true);

            Font tituloFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font cabeceraFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
            Font textoFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Font textoFontBold = new Font(Font.HELVETICA, 9, Font.BOLD);

            Paragraph titulo = new Paragraph("Reporte de Consumo Energético", tituloFont);
            titulo.setSpacingAfter(15);
            document.add(titulo);

            // ==========================================
            // PREPARACIÓN DE DATOS (Filtros y cálculos)
            // ==========================================
            java.util.List<RegistroDTO> regValidos = registros.stream()
                    .filter(r -> r.getConsumo() != null && r.getFecha() != null && r.getFecha().length() >= 7)
                    .sorted((a, b) -> Double.compare(b.getConsumo(), a.getConsumo()))
                    .collect(Collectors.toList());

            Map<String, Double> consumoPorAnio = regValidos.stream()
                    .collect(Collectors.groupingBy(r -> r.getFecha().substring(0, 4), Collectors.summingDouble(RegistroDTO::getConsumo)));

            Map<String, Double> consumoPorMes = regValidos.stream()
                    .collect(Collectors.groupingBy(r -> r.getFecha().substring(0, 7), Collectors.summingDouble(RegistroDTO::getConsumo)));


            // ==========================================
            // 1. SECCIÓN: RESUMEN GLOBAL
            // ==========================================
            if (imprimirResumenGlobal && !regValidos.isEmpty()) {
                document.add(new Paragraph("Resumen General de Consumo", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(13, 110, 253))));
                document.add(new Paragraph(" "));

                PdfPTable tablaGlobal = new PdfPTable(1);
                tablaGlobal.setWidthPercentage(100);

                RegistroDTO diaMax = regValidos.get(0);
                java.util.List<RegistroDTO> regMayoresQueCero = regValidos.stream().filter(r -> r.getConsumo() > 0).collect(Collectors.toList());
                RegistroDTO diaMin = regMayoresQueCero.isEmpty() ? regValidos.get(regValidos.size() - 1) : regMayoresQueCero.get(regMayoresQueCero.size() - 1);

                PdfPCell celdaHitos = new PdfPCell();
                celdaHitos.setPadding(10);
                celdaHitos.addElement(new Paragraph("Día de mayor consumo: " + diaMax.getFecha() + " (" + String.format("%.2f", diaMax.getConsumo()) + " kWh)", textoFontBold));
                celdaHitos.addElement(new Paragraph("Día de menor consumo (>0): " + diaMin.getFecha() + " (" + String.format("%.2f", diaMin.getConsumo()) + " kWh)", textoFont));
                tablaGlobal.addCell(celdaHitos);

                PdfPCell celdaTopMax = new PdfPCell();
                celdaTopMax.setPadding(10);
                celdaTopMax.addElement(new Paragraph("Top 3 días de mayor consumo:", cabeceraFont));
                com.lowagie.text.List listaMax = new com.lowagie.text.List(com.lowagie.text.List.ORDERED);
                for (int i = 0; i < Math.min(3, regValidos.size()); i++) {
                    listaMax.add(new ListItem(regValidos.get(i).getFecha() + " - " + String.format("%.2f", regValidos.get(i).getConsumo()) + " kWh", textoFont));
                }
                celdaTopMax.addElement(listaMax);
                tablaGlobal.addCell(celdaTopMax);

                String anioMax = consumoPorAnio.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("-");
                String anioMin = consumoPorAnio.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("-");
                String mesMin = consumoPorMes.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("-");

                PdfPCell celdaPeriodos = new PdfPCell();
                celdaPeriodos.setPadding(10);
                celdaPeriodos.addElement(new Paragraph("Año con más consumo: " + anioMax + " (" + String.format("%.2f", consumoPorAnio.getOrDefault(anioMax, 0.0)) + " kWh)", textoFont));
                celdaPeriodos.addElement(new Paragraph("Año con menos consumo: " + anioMin + " (" + String.format("%.2f", consumoPorAnio.getOrDefault(anioMin, 0.0)) + " kWh)", textoFont));
                celdaPeriodos.addElement(new Paragraph("Mes con menor consumo: " + mesMin + " (" + String.format("%.2f", consumoPorMes.getOrDefault(mesMin, 0.0)) + " kWh)", textoFont));
                tablaGlobal.addCell(celdaPeriodos);

                tablaGlobal.setSpacingAfter(20);
                document.add(tablaGlobal);
            }

            // ==========================================
            // 2. SECCIÓN: RESUMEN POR AÑOS (Tarjetas)
            // ==========================================
            if (imprimirTarjetasAnuales && !consumoPorAnio.isEmpty()) {
                document.add(new Paragraph("Detalle por Años", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(13, 110, 253))));
                document.add(new Paragraph(" "));

                PdfPTable tablaTarjetas = new PdfPTable(2);
                tablaTarjetas.setWidthPercentage(100);
                tablaTarjetas.setSpacingBefore(10);
                tablaTarjetas.setSpacingAfter(20);

                for (String anio : consumoPorAnio.keySet().stream().sorted().collect(Collectors.toList())) {
                    double totalAnio = consumoPorAnio.get(anio);

                    Map<String, Double> mesesDeEsteAnio = consumoPorMes.entrySet().stream()
                            .filter(e -> e.getKey().startsWith(anio))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    int numMeses = mesesDeEsteAnio.size();
                    double promedioMensual = numMeses > 0 ? totalAnio / numMeses : 0;

                    String mesMasAlto = mesesDeEsteAnio.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(e -> e.getKey() + " (" + String.format("%.2f", e.getValue()) + " kWh)")
                            .orElse("N/A");

                    PdfPCell celdaTarjeta = new PdfPCell();
                    celdaTarjeta.setPadding(10);
                    celdaTarjeta.setBorderColor(new Color(200, 200, 200));

                    PdfPCell celdaCabecera = new PdfPCell(new Phrase("Año " + anio, new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));
                    celdaCabecera.setBackgroundColor(new Color(13, 110, 253));
                    celdaCabecera.setPadding(5);

                    PdfPTable miniTabla = new PdfPTable(1);
                    miniTabla.setWidthPercentage(100);
                    miniTabla.addCell(celdaCabecera);

                    PdfPCell contenidoTarjeta = new PdfPCell();
                    contenidoTarjeta.setPadding(8);
                    contenidoTarjeta.addElement(new Paragraph("Total anual: " + String.format("%.2f", totalAnio) + " kWh", textoFontBold));
                    contenidoTarjeta.addElement(new Paragraph("Promedio mensual: " + String.format("%.2f", promedioMensual) + " kWh", textoFont));
                    contenidoTarjeta.addElement(new Paragraph("Mes más alto: " + mesMasAlto, textoFont));

                    if (incluirDetallesTarjetas) {
                        contenidoTarjeta.addElement(new Paragraph(" "));
                        for (Map.Entry<String, Double> mesEntry : mesesDeEsteAnio.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {
                            contenidoTarjeta.addElement(new Paragraph("• " + mesEntry.getKey() + ": " + String.format("%.2f", mesEntry.getValue()) + " kWh", new Font(Font.HELVETICA, 8, Font.NORMAL)));
                        }
                    }

                    miniTabla.addCell(contenidoTarjeta);
                    celdaTarjeta.addElement(miniTabla);
                    tablaTarjetas.addCell(celdaTarjeta);
                }

                if (consumoPorAnio.size() % 2 != 0) {
                    PdfPCell celdaVacia = new PdfPCell();
                    celdaVacia.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                    tablaTarjetas.addCell(celdaVacia);
                }

                document.add(tablaTarjetas);
            }

            // ==========================================
            // 3. SECCIÓN: GRÁFICO
            // ==========================================
            if (imprimirGrafico) {
                document.add(new Paragraph("Gráfica de Evolución", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(13, 110, 253))));
                ChartService chartService = new ChartService();
                BufferedImage chartImage = chartService.generarGraficaConsumo(registros);

                ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
                ImageIO.write(chartImage, "png", chartBaos);

                Image pdfImage = Image.getInstance(chartBaos.toByteArray());
                pdfImage.scaleToFit(760, 320);
                pdfImage.setAlignment(Element.ALIGN_CENTER);
                pdfImage.setSpacingAfter(15);
                pdfImage.setSpacingBefore(10);

                document.add(pdfImage);
            }

            // ==========================================
            // 4. SECCIÓN: TABLA DE DATOS (Arreglo Definitivo)
            // ==========================================
            if (imprimirTabla) {
                document.add(new Paragraph("Desglose de Registros", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(13, 110, 253))));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1.2f, 2f, 2.5f, 3.5f, 2f, 1.5f});

                // CORRECCIÓN 2 y 3: Cabeceras en todas las páginas y evitar pánico de salto de página
                table.setHeaderRows(1);
                table.setSplitLate(false); // Esta es la línea que evita que la página 1 quede en blanco

                addHeaderCell(table, "ID", cabeceraFont);
                addHeaderCell(table, "Municipio", cabeceraFont);
                addHeaderCell(table, "CUPS", cabeceraFont);
                addHeaderCell(table, "Dirección", cabeceraFont);
                addHeaderCell(table, "Fecha", cabeceraFont);
                addHeaderCell(table, "Consumo", cabeceraFont);

                for (RegistroDTO r : registros) {
                    table.addCell(new Phrase(String.valueOf(r.getId()), textoFont));
                    table.addCell(new Phrase(valor(r.getMunicipio()), textoFont));
                    table.addCell(new Phrase(valor(r.getCups()), textoFont));
                    table.addCell(new Phrase(valor(r.getDireccion()), textoFont));
                    table.addCell(new Phrase(valor(r.getFecha()), textoFont));
                    table.addCell(new Phrase(
                            r.getConsumo() != null ? String.format("%.2f", r.getConsumo()) : "",
                            textoFont
                    ));
                }

                document.add(table);
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String texto, Font font) {
        Font fuenteBlanca = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuenteBlanca));
        cell.setBackgroundColor(new Color(25, 135, 84));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private String valor(String s) {
        return s != null ? s : "";
    }
}