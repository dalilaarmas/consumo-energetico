package com.dalila.service;

import com.dalila.dto.RegistroDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Image;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PdfService {
    private static final Logger logger = LogManager.getLogger(PdfService.class);

    public byte[] generarPdfRegistros(
            List<RegistroDTO> registros,
            boolean imprimirResumenGlobal,
            boolean imprimirTarjetasAnuales,
            boolean incluirDetallesTarjetas,
            boolean imprimirGrafico,
            boolean imprimirTabla,
            String aniosTarjetas,
            String rangoGrafico,
            String rangoTabla
    ) {
        logger.info("Mensaje de información");
        logger.debug(String.format("El valor de la variable imprimirResultadoGlobal es: %b", imprimirResumenGlobal));
        logger.error("Mensaje de información");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(document, baos);

            document.open();

            Font tituloFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font cabeceraFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font textoFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            Paragraph titulo = new Paragraph("Listado de registros de consumo energético", tituloFont);
            titulo.setSpacingAfter(15);
            document.add(titulo);

            if (imprimirResumenGlobal) {
                Paragraph resumen = new Paragraph("Total de registros: " + registros.size(), textoFont);
                resumen.setSpacingAfter(10);
                document.add(resumen);
            }

            if (imprimirGrafico) {
                ChartService chartService = new ChartService();
                BufferedImage chartImage = chartService.generarGraficaConsumo(registros);

                ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
                ImageIO.write(chartImage, "png", chartBaos);

                Image pdfImage = Image.getInstance(chartBaos.toByteArray());
                pdfImage.scaleToFit(760, 320);
                pdfImage.setSpacingAfter(15);

                document.add(pdfImage);
            }

            if (imprimirTabla) {
                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1.2f, 2f, 2.5f, 3.5f, 2f, 1.5f});

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
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(new Color(25, 135, 84));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private String valor(String s) {
        return s != null ? s : "";
    }
}