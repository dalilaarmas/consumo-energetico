package com.dalila.service;

import com.dalila.dto.RegistroDTO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChartService {

    public BufferedImage generarGraficaConsumo(List<RegistroDTO> registros) {
        Map<String, Double> agrupado = agruparPorFecha(registros);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Double> entry : agrupado.entrySet()) {
            dataset.addValue(entry.getValue(), "Consumo energético", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Evolución del consumo energético",
                "Fecha",
                "Consumo (kWh)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
        );

        // --- MEJORA VISUAL ---
        CategoryPlot plot = chart.getCategoryPlot();

        // 1. Inclinamos el texto del eje X 45 grados para que se lea perfectamente sin chocarse
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);


        org.jfree.chart.renderer.category.LineAndShapeRenderer renderer =
                (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);

        return chart.createBufferedImage(1000, 450);
    }

    private Map<String, Double> agruparPorFecha(List<RegistroDTO> registros) {
        Map<String, Double> agrupado = new TreeMap<>();


        boolean agruparPorDia = registros.size() <= 60;

        for (RegistroDTO r : registros) {
            if (r.getFecha() == null || r.getConsumo() == null) continue;

            String clave = r.getFecha(); // Trae YYYY-MM-DD

            // Si hay más de 60 registros, cortamos el string para quedarnos solo con YYYY-MM
            if (!agruparPorDia && clave.length() >= 7) {
                clave = clave.substring(0, 7);
            }

            agrupado.put(clave, agrupado.getOrDefault(clave, 0.0) + r.getConsumo());
        }

        return agrupado;
    }
}